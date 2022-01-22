package ir.shahabazimi.instagrampickerexample;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ir.shahabazimi.instagrampicker.InstagramPicker;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_button).setOnClickListener(w ->
                new InstagramPicker(MainActivity.this).show(1, 1, 5, addresses -> {
                    return null;
                }));

    }
}
