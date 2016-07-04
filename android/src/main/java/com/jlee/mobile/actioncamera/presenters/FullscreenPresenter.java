package com.jlee.mobile.actioncamera.presenters;

public interface FullscreenPresenter {
    interface FullScreen {
        void setVisibility(int visible);
        void setSystemUiVisibility(int flag);
    }

    boolean isVisible();

    void toggle();
    void show();
    void hide();
}
