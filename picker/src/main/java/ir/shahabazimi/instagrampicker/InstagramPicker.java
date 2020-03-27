package ir.shahabazimi.instagrampicker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.List;

import ir.shahabazimi.instagrampicker.gallery.SelectActivity;

public class InstagramPicker {

    private Activity activity;
    private InstagramPickerListener listener;
    public static int x;
    public static int y;
    public static boolean multiSelect;

    public static List<String> addresses;

    public InstagramPicker(Activity activity){
        this.activity=activity;
    }
    public void show(int CropXRatio, int CropYRatio,boolean multiSelect,InstagramPickerListener listener){
        InstagramPicker.x=CropXRatio;
        InstagramPicker.y=CropYRatio;
        InstagramPicker.multiSelect=multiSelect;
        this.listener=listener;
        Intent in = new Intent(activity, SelectActivity.class);
        activity.startActivity(in);
        activity.registerReceiver(br,new IntentFilter("refreshPlease"));
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            listener.selectedPics(addresses);
        }
    };


}
