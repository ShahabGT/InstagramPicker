package ir.shahabazimi.instagrampickerexample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ir.shahabazimi.instagrampicker.InstagramPicker;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InstagramPicker instagramPicker = new InstagramPicker(MainActivity.this);

        findViewById(R.id.main_button).setOnClickListener(w -> {
            //      CropXRatio and CropYRatio are ratio for cropping for example if you want to limit the users to
            //       only crop in 16:9 put 16,9

            //      numberOfPictures allows the user to choose more than on picture between 2 and 1000
            instagramPicker.show(1, 1, 5, addresses -> {
                //       receive image addresses in here
            });

            //       this way for just a picture
            instagramPicker.show(1, 1, 2, address -> {
                //        receive image address in here
            });

        });

    }
}
