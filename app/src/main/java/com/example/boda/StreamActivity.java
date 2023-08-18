package com.example.boda;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.boda.utils.PathUtils;
import com.example.boda.utils.PermissionUtils;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StreamActivity extends AppCompatActivity
        implements ConnectCheckerRtsp, View.OnClickListener, SurfaceHolder.Callback {

    public static final String[] permissionList = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 200;

    private RtspCamera1 rtspCamera1;
    private Button button;
    private Button bRecord;

    private String currentDateAndTime = "";
    private File folder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);

        if (!PermissionUtils.haveAllPermission(StreamActivity.this, permissionList))
            ActivityCompat.requestPermissions(this, permissionList, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

        folder = PathUtils.getRecordPath();
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.b_start_stop);
        button.setOnClickListener(this);
        bRecord = findViewById(R.id.b_record);
        bRecord.setOnClickListener(this);
        Button switchCamera = findViewById(R.id.switch_camera);
        switchCamera.setOnClickListener(this);

        rtspCamera1 = new RtspCamera1(surfaceView, this);
        rtspCamera1.setReTries(10);
        surfaceView.getHolder().addCallback(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE &&
                grantResults.length == permissionList.length) {

            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( !check_result ) {
                // if permitted
                finish();
            }
//            else {
//                if (PermissionUtils.recheckPermission(this, permissionList)){
//                    finish();
//                }
//                else{
//                    finish();
//                }
//            }
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {}

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rtspCamera1.startPreview();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtspCamera1.isRecording()) {
            rtspCamera1.stopRecord();
            PathUtils.updateGallery(this, folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
            bRecord.setText("녹화시작");
            Toast.makeText(this,
                    "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            currentDateAndTime = "";
        }
        if (rtspCamera1.isStreaming()) {
            rtspCamera1.stopStream();
            button.setText("시작");
        }
        rtspCamera1.stopPreview();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.b_start_stop) {
            if (!rtspCamera1.isStreaming()) {
                if (rtspCamera1.isRecording()
                        || rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo()) {
                    button.setText("정지");
                    rtspCamera1.startStream("rtsp://10.0.2.2:8556/boda");
                } else {
                    Toast.makeText(this, "Error preparing stream, This device cant do it",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                button.setText("시작");
                rtspCamera1.stopStream();
            }
        } else if (id == R.id.switch_camera) {
            try {
                rtspCamera1.switchCamera();
            } catch (Exception e) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.b_record) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (!rtspCamera1.isRecording()) {
                    try {
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                        currentDateAndTime = sdf.format(new Date());
                        if (!rtspCamera1.isStreaming()) {
                            if (rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo()) {
                                rtspCamera1.startRecord(
                                        folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                                bRecord.setText("녹화시작");
                                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Error preparing stream, This device cant do it",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            rtspCamera1.startRecord(
                                    folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                            bRecord.setText("녹화중지");
                            Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        rtspCamera1.stopRecord();
                        PathUtils.updateGallery(this, folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                        bRecord.setText("녹화시작");
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    rtspCamera1.stopRecord();
                    PathUtils.updateGallery(this, folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                    bRecord.setText("녹화시작");
                    Toast.makeText(this,
                            "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onAuthErrorRtsp() {
        runOnUiThread(() -> {
            Toast.makeText(StreamActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
            rtspCamera1.stopStream();
            button.setText("시작");
        });
    }

    @Override
    public void onAuthSuccessRtsp() {
        runOnUiThread(() -> Toast.makeText(StreamActivity.this, "인증 성공", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onConnectionFailedRtsp(@NonNull String reason) {
        runOnUiThread(() -> {
            if (rtspCamera1.reTry(5000, reason, null)) {
                Toast.makeText(StreamActivity.this, "재시도", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(StreamActivity.this, "연결 실패: " + reason, Toast.LENGTH_SHORT)
                        .show();
                rtspCamera1.stopStream();
                button.setText("재시도");
            }
        });
    }

    @Override
    public void onConnectionStartedRtsp(@NonNull String s) {}

    @Override
    public void onConnectionSuccessRtsp() {
        runOnUiThread(() -> Toast.makeText(StreamActivity.this, "서버 연결 성공", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDisconnectRtsp() {
        runOnUiThread(() -> Toast.makeText(StreamActivity.this, "서버 연결 종료", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onNewBitrateRtsp(long l) {}
}
