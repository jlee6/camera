package com.jlee.mobile.camviewer.ui.fragment;

import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.jlee.mobile.camviewer.R;
import com.jlee.mobile.camviewer.modules.Camera;
import com.jlee.mobile.camviewer.modules.Recorder;
import com.jlee.mobile.camviewer.ui.view.AutoFitView;
import com.jlee.mobile.camviewer.util.SizeHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CameraFragment extends Fragment {
    private static final int BACK_FACING_CAMERA = 0;
    private static final int FRONT_FACING_CAMERA = 1;
    private static final Size DEFAULT_CAMERA_PREVIEW_SIZE = new Size(320, 240);

    private Camera camera;
    private Recorder recorder;

    private Unbinder unbinder;

    @BindView(R.id.camera_preview)
    AutoFitView camView;

    public CameraFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        camera = new Camera(getActivity(), camView);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!camView.isAvailable()) {
            camView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    try {
                        camera.openCamera(BACK_FACING_CAMERA, new Size(1280, 720));
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    SizeHelper.configureTransform(camView, getActivity(),
                            new Size(camView.getWidth(), camView.getHeight()), 1280, 720);
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
            camera.openCamera(BACK_FACING_CAMERA, new Size(1280, 720));
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
        unbinder.unbind();
    }
}
