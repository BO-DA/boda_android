package com.example.boda;

import android.Manifest;
import android.content.Intent;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class SttActivity extends AppCompatActivity {

    Intent intent;
    SpeechRecognizer mRecognizer;
    ImageButton sttBtn;
    TextView textView;
    final int PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt);

        // 퍼미션 체크
        if ( Build.VERSION.SDK_INT >= 23 ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }

        // xml의 버튼과 텍스트 뷰 연결
        textView = (TextView)findViewById(R.id.stt_text);
        textView.setText("목적지를 말씀해주세요.");
        sttBtn = (ImageButton)findViewById(R.id.stt_button);

        // RecognizerIntent 객체 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        // 버튼을 클릭 이벤트 - 객체에 Context와 listener를 할당한 후 실행
        sttBtn.setOnClickListener(v -> {
            mRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        });

    }

    // RecognizerIntent 객체에 할당할 listener 생성
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(),"음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
//            Toast.makeText(getApplicationContext(),"말하기 시작", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {
//            Toast.makeText(getApplicationContext(),"말하기 중지", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "error: AUDIO";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "error: CLIENT";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "error: INSUFFICIENT_PERMISSION";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "error: NETWORK";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "error: NETWORK_TIMEOUT";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "error: NO_MATCH";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "error: RECOGNIZER_BUSY";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "error: SERVER";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "error: SPEECH_TIMEOUT";
                    break;
                default:
                    message = "error: unknown";
                    break;
            }

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            String sttResult = "";
            for (int i = 0; i < matches.size(); i++) {
                sttResult += matches.get(i);
                textView.setText(matches.get(i));
            }
            Log.d("여기", sttResult);

            Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainIntent.putExtra("sttResult", sttResult);
            setResult(RESULT_OK, mainIntent);

            startActivity(mainIntent);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };
}
