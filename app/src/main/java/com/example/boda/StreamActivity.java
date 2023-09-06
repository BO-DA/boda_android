package com.example.boda;

import android.Manifest;
import android.content.pm.PackageManager;
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

import com.example.boda.utils.PermissionUtils;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

public class StreamActivity extends AppCompatActivity
        implements ConnectCheckerRtsp, View.OnClickListener, SurfaceHolder.Callback {

    public static final String[] permissionList = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 200;
    private final String URL = "rtsp://10.0.2.2:8556/boda";

    private RtspCamera1 rtspCamera1;
    private Button button;

    private String currentDateAndTime = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);

        if (!PermissionUtils.haveAllPermission(StreamActivity.this, permissionList))
            ActivityCompat.requestPermissions(this, permissionList, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.b_start_stop);
        button.setOnClickListener(this);

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
                // if not permitted
                finish();
            }
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
                    rtspCamera1.startStream(URL);
                } else {
                    Toast.makeText(this, "연결에 오류가 발생하였습니다.",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                button.setText("시작");
                rtspCamera1.stopStream();
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
                Toast.makeText(StreamActivity.this, "연결에 실패하여 다시 연결을 시도합니다.", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(StreamActivity.this, reason, Toast.LENGTH_SHORT)
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
        runOnUiThread(() ->
                Toast.makeText(StreamActivity.this, "서버에 정상적으로 연결되었습니다.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDisconnectRtsp() {
        runOnUiThread(() ->
                Toast.makeText(StreamActivity.this, "서버와의 연결이 종료되었습니다.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onNewBitrateRtsp(long l) {}
}
