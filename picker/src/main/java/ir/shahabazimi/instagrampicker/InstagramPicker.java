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
    private MultiListener mListener;
    private SingleListener sListener;
    public static int x;
    public static int y;
    public static boolean multiSelect;
    public static int numberOfPictures;

    public static List<String> addresses;

    public InstagramPicker(Activity activity) {
        this.activity = activity;
    }
    /**
     * This method won't work anymore please use one of the bellow for single selection or multi selection
     *
     * @deprecated use {@link #show(int, int, SingleListener)} ()}
     * or {@link #show(int, int, int, MultiListener)} instead.
     */
    @Deprecated
    public void show(int CropXRatio, int CropYRatio, boolean multiSelect, InstagramPickerListener listener) {
        InstagramPicker.x = CropXRatio;
        InstagramPicker.y = CropYRatio;
        InstagramPicker.multiSelect = multiSelect;
        this.listener = listener;
        Intent in = new Intent(activity, SelectActivity.class);
        activity.startActivity(in);
        activity.registerReceiver(br, new IntentFilter("refreshPlease"));
    }

    public void show(int CropXRatio, int CropYRatio, SingleListener listener) {
        InstagramPicker.x = CropXRatio;
        InstagramPicker.y = CropYRatio;
        InstagramPicker.multiSelect = false;
        this.sListener = listener;
        Intent in = new Intent(activity, SelectActivity.class);
        activity.startActivity(in);
        activity.registerReceiver(br, new IntentFilter("refreshPlease"));
    }

    public void show(int CropXRatio, int CropYRatio, int numberOfPictures, MultiListener listener) {
        InstagramPicker.x = CropXRatio;
        InstagramPicker.y = CropYRatio;
        if (numberOfPictures > 1 && numberOfPictures < 1000) {
            InstagramPicker.multiSelect = true;
            InstagramPicker.numberOfPictures = numberOfPictures;
        } else {
            InstagramPicker.multiSelect = false;
        }
        this.mListener = listener;
        Intent in = new Intent(activity, SelectActivity.class);
        activity.startActivity(in);
        activity.registerReceiver(br, new IntentFilter("refreshPlease"));
    }

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (listener != null)
                listener.selectedPics(addresses);
            else {
                if (multiSelect)
                    mListener.selectedPics(addresses);
                else
                    sListener.selectedPic(addresses.get(0));
            }
        }
    };

    @Override
    protected void finalize() throws Throwable {
        activity.unregisterReceiver(br);
        super.finalize();
    }
}
