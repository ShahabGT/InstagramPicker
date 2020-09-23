package ir.shahabazimi.instagrampicker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import ir.shahabazimi.instagrampicker.classes.MultiListener;
import ir.shahabazimi.instagrampicker.classes.SingleListener;
import ir.shahabazimi.instagrampicker.gallery.SelectActivity;

import static ir.shahabazimi.instagrampicker.classes.Statics.INTENT_FILTER_ACTION_NAME;

public class InstagramPicker {

    private Activity activity;
    private MultiListener mListener;
    private SingleListener sListener;
    public static int x;
    public static int y;
    public static boolean multiSelect;
    public static int numberOfPictures;

    public static List<String> addresses;

    public InstagramPicker(@NonNull Activity activity) {
        this.activity = activity;
    }


    public void show(int CropXRatio, int CropYRatio, SingleListener listener) {
        addresses = new ArrayList<>();
        InstagramPicker.x = CropXRatio;
        InstagramPicker.y = CropYRatio;
        InstagramPicker.multiSelect = false;
        this.sListener = listener;
        Intent in = new Intent(activity, SelectActivity.class);
        activity.startActivity(in);
        activity.registerReceiver(br, new IntentFilter(INTENT_FILTER_ACTION_NAME));
    }

    public void show(int CropXRatio, int CropYRatio, int numberOfPictures, MultiListener listener) {
        addresses = new ArrayList<>();
        if (numberOfPictures < 2)
            numberOfPictures = 2;
        else if (numberOfPictures > 1000)
            numberOfPictures = 1000;

        InstagramPicker.x = CropXRatio;
        InstagramPicker.y = CropYRatio;
        InstagramPicker.multiSelect = true;
        InstagramPicker.numberOfPictures = numberOfPictures;

        this.mListener = listener;
        Intent in = new Intent(activity, SelectActivity.class);
        activity.startActivity(in);
        activity.registerReceiver(br, new IntentFilter(INTENT_FILTER_ACTION_NAME));

    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (multiSelect)
                mListener.selectedPics(addresses);
            else
                sListener.selectedPic(addresses.get(0));
            activity.unregisterReceiver(br);
        }
    };

    @Override
    protected void finalize() throws Throwable {
        activity.unregisterReceiver(br);
        super.finalize();
    }
}
