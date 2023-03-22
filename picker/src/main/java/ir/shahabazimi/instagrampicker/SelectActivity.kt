package ir.shahabazimi.instagrampicker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.shahabazimi.instagrampicker.databinding.ActivitySelectBinding

class SelectActivity : AppCompatActivity() {

    private var b: ActivitySelectBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(b?.root)
    }

    override fun onDestroy() {
        b = null
        super.onDestroy()
    }
}
