package ir.shahabazimi.instagrampicker.filter;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.zomato.photofilters.imageprocessors.Filter;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ir.shahabazimi.instagrampicker.InstagramPicker;
import ir.shahabazimi.instagrampicker.R;
import ir.shahabazimi.instagrampicker.classes.BackgroundActivity;
import ir.shahabazimi.instagrampicker.classes.Statics;

import static ir.shahabazimi.instagrampicker.classes.Statics.INTENT_FILTER_ACTION_NAME;


public class FilterActivity extends AppCompatActivity implements FiltersListFragment.FiltersListFragmentListener {


    private ImageView imagePreview;
    private Bitmap originalImage;
    private Bitmap filteredImage;
    private Bitmap finalImage;
    private FiltersListFragment filtersListFragment;
    public static int position = -1;
    public static Uri picAddress;


    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        Toolbar toolbar = findViewById(R.id.filter_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar()!=null;
        getSupportActionBar().setTitle(getString(R.string.instagrampicker_filter_title));

        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        imagePreview = findViewById(R.id.image_preview);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        new Handler().postDelayed(() -> {
            try {

                renderImage(picAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 500);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        filtersListFragment = new FiltersListFragment();
        filtersListFragment.setListener(this);

        adapter.addFragment(filtersListFragment, getString(R.string.tab_filters));

        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFilterSelected(Filter filter) {
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        imagePreview.setImageBitmap(filter.processFilter(filteredImage));
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true);
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open) {
            Statics.updatedPic = null;
            if (position != -1) {
                Intent i = new Intent("ImageUpdated");
                Statics.updatedPic = filteredImage;
                i.putExtra("position", position);
                sendBroadcast(i);
                FilterActivity.this.finish();
            } else {
                    try {
                        Bitmap b = finalImage;
                        File f = File.createTempFile("my_pic", ".jpeg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        byte[] pic = bos.toByteArray();
                        FileOutputStream fileOutputStream = new FileOutputStream(f);
                        fileOutputStream.write(pic);
                        fileOutputStream.close();
                        fileOutputStream.flush();
                        if(InstagramPicker.addresses==null){
                            InstagramPicker.addresses = new ArrayList<>();
                        }
                        InstagramPicker.addresses.add(Uri.fromFile(f).toString());
                        sendBroadcast(new Intent(INTENT_FILTER_ACTION_NAME));
                        FilterActivity.this.finish();
                        Objects.requireNonNull(BackgroundActivity.getInstance().getActivity()).finish();

                    } catch (Exception ignored) { }

            }
            return true;


        }

        return super.onOptionsItemSelected(item);
    }

    private void renderImage(Uri uri) throws Exception {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
        imagePreview.setImageBitmap(originalImage);
        bitmap.recycle();
        filtersListFragment.prepareThumbnail(originalImage);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int SELECT_GALLERY_IMAGE = 101;
        if (resultCode == RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) {
            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData());
            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
            imagePreview.setImageBitmap(originalImage);
            bitmap.recycle();
            filtersListFragment.prepareThumbnail(originalImage);
        } else {
            onBackPressed();
        }
    }
}