package ir.shahabazimi.instagrampicker.gallery;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.theartofdev.edmodo.cropper.CropImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ir.shahabazimi.instagrampicker.InstagramPicker;
import ir.shahabazimi.instagrampicker.R;
import ir.shahabazimi.instagrampicker.TouchImageView;
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
    private List<GalleryModel> data = new ArrayList<>();
    private String selectedPic="";
    private List<String> selectedPics ;
    private Context context;
    private FragmentActivity activity;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v= inflater.inflate(R.layout.fragment_gallery, container, false);
        context = getContext();
        activity = getActivity();
        assert activity != null;
        ActionBar actionBar = ((AppCompatActivity)activity).getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getString(R.string.instagrampicker_gallery_title));
        setHasOptionsMenu(true);

        multiSelectBtn = v.findViewById(R.id.gallery_multiselect);
        if(!InstagramPicker.multiSelect){
            multiSelectBtn.setVisibility(View.GONE);
        }
        multiSelectBtn.setOnClickListener(w->{

            int positionView = ((GridLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstVisibleItemPosition();
            multiSelect=!multiSelect;
            multiSelectBtn.setImageResource(multiSelect? R.mipmap.ic_multi_enable:R.mipmap.ic_multi_disable);
            adapter = new GalleryAdapter(data, new GalleySelectedListener() {
                @Override
                public void onSingleSelect(String address) {
                    preview.setImageURI(Uri.parse(address));

                    selectedPic = address;
                }

                @Override
                public void onMultiSelect(List<String> addresses) {
                    if(!addresses.isEmpty()) {
                        selectedPic="";
                        preview.setImageURI(Uri.parse(addresses.get(addresses.size()-1)));

                        selectedPics = addresses;
                    }

                }
            },multiSelect);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            recyclerView.getLayoutManager().scrollToPosition(positionView);
        });

        preview = v.findViewById(R.id.gallery_view);

        recyclerView = v.findViewById(R.id.gallery_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),4, RecyclerView.VERTICAL,false));

        adapter = new GalleryAdapter(data, new GalleySelectedListener() {
            @Override
            public void onSingleSelect(String address) {
                preview.setImageURI(Uri.parse(address));

                selectedPic = address;
            }

            @Override
            public void onMultiSelect(List<String> addresses) {
                selectedPic="";
                if(!addresses.isEmpty()) {
                    preview.setImageURI(Uri.parse(addresses.get(0)));
                    selectedPics = addresses;
                }

            }
        },multiSelect);
        recyclerView.setAdapter(adapter);

        getPicturePaths();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Intent in = new Intent(getContext(), FilterActivity.class);
                in.putExtra("uri",resultUri);
                FilterActivity.picAddress=resultUri;
                startActivity(in);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            int x=InstagramPicker.x;
            int y=InstagramPicker.y;


            if(!selectedPic.isEmpty()) {
                CropImage.activity(Uri.parse(selectedPic))
                        .setAspectRatio(x, y)
                        .start(context, this);
            }else if( selectedPics.size()==1) {

                CropImage.activity(Uri.parse(selectedPics.get(0)))
                        .setAspectRatio(x, y)
                        .start(context, this);
            }else if(selectedPics.size()>1) {
                Intent i = new Intent(getActivity(),MultiSelectActivity.class);
                MultiSelectActivity.addresses=selectedPics;
                startActivity(i);
                activity.overridePendingTransition(R.anim.bottom_up_anim,R.anim.bottom_down_anim);

            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPicturePaths(){
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media._ID};

        try {
            if(getActivity()!=null) {
                Cursor cursor = getActivity().getContentResolver().query(allImagesuri, projection, null, null, MediaStore.Images.Media.DATE_ADDED);

                if (cursor != null && cursor.moveToFirst()) {

                    do {
                        String datapath = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))).toString();
                        GalleryModel model = new GalleryModel(datapath, false);

                        data.add(0, model);
                        adapter.notifyItemInserted(data.size());

                    } while (cursor.moveToNext());
                    if(!data.get(0).getAddress().isEmpty()) {
                        selectedPic = data.get(0).getAddress();
                        preview.setImageURI(Uri.parse(selectedPic));
                    }
                    cursor.close();
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }




}
