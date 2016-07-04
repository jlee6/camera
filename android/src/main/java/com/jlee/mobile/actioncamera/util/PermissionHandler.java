package com.jlee.mobile.actioncamera.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.app.Fragment;
import android.util.Log;

/**
 * Created by jlee on 2/25/16.
 */
public class PermissionHandler {
    private static final String TAG = "CameraPermissionHandler";
    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param fragment A fragment which the permissions associated with
     * @return Whether you can show permission rationale UI.
     */
    private boolean shouldShowRequestPermissionRationale(Fragment fragment) {
        for (String permission : VIDEO_PERMISSIONS) {
            if (FragmentCompat.shouldShowRequestPermissionRationale(fragment, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Requests permissions needed for recording video.
     */
    private void requestVideoPermissions(Fragment fragment) {
//        if (shouldShowRequestPermissionRationale(fragment)) {
//            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
//        } else {
//            FragmentCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
//        }
    }

//    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
//                        ErrorDialog.newInstance(getString(R.string.permission_request))
//                                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
                        break;
                    }
                }
            } else {
//                ErrorDialog.newInstance(getString(R.string.permission_request))
//                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
//            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
//                    != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
        }
        return true;
    }
}
