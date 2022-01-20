package ir.shahabazimi.instagrampicker.gallery

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.classes.BackgroundActivity
import ir.shahabazimi.instagrampicker.databinding.ActivitySelectBinding

class SelectActivity : AppCompatActivity() {
    private lateinit var b: ActivitySelectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySelectBinding.inflate(layoutInflater)
        setContentView(b.root)
        init()
    }

    private fun init() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.select_container) as NavHostFragment
        NavigationUI.setupWithNavController(b.selectBnv, navHostFragment.navController)
        setSupportActionBar(b.selectToolbar)
        BackgroundActivity.setActivity(this)
    }

}
