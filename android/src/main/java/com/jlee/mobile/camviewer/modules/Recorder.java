package com.jlee.mobile.camviewer.modules;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.util.Size;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Android media recorder wrapper
 */
public class Recorder {
    private static final int[] ORIENTATIONS = {90, 0, 270, 180};
    private Context context;

    private Size size;
    private MediaRecorder recorder;

    public void initialize(Context context, Size size) {
        this.context = context;
        this.size = size;

        recorder= new MediaRecorder();
    }

    private void setUpMediaRecorder() throws IOException {
        if (context == null) {
            return;
        }

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(getVideoFile(context).getAbsolutePath());
        recorder.setVideoEncodingBitRate(10000000);
        recorder.setVideoFrameRate(30);
        recorder.setVideoSize(size.getWidth(), size.getHeight());
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();

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

//        Activity activity = getActivity();
//        if (null != activity) {
//            Toast.makeText(activity, "Video saved: " + getVideoFile(activity),
//                    Toast.LENGTH_SHORT).show();
//        }
//        startPreview();
    }

    public void close() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}
