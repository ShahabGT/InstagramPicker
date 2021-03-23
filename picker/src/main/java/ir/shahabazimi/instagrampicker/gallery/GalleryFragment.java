package ir.shahabazimi.instagrampicker.gallery;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCrop.Options;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ir.shahabazimi.instagrampicker.InstagramPicker;
import ir.shahabazimi.instagrampicker.R;
import ir.shahabazimi.instagrampicker.classes.Statics;
import ir.shahabazimi.instagrampicker.classes.TouchImageView;
import ir.shahabazimi.instagrampicker.filter.FilterActivity;

import static android.app.Activity.RESULT_OK;


public class GalleryFragment extends Fragment {

    public GalleryFragment() {
    }

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private ImageView multiSelectBtn;
    private TouchImageView preview;
    private boolean multiSelect = false;
    private final List<GalleryModel> data = new ArrayList<>();
    private String selectedPic = "";
    private List<String> selectedPics;
    private Context context;
    private FragmentActivity activity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        context = getContext();
        activity = getActivity();
        assert activity != null;
        ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getString(R.string.instagrampicker_gallery_title));
        setHasOptionsMenu(true);

        multiSelectBtn = v.findViewById(R.id.gallery_multiselect);
        if (!InstagramPicker.multiSelect) {
            multiSelectBtn.setVisibility(View.GONE);
        }
        multiSelectBtn.setOnClickListener(w -> {

            int positionView = ((GridLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstVisibleItemPosition();
            multiSelect = !multiSelect;
            multiSelectBtn.setImageResource(multiSelect ? R.mipmap.ic_multi_enable : R.mipmap.ic_multi_disable);
            adapter = new GalleryAdapter(data, new GalleySelectedListener() {
                @Override
                public void onSingleSelect(String address) {
                    try {
                        preview.setImageBitmap(scale(address));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    selectedPic = address;
                }

                @Override
                public void onMultiSelect(List<String> addresses) {
                    if (!addresses.isEmpty()) {
                        selectedPic = "";
                        try {
                            preview.setImageBitmap(scale(addresses.get(addresses.size() - 1)));
                        } catch (Exception ignored) {
                        }
                        selectedPics = addresses;
                    }

                }
            }, multiSelect);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(positionView);
        });

        preview = v.findViewById(R.id.gallery_view);

        recyclerView = v.findViewById(R.id.gallery_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4, RecyclerView.VERTICAL, false));

        adapter = new GalleryAdapter(data, new GalleySelectedListener() {
            @Override
            public void onSingleSelect(String address) {
                try {
                    preview.setImageBitmap(scale(address));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                selectedPic = address;
            }

            @Override
            public void onMultiSelect(List<String> addresses) {
                selectedPic = "";
                if (!addresses.isEmpty()) {
                    try {
                        preview.setImageBitmap(scale(addresses.get(0)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    selectedPics = addresses;
                }

            }
        }, multiSelect);
        recyclerView.setAdapter(adapter);

        getPicturePaths();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            Uri resultUri = UCrop.getOutput(data);
            Intent in = new Intent(getContext(), FilterActivity.class);
            in.putExtra("uri", resultUri);
            FilterActivity.picAddress = resultUri;
            startActivity(in);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.withMaxResultSize(2000, 2000);
        options.setToolbarTitle(getString(R.string.instagrampicker_crop_title));
        if (id == R.id.action_open) {
            float x = InstagramPicker.x;
            float y = InstagramPicker.y;


            if (!selectedPic.isEmpty()) {
                UCrop.of(Uri.parse(selectedPic), Uri.fromFile(new File(requireActivity().getCacheDir(), Statics.getCurrentDate())))
                        .withAspectRatio(x, y)
                        .withOptions(options)
                        .start(context, this);

            } else if (selectedPics.size() == 1) {
                UCrop.of(Uri.parse(selectedPics.get(0)), Uri.fromFile(new File(requireActivity().getCacheDir(), Statics.getCurrentDate())))
                        .withAspectRatio(x, y)
                        .withOptions(options)
                        .start(context, this);
            } else if (selectedPics.size() > 1) {
                Intent i = new Intent(getActivity(), MultiSelectActivity.class);
                MultiSelectActivity.addresses = selectedPics;
                startActivity(i);
                activity.overridePendingTransition(R.anim.bottom_up_anim, R.anim.bottom_down_anim);

            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private int getOrientation(Uri filepath) {
        try {
            switch (new ExifInterface(filepath.getPath()).getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    private Bitmap scale(String address) throws Exception {
        Uri photoUri = Uri.parse(address);
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        int MAX_IMAGE_DIMENSION = 960;
        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    private void getPicturePaths() {
        Uri allImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media._ID};

        try {
            if (getActivity() != null) {
                Cursor cursor = getActivity().getContentResolver().query(allImagesUri, projection, null, null, MediaStore.Images.Media.DATE_ADDED);

                if (cursor != null && cursor.moveToFirst()) {

                    do {
                        String dataPath = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))).toString();
                        GalleryModel model = new GalleryModel(dataPath, false);

                        data.add(0, model);
                        adapter.notifyItemInserted(data.size());

                    } while (cursor.moveToNext());
                    if (!data.get(0).getAddress().isEmpty()) {
                        selectedPic = data.get(0).getAddress();
                        try {
                            preview.setImageBitmap(scale(selectedPic));
                        } catch (Exception ignored) {
                        }
                    }
                    cursor.close();
                }
            }
        } catch (Exception ignored) {
        }
    }
}
