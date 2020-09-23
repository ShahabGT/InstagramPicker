package ir.shahabazimi.instagrampicker.classes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BackgroundActivity {
    private static BackgroundActivity instance;
    private AppCompatActivity activity=null;

    public static BackgroundActivity getInstance(){
        if(instance==null)
            instance = new BackgroundActivity();

        return instance;
    }

    @Nullable
    public AppCompatActivity getActivity() {
        return activity;
    }

    public void setActivity(@NonNull AppCompatActivity activity) {
        this.activity = activity;
    }
}
