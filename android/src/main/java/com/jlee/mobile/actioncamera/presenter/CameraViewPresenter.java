package com.jlee.mobile.actioncamera.presenter;

import android.hardware.camera2.CameraDevice;
import android.util.Size;

import java.io.IOException;

public interface CameraViewPresenter {
    interface CameraView {
        void setAspectRatio(int width, int height);

        void startPreview(CameraDevice camera);
        void configureTransformSize(Size size);
    }

    interface CameraRecorder {
        void start();
        void stop();

        void initialize(String output) throws IOException;

        void close();
    }
}
