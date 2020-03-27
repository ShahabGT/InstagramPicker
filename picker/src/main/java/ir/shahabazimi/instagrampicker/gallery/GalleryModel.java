package ir.shahabazimi.instagrampicker.gallery;

public class GalleryModel {

    private String address;
    private boolean isSelected;

    GalleryModel(String address, boolean isSelected) {
        this.address = address;
        this.isSelected = isSelected;
    }

    String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    boolean isSelected() {
        return isSelected;
    }

    void setSelected(boolean selected) {
        isSelected = selected;
    }
}
