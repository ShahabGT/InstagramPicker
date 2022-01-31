package ir.shahabazimi.instagrampickerexample

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.shahabazimi.instagrampicker.InstagramPicker
import ir.shahabazimi.instagrampickerexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.mainButton.setOnClickListener {
            InstagramPicker(this).show(1, 1, 5) {
                b.mainPreview.setImageURI(Uri.parse(it[0]))
            }
        }

    }
}
