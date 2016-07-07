package com.jlee.mobile.actioncamera.module;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Size;
import android.widget.Toast;

import com.jlee.mobile.actioncamera.presenter.CameraViewPresenter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Android media recorder wrapper
 */
public class Recorder implements CameraViewPresenter.CameraRecorder {
    private static final int[] ORIENTATIONS = {90, 0, 270, 180};
    private WeakReference<Context> context;
    private CameraViewPresenter.CameraView view;

    private Size size;
    private MediaRecorder recorder;

    public Recorder(Context context, Size size, CameraViewPresenter.CameraView view) {
        this.context = new WeakReference<>(context);
        this.view = view;
        this.size = size;

        recorder= new MediaRecorder();
    }

    @Override
    public void initialize(String output) throws IOException {
        Context ctx = context.get();
        if (ctx == null) {
            return;
        }

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(getVideoFile(ctx, output).getAbsolutePath());
        recorder.setVideoEncodingBitRate(10000000);
        recorder.setVideoFrameRate(30);
        recorder.setVideoSize(size.getWidth(), size.getHeight());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        int rotation = ((Activity) ctx).getWindowManager().getDefaultDisplay().getRotation();

        recorder.setOrientationHint(ORIENTATIONS[rotation]);
        recorder.prepare();
    }

    private File getVideoFile(Context context, String output) {
        if (TextUtils.isEmpty(output)) {
            output = "video.mp4";
        }

        return new File(context.getExternalFilesDir(null), output);
    }

    @Override
    public void start() {
        try {
            // Start recording
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Stop recording
        recorder.stop();
        recorder.reset();

        Activity activity = (Activity) context.get();
        if (activity != null) {
            Toast.makeText(activity, "Video saved: " + getVideoFile(activity, "video.mp4"),
                    Toast.LENGTH_SHORT).show();
        }

        view.startPreview(null);
    }

    @Override
    public void close() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}
