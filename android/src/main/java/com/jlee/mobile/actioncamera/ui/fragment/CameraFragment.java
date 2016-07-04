package com.jlee.mobile.actioncamera.ui.fragment;

import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.jlee.mobile.actioncamera.R;
import com.jlee.mobile.actioncamera.module.Camera;
import com.jlee.mobile.actioncamera.module.Recorder;
import com.jlee.mobile.actioncamera.presenter.CameraViewPresenter;
import com.jlee.mobile.actioncamera.presenter.FullscreenHandler;
import com.jlee.mobile.actioncamera.presenter.FullscreenPresenter;
import com.jlee.mobile.actioncamera.ui.view.AutoFitView;
import com.jlee.mobile.actioncamera.util.SizeHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CameraFragment extends Fragment
        implements FullscreenPresenter.FullScreen, CameraViewPresenter.CameraView {
    private static final int BACK_FACING_CAMERA = 0;
    private static final int FRONT_FACING_CAMERA = 1;
    private static final Size DEFAULT_CAMERA_PREVIEW_SIZE = new Size(320, 240);

    private static final int CAMERA_PREVIEW_WIDTH = 1280;
    private static final int CAMERA_PREVIEW_HEIGHT = 720;

    private Camera camera;
    private Recorder recorder;

    private FullscreenPresenter fsPresenter;

    private CaptureRequest.Builder builder;
    private HandlerThread threadBg;

    private Unbinder unbinder;

    AutoFitView camView;

    public CameraFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.wtf("Fragment", "Inflating camera fragment");

        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        camView = (AutoFitView) view.findViewById(R.id.camera_autofit_preview);

        unbinder = ButterKnife.bind(this, view);

        fsPresenter = new FullscreenHandler(getActivity(), this);
        fsPresenter.show();

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        camera = new Camera(getActivity(), this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!camView.isAvailable()) {
            camView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    try {
                        camera.openCamera(BACK_FACING_CAMERA);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    SizeHelper.configureTransform(camView, getActivity(),
                            new Size(camView.getWidth(), camView.getHeight()), CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }

        try {
            camera.openCamera(BACK_FACING_CAMERA);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        camera.closeCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }

    @Override
    public void setVisibility(int visibility) {
        camView.setVisibility(visibility);
    }

    @Override
    public void setAspectRatio(int width, int height) {
        camView.setAspectRatio(width, height);
    }

    @Override
    public void setSystemUiVisibility(int flag) {
        camView.setSystemUiVisibility(flag);
    }

    @Override
    public void configureTransformSize(Size previewSize) {
        SizeHelper.configureTransform(camView, getActivity(), previewSize, CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT);
    }

    public void startPreview(final CameraDevice device) {
        if (camView == null || !camView.isAvailable()) {
            return;
        }

        try {
            SurfaceTexture texture = camView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT);
            builder = device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            List<Surface> surfaces = new ArrayList<>();

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            builder.addTarget(previewSurface);

//            Surface recorderSurface = mMediaRecorder.getSurface();
//            surfaces.add(recorderSurface);
//            viewBuilder.addTarget(recorderSurface);

            threadBg = new HandlerThread("cam bg");
            threadBg.start();

            device.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    updatePreview(device, builder, cameraCaptureSession);
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Snackbar.make(camView, "Failed to set capture view", Snackbar.LENGTH_LONG).show();
                }
            }, new Handler(threadBg.getLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    /**
     * Update the camera preview. {@link #startPreview(CameraDevice device)} needs to be called in advance.
     */
    private void updatePreview(CameraDevice device,
                               CaptureRequest.Builder builder,
                               CameraCaptureSession cameraCaptureSession) {
        if (device == null) {
            return;
        }

        try {
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();

            cameraCaptureSession.setRepeatingRequest(builder.build(), null, new Handler(threadBg.getLooper()));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
