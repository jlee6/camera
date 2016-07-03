package com.jlee.mobile.camviewer.presenters;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class FullscreenView implements FullscreenPresenter {
    private boolean visible;
    private Context context;

    // A main activity view
    private View view;

    public FullscreenView(Context context, View view) {
        visible = false;

        this.context = context;
        this.view = view;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void toggle() {
        visible = !visible;

        if (!visible) {
            show();
        } else {
            hide();
        }
    }

    @Override
    public void show() {
        // Show the system bar
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        visible = true;

//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    @Override
    public void hide() {
        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (view != null) {
            view.setVisibility(View.GONE);
        }
        visible = false;
    }
}
