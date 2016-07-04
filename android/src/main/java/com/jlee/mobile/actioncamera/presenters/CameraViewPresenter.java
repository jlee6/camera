package com.jlee.mobile.actioncamera.presenters;

import android.hardware.camera2.CameraDevice;
import android.util.Size;

public interface CameraViewPresenter {
    interface CameraView {
        void setAspectRatio(int width, int height);

        void startPreview(CameraDevice camera);
        void configureTransformSize(Size size);
    }
}
