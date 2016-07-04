package com.jlee.mobile.actioncamera.modules;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Size;

import com.jlee.mobile.actioncamera.presenters.CameraViewPresenter;
import com.jlee.mobile.actioncamera.util.SizeHelper;

import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Android camera wrapper
 */
public class Camera implements CameraViewPresenter {
    private static final String TAG = Camera.class.getSimpleName();
    private static final int DEFAULT_SEMAPHORE_COUNT = 1;
    private static final int UNSELECTED = -1;

    private WeakReference<Context> context;
    private CameraViewPresenter.CameraView view;

    /**
     * Default camera lock semaphore
     */
    private Semaphore lock = new Semaphore(DEFAULT_SEMAPHORE_COUNT);

    private CameraManager manager;
    private CameraDevice camera;

    public Camera(Context context, CameraViewPresenter.CameraView view) {
        this.context = new WeakReference<>(context);
        this.view = view;

        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    private CameraCharacteristics getCameraDirection(int index) {
        String face = "Facing Unknown";
        String[] cameras;
        CameraCharacteristics characteristics = null;
        try {
            cameras = manager.getCameraIdList();

            String cam = cameras[index];
            characteristics = manager.getCameraCharacteristics(cam);
        } catch (CameraAccessException camEx) {
            return characteristics;
        }

        int lensDirection = UNSELECTED;
        if (characteristics.getKeys().contains(CameraCharacteristics.LENS_FACING)) {
            lensDirection = characteristics.get(CameraCharacteristics.LENS_FACING);
        } else {
            return characteristics;
        }

        switch (lensDirection) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                face = "Facing Front";
                break;
            case CameraCharacteristics.LENS_FACING_BACK:
                face = "Facing Back";
                break;
            case CameraCharacteristics.LENS_FACING_EXTERNAL:
                face = "Facing External";
                break;
        }

        Log.wtf(TAG, "Camera is facing " + face);
        return characteristics;
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public boolean openCamera(int cameraIndex) {
        if (camera != null) {
            camera.close();
            camera = null;
        }

        try {
            if (!lock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = getCameraDirection(cameraIndex);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size size = SizeHelper.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            Size previewSize = SizeHelper.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), size.getWidth(), size.getHeight(), size);

            Activity act = (Activity) context.get();
            if (act == null) {
                return false;
            }

            int orientation = act.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                view.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            } else {
                view.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
            }

            view.configureTransformSize(previewSize);

            String cameraId = manager.getCameraIdList()[cameraIndex];
            manager.openCamera(cameraId, cameraCallback, null);

            return true;
        } catch (CameraAccessException|InterruptedException|SecurityException e) {
            Snackbar.make((android.view.View) view, "Cannot access the camera.", Snackbar.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            Snackbar.make((android.view.View) view, "System doesn't support.", Snackbar.LENGTH_SHORT).show();
        }

        return false;
    }

    public void closeCamera() {
        try {
            lock.acquire();

            if (camera != null) {
                camera.close();
                camera = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            lock.release();
        }
    }

    private CameraDevice.StateCallback cameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            view.configureTransformSize(new Size(250, 250));

            view.startPreview(cameraDevice);
            lock.release();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            lock.release();
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            lock.release();
            cameraDevice.close();

            if (context.get() != null) {
                ((Activity) context.get()).finish();
            }
        }
    };
}
