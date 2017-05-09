package com.afollestad.materialcamerasample;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;

import java.io.File;
import java.text.DecimalFormat;

public class DemoFragment extends Fragment implements View.OnClickListener {

    private final static int CAMERA_RQ = 6969;
    private final static int PERMISSION_RQ = 84;

    public DemoFragment() {
    }

    public static DemoFragment getInstance() {
        DemoFragment fragment = new DemoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_demo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
    }

    private void bindViews(View view) {
        view.findViewById(R.id.launchCamera).setOnClickListener(this);
        view.findViewById(R.id.launchCameraStillshot).setOnClickListener(this);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Request permission to save videos in external storage
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_RQ);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onClick(View view) {
        File saveDir = null;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Only use external storage directory if permission is granted, otherwise cache directory is used by default
            saveDir = new File(Environment.getExternalStorageDirectory(), "MaterialCamera");
            saveDir.mkdirs();
        }
        Log.d("test","Material camera");

        MaterialCamera materialCamera = new MaterialCamera(this)
                .allowRetry(true)                                  // Whether or not 'Retry' is visible during playback
                .autoSubmit(false)                                 // Whether or not user is allowed to playback videos after recording. This can affect other things, discussed in the next section.
                .saveDir(saveDir)                               // The folder recorded videos are saved to
                .primaryColorAttr(R.attr.colorPrimary)             // The theme color used for the camera, defaults to colorPrimary of Activity in the constructor
                .showPortraitWarning(true)                         // Whether or not a warning is displayed if the user presses record in portrait orientation
                .defaultToFrontFacing(false)                       // Whether or not the camera will initially show the front facing camera
                .allowChangeCamera(true)                           // Allows the user to change cameras.
                .retryExits(false)                                 // If true, the 'Retry' button in the playback screen will exit the camera instead of going back to the recorder
                .restartTimerOnRetry(false)                        // If true, the countdown timer is reset to 0 when the user taps 'Retry' in playback
                .continueTimerInPlayback(false)                    // If true, the countdown timer will continue to go down during playback, rather than pausing.
                .videoEncodingBitRate(1024000)                     // Sets a custom bit rate for video recording.
                .audioEncodingBitRate(50000)                       // Sets a custom bit rate for audio recording.
                .videoFrameRate(24)                                // Sets a custom frame rate (FPS) for video recording.
                .qualityProfile(MaterialCamera.QUALITY_HIGH)       // Sets a quality profile, manually setting bit rates or frame rates with other settings will overwrite individual quality profile settings
                .videoPreferredHeight(720)                         // Sets a preferred height for the recorded video output.
                .videoPreferredAspect(4f / 3f)                     // Sets a preferred aspect ratio for the recorded video output.
                .maxAllowedFileSize(1024 * 1024 * 5)               // Sets a max file size of 5MB, recording will stop if file reaches this limit. Keep in mind, the FAT file system has a file size limit of 4GB.
                .iconRecord(R.drawable.mcam_action_capture)        // Sets a custom icon for the button used to start recording
                .iconStop(R.drawable.mcam_action_stop)             // Sets a custom icon for the button used to stop recording
                .iconFrontCamera(R.drawable.mcam_camera_front)     // Sets a custom icon for the button used to switch to the front camera
                .iconRearCamera(R.drawable.mcam_camera_rear)       // Sets a custom icon for the button used to switch to the rear camera
                .iconPlay(R.drawable.evp_action_play)              // Sets a custom icon used to start playback
                .iconPause(R.drawable.evp_action_pause)            // Sets a custom icon used to pause playback
                .iconRestart(R.drawable.evp_action_restart)        // Sets a custom icon used to restart playback
                .labelRetry(R.string.mcam_retry)                   // Sets a custom button label for the button used to retry recording, when available
                .labelConfirm(R.string.mcam_use_video)             // Sets a custom button label for the button used to confirm/submit a recording
                .autoRecordWithDelaySec(5)                         // The video camera will start recording automatically after a 5 second countdown. This disables switching between the front and back camera initially.
                .autoRecordWithDelayMs(5000)                       // Same as the above, expressed with milliseconds instead of seconds.
                .audioDisabled(false);                               // Starts the camera activity, the result will be sent back to the current Activity

        if (view.getId() == R.id.launchCameraStillshot)
            materialCamera.stillShot(); // launches the Camera in stillshot mode

        materialCamera.start(CAMERA_RQ);
    }

    private String readableFileSize(long size) {
        if (size <= 0) return size + " B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private String fileSize(File file) {
        return readableFileSize(file.length());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Received recording or error from MaterialCamera
        if (requestCode == CAMERA_RQ) {
            if (resultCode == Activity.RESULT_OK) {
                final File file = new File(data.getData().getPath());
                Toast.makeText(getActivity(), String.format("Saved to: %s, size: %s",
                        file.getAbsolutePath(), fileSize(file)), Toast.LENGTH_LONG).show();
            } else if (data != null) {
                Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            // Sample was denied WRITE_EXTERNAL_STORAGE permission
            Toast.makeText(getActivity(), "Videos will be saved in a cache directory instead of an external storage directory since permission was denied.", Toast.LENGTH_LONG).show();
        }
    }
}