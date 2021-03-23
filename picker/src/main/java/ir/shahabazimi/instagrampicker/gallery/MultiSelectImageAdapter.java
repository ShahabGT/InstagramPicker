package ir.shahabazimi.instagrampicker.gallery;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MultiSelectImageAdapter extends PagerAdapter {

    private final Context context;
    private final List<String> addresses;
    private final SelectListener sl;


    MultiSelectImageAdapter(Context context, List<String> addresses, SelectListener sl) {
        this.context = context;
        this.addresses = addresses;
        this.sl = sl;
    }

    @Override
    public int getCount() {
        return addresses.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        String model = addresses.get(position);


            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                       imageView.setOnClickListener(w -> sl.onClick(model, position));

            Picasso.get().load(Uri.parse(model))
                    .into(imageView);
            container.addView(imageView);


            return imageView;

    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}
