package ir.shahabazimi.instagrampicker;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreference {
    private static MySharedPreference instance;
    private SharedPreferences sp;

    private MySharedPreference(Context context) {
        sp = context.getSharedPreferences("InstagramPicker", 0);
    }

    public static MySharedPreference getInstance(Context context) {
        if (instance == null)
            instance = new MySharedPreference(context);

        return instance;

    }

    public boolean getCameraPermission(){
        return sp.getBoolean("camera",false);

    }

    public void setCameraPermission(){
        SharedPreferences.Editor editor=sp.edit();
        editor.putBoolean("camera",true);
        editor.apply();
    }

    public boolean getStoragePermission(){
        return sp.getBoolean("external",false);

    }
    public void setStoragePermission(){
        SharedPreferences.Editor editor=sp.edit();
        editor.putBoolean("external",true);
        editor.apply();
    }
}
