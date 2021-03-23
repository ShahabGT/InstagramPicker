package ir.shahabazimi.instagrampicker.gallery;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ir.shahabazimi.instagrampicker.InstagramPicker;
import ir.shahabazimi.instagrampicker.R;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final List<GalleryModel> list;
    private final GalleySelectedListener galleySelectedListener;
    private int count=0;
    private final List<String> selectedPics;
    private final boolean multiSelect;

    GalleryAdapter( List<GalleryModel> list,GalleySelectedListener galleySelectedListener, boolean multiSelect){
        this.list=list;
        this.galleySelectedListener=galleySelectedListener;
        if(multiSelect) {
            count = InstagramPicker.numberOfPictures;
        }
        this.multiSelect=multiSelect;
        selectedPics = new ArrayList<>();
        if(!multiSelect){
            for(int i=1;i<list.size();i++)
                list.get(i).setSelected(false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_gallery_pics,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        GalleryModel model = list.get(position);

        Picasso.get().load(Uri.parse(model.getAddress()))
                .resize(150,150)
                .centerCrop()
                .into(h.pic);

        h.bgSelect.setVisibility(model.isSelected() ? View.VISIBLE : View.GONE);

        h.itemView.setOnClickListener(v->{
            if(multiSelect ) {

                if(count==selectedPics.size()){
                    if(model.isSelected()){
                        h.bgSelect.setVisibility(View.GONE);
                        selectedPics.remove(model.getAddress());
                    }
                    return;
                }

                model.setSelected(!model.isSelected());
                h.bgSelect.setVisibility(model.isSelected() ? View.VISIBLE : View.GONE);

                if (model.isSelected()) {
                    h.bgSelect.setChecked(!h.bgSelect.isChecked());
                    selectedPics.add(model.getAddress());
                } else {
                    selectedPics.remove(model.getAddress());
                }
                galleySelectedListener.onMultiSelect(selectedPics);


            }else{

                galleySelectedListener.onSingleSelect(model.getAddress());

            }

        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private final ImageView pic;
        private final RadioButton bgSelect;

        ViewHolder(@NonNull View v){
            super(v);
            pic = v.findViewById(R.id.row_gallery_pic);
            bgSelect = v.findViewById(R.id.row_gallery_select);
        }
    }
}
