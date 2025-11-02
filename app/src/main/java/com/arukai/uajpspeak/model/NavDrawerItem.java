package com.arukai.uajpspeak.model;

public class NavDrawerItem {
    private boolean showNotify;
    private String title;
    private boolean isSelected;

    public NavDrawerItem() {
    }

    public NavDrawerItem(boolean showNotify, String title) {
        this.showNotify = showNotify;
        this.title = title;
    }

    public boolean isShowNotify() {
        return showNotify;
    }

    public void setShowNotify(boolean showNotify) {
        this.showNotify = showNotify;
    }

    public String getTitle() {
        return title;
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}
