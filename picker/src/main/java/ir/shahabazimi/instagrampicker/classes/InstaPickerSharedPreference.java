package ir.shahabazimi.instagrampicker.classes;

import android.content.Context;
import android.content.SharedPreferences;

public class InstaPickerSharedPreference {
    private static InstaPickerSharedPreference instance;
    private final SharedPreferences sp;

    private InstaPickerSharedPreference(Context context) {
        sp = context.getSharedPreferences("InstagramPickerSharedPreference", 0);
    }

    public static InstaPickerSharedPreference getInstance(Context context) {
        if (instance == null)
            instance = new InstaPickerSharedPreference(context);

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
