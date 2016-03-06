package com.jlee.mobile.camviewer.modules;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.Snackbar;
import android.util.Size;
import android.view.Surface;

import com.jlee.mobile.camviewer.ui.view.AutoFitView;
import com.jlee.mobile.camviewer.util.SizeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Android camera wrapper
 */
public class Camera {
    private static final int DEFAULT_SEMAPHORE_COUNT = 1;
    private Context context;
    private AutoFitView view;
    private Size size;

    /**
     * Default camera lock semaphore
     */
    private Semaphore lock = new Semaphore(DEFAULT_SEMAPHORE_COUNT);

    private CameraManager manager;
    private CameraDevice camera;

    /**
     * Camera preview.
     */
    private CaptureRequest.Builder viewBuilder;

    public Camera(Context context, AutoFitView view) {
        this.context = context;
        this.view = view;

        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public boolean openCamera(int cameraIndex, Size size) {
        this.size = size;

        if (camera != null) {
            camera.close();
            camera = null;
        }

        try {
            if (!lock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            for (String cam : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cam);
                String face;
                switch (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    case CameraCharacteristics.LENS_FACING_FRONT:
                        face = "Facing Front";
                        break;
                    case CameraCharacteristics.LENS_FACING_BACK:
                        face = "Facing Back";
                        break;
                    case CameraCharacteristics.LENS_FACING_EXTERNAL:
                        face = "Facing External";
                        break;
                    default:
                        face = "Facing Unknown";
                        break;
                }
            }

            String cameraId = manager.getCameraIdList()[cameraIndex];

            // Choose the sizes for camera preview and video recording
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            size = SizeHelper.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            Size previewSize = SizeHelper.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), size.getWidth(), size.getHeight(), size);

            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                view.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            } else {
                view.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
            }

            SizeHelper.configureTransform(view, (Activity) context, previewSize, size.getWidth(), size.getHeight());

            manager.openCamera(cameraId, cameraCallback, null);
            return true;
        } catch (CameraAccessException|InterruptedException|SecurityException e) {
            Snackbar.make(view, "Cannot access the camera.", Snackbar.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            Snackbar.make(view, "System doesn't support.", Snackbar.LENGTH_SHORT).show();
        }

        return false;
    }

    private HandlerThread threadBg;
    private void startPreview() {
        if (view == null || !view.isAvailable()) {
            return;
        }

        try {
            SurfaceTexture texture = view.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(size.getWidth(), size.getHeight());
            viewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            List<Surface> surfaces = new ArrayList<>();

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            viewBuilder.addTarget(previewSurface);

//            Surface recorderSurface = mMediaRecorder.getSurface();
//            surfaces.add(recorderSurface);
//            viewBuilder.addTarget(recorderSurface);

            threadBg = new HandlerThread("cam bg");
            threadBg.start();

            camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    updatePreview(cameraCaptureSession);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Snackbar.make(view, "Failed to set capture view", Snackbar.LENGTH_LONG).show();
                }
            }, new Handler(threadBg.getLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
        }
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

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview(CameraCaptureSession cameraCaptureSession) {
        if (camera == null) {
            return;
        }

        try {
            viewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();

            cameraCaptureSession.setRepeatingRequest(viewBuilder.build(), null, new Handler(threadBg.getLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback cameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            camera = cameraDevice;

            SizeHelper.configureTransform(view, (Activity) context, new Size(250, 250), size.getWidth(), size.getHeight());

            startPreview();
            lock.release();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            lock.release();
            cameraDevice.close();

            camera = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            lock.release();
            cameraDevice.close();
            camera = null;

            Activity activity = (Activity) context;
            if (activity != null) {
                activity.finish();
            }
        }
    };
}
