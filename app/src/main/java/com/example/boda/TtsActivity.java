package com.example.boda;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class TtsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, this);
        speakOut();
    }

    private void speakOut() {
        Intent intent = getIntent();
        String speechText = intent.getStringExtra("SpeechText");
        tts.setPitch((float) 1.0); // 음성 톤 높이 지정
        tts.setSpeechRate((float) 1.2); // 음성 속도 지정

        // TextToSpeech.QUEUE_FLUSH: play TTS directly
        // TextToSpeech.QUEUE_ADD: play TTS after this TTS ends
        tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "id1");
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(Locale.KOREA);
            if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                Log.d("TTSActivity", "This Language is not supported");
            } else {
                speakOut();
            }
        } else {
            Log.d("TTSActivity", "Initialization Failed");
        }
        finish();
    }
}
