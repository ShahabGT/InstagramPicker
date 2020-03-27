package ir.shahabazimi.instagrampickerexample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import ir.shahabazimi.instagrampicker.InstagramPicker;
import ir.shahabazimi.instagrampicker.InstagramPickerListener;

public class MainActivity extends AppCompatActivity {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.main_button);

        button.setOnClickListener(w->{
            InstagramPicker a = new InstagramPicker(MainActivity.this);
            // CropXRatio and CropYRatio are ratio for croping for example if you want to limit the users to
            // only crop in 16:9 put 16,9

            // multiselect allows the user to choose more than on picture
            a.show(1, 1, true, new InstagramPickerListener() {
                @Override
                public void selectedPics(List<String> addresses) {
                    // receive image address in here
                }
            });

        });

    }
}
