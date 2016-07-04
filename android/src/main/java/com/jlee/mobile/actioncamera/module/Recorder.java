package com.jlee.mobile.actioncamera.module;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.util.Size;
import android.widget.Toast;

import com.jlee.mobile.actioncamera.presenter.CameraViewPresenter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Android media recorder wrapper
 */
public class Recorder {
    private static final int[] ORIENTATIONS = {90, 0, 270, 180};
    private WeakReference<Context> context;
    private CameraViewPresenter.CameraView view;

    private Size size;
    private MediaRecorder recorder;

    public void initialize(Context context, Size size, CameraViewPresenter.CameraView view) {
        this.context = new WeakReference<>(context);
        this.view = view;
        this.size = size;

        recorder= new MediaRecorder();
    }

    private void setUpMediaRecorder() throws IOException {
        Context ctx = context.get();
        if (ctx == null) {
            return;
        }

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(getVideoFile(ctx).getAbsolutePath());
        recorder.setVideoEncodingBitRate(10000000);
        recorder.setVideoFrameRate(30);
        recorder.setVideoSize(size.getWidth(), size.getHeight());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        int rotation = ((Activity) ctx).getWindowManager().getDefaultDisplay().getRotation();

        recorder.setOrientationHint(ORIENTATIONS[rotation]);
        recorder.prepare();
    }

    private File getVideoFile(Context context) {
        return new File(context.getExternalFilesDir(null), "video.mp4");
    }

    private void startRecordingVideo() {
        try {
            // Start recording
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingVideo() {
        // Stop recording
        recorder.stop();
        recorder.reset();

        Activity activity = (Activity) context.get();
        if (null != activity) {
            Toast.makeText(activity, "Video saved: " + getVideoFile(activity),
                    Toast.LENGTH_SHORT).show();
        }

        view.startPreview(null);
    }

    public void close() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}
