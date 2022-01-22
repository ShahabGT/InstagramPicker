package ir.shahabazimi.instagrampicker.gallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.tmall.ultraviewpager.UltraViewPager;
import com.tmall.ultraviewpager.transformer.UltraScaleTransformer;
import com.yalantis.ucrop.UCrop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ir.shahabazimi.instagrampicker.InstagramPicker;
import ir.shahabazimi.instagrampicker.R;
import ir.shahabazimi.instagrampicker.classes.BackgroundActivity;
import ir.shahabazimi.instagrampicker.classes.Const;
import ir.shahabazimi.instagrampicker.classes.Statics;
import ir.shahabazimi.instagrampicker.filter.FilterActivity;

import static ir.shahabazimi.instagrampicker.classes.Statics.INTENT_FILTER_ACTION_NAME;


public class MultiSelectActivity extends AppCompatActivity {

    public static List<String> addresses;
    private List<String> finalAddresses;
    private int position = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_select);
        Toolbar toolbar = findViewById(R.id.multi_select_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.instagrampicker_multi_select_title));
        finalAddresses = new ArrayList<>();
        finalAddresses.addAll(addresses);
        initViewPager(finalAddresses);
        registerReceiver(br, new IntentFilter("ImageUpdated"));

    }

    private void initViewPager(List<String> addressesList) {
        UltraViewPager ultraViewPager = findViewById(R.id.multi_select_pager);
        ultraViewPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        MultiSelectImageAdapter adapter = new MultiSelectImageAdapter(this, addressesList, (a, p) -> {
            position = p;
            UCrop.Options options = new UCrop.Options();
            options.setToolbarTitle(getString(R.string.instagrampicker_crop_title));
            UCrop.of(Uri.parse(a),Uri.fromFile(new File(getCacheDir(), Statics.getCurrentDate())))
                    .withAspectRatio(4,3)
                    .withOptions(options)
                    .start(this);

        });
        ultraViewPager.setAdapter(adapter);
        ultraViewPager.setPageTransformer(true, new UltraScaleTransformer());
        ultraViewPager.initIndicator();
        ultraViewPager.getIndicator()
                .setOrientation(UltraViewPager.Orientation.HORIZONTAL)
                .setFocusColor(0xFFFCD736)
                .setNormalColor(0xFFECEFF1)
                .setRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        ultraViewPager.getIndicator().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        ultraViewPager.getIndicator().setMargin(0, 0, 0, 30);
        ultraViewPager.setMultiScreen(0.6f);
        ultraViewPager.setItemRatio(1.0f);
        ultraViewPager.getIndicator().build();

    }


    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            try {
                Bitmap b = Statics.updatedPic;
                int p = Objects.requireNonNull(data.getExtras()).getInt("position");
                File f = File.createTempFile("mypic", ".jpeg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                byte[] pic = bos.toByteArray();
                FileOutputStream fileOutputStream = new FileOutputStream(f);
                fileOutputStream.write(pic);
                fileOutputStream.close();
                fileOutputStream.flush();
                finalAddresses.set(p,Uri.fromFile(f).toString());
                initViewPager(finalAddresses);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(br);
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

            Const.addresses= finalAddresses;
            sendBroadcast(new Intent(INTENT_FILTER_ACTION_NAME));
            MultiSelectActivity.this.finish();
            Objects.requireNonNull(BackgroundActivity.Companion.getActivity()).finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data!=null) {
            Uri resultUri = UCrop.getOutput(data);
            Intent in = new Intent(this, FilterActivity.class);
            FilterActivity.picAddress = resultUri;
            FilterActivity.position = position;
            startActivityForResult(in, 123);
        }
    }
}