package com.jlee.mobile.camviewer.presenters;

public interface FullscreenPresenter {
    boolean isVisible();

    void toggle();
    void show();
    void hide();
}
