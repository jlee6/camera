package com.jlee.mobile.actioncamera.ui.fragment;

import android.app.Fragment;

/**
 * Created by jlee on 2/28/16.
 */
public class Factory {
    public static final int CAMERA_FRAGMENT = 1;

    public static Fragment createFragment(int type) {
        Fragment fragment = null;
        switch (type) {
            case CAMERA_FRAGMENT:
                fragment = new CameraFragment();
        }

        return fragment;
    }
}
