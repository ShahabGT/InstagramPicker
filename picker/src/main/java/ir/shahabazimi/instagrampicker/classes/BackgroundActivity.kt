package ir.shahabazimi.instagrampicker.classes

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity

class BackgroundActivity {


    companion object {
        private lateinit var activity: AppCompatActivity

        @Nullable
        fun getActivity() = activity

        fun setActivity(@NonNull activity: AppCompatActivity) {
            this.activity = activity
        }

    }

}
