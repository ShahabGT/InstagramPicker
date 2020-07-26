package ir.shahabazimi.instagrampickerexample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import ir.shahabazimi.instagrampicker.InstagramPicker;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_button).setOnClickListener(w->{
            InstagramPicker a = new InstagramPicker(MainActivity.this);
            // CropXRatio and CropYRatio are ratio for cropping for example if you want to limit the users to
            // only crop in 16:9 put 16,9

            // numberOfPictures allows the user to choose more than on picture between 2 and 1000
            a.show(1, 1,5, addresses -> {
                // receive image address in here
            });

            // this way for just a picture
            a.show(1, 1, address -> {
                // receive image address in here
            });

        });

    }
}
