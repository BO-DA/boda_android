package com.example.boda;

import static com.example.boda.Config.STREAMING_SERVER;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.boda.utils.PermissionUtils;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import android.view.ViewGroup.LayoutParams;

public class StreamActivity extends AppCompatActivity
        implements ConnectCheckerRtsp, View.OnClickListener, SurfaceHolder.Callback {

    public static final String[] permissionList = {
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 200;

    private RtspCamera1 rtspCamera1;
    private Button button;

//    private SocketIOService mService;
    private boolean mBound = false;
    private Socket mSocket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stream);

        MainFragment mainFragment = new MainFragment();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map_view, mainFragment);
        fragmentTransaction.commit();

        if (!PermissionUtils.haveAllPermission(StreamActivity.this, permissionList))
            ActivityCompat.requestPermissions(this, permissionList, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        button = findViewById(R.id.b_start_stop);
        button.setOnClickListener(this);

        rtspCamera1 = new RtspCamera1(surfaceView, this);
        rtspCamera1.setReTries(10);
        surfaceView.getHolder().addCallback(this);



        try {
            mSocket = IO.socket("http://10.0.2.2:13245");
            mSocket.connect();
            Log.d("WPW", String.valueOf(mSocket.connected()));
        } catch(URISyntaxException e) {
            e.printStackTrace();
        }

        // Bind to the service
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
//
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        DrawOnTop mDraw = new DrawOnTop(this);
        addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));


    }

    class DrawOnTop extends View {

        public DrawOnTop(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);                    // 적색
            paint.setStrokeWidth(20);                     // 크기 10
            canvas.drawLine(100, 100, 500, 500, paint);    // 라인그리기

            super.onDraw(canvas);
        }

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
                if (rtspCamera1.prepareVideo()) {
                    button.setText("정지");
                    rtspCamera1.startStream(STREAMING_SERVER);
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

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        // Unbind from the service
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
//    }

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
