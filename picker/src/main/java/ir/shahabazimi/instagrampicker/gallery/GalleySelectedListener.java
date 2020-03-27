package ir.shahabazimi.instagrampicker.gallery;

import java.util.List;

public interface GalleySelectedListener {

    void onSingleSelect(String address);
    void onMultiSelect(List<String> addresses);

}
