package ir.shahabazimi.instagrampicker.classes;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Statics {
    public static Bitmap updatedPic;
    public static final String INTENT_FILTER_ACTION_NAME ="instagrampicker_refresh";


    public static String getCurrentDate(){
        Date d = new Date();
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(d);
    }
}
