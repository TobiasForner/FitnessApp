package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TimerActivity extends AppCompatActivity {
    CountDownTimer timer;
    int timeElapsed = 0;
    boolean playSound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        if (savedInstanceState != null) {
            timeElapsed = savedInstanceState.getInt("timeElapsed");
        }

        Intent intent = getIntent();
        startTimer(intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 30000) - timeElapsed);

        playSound = true;
    }

    public void skipTimer(View view) {
        timer.cancel();
        playSound = false;
        timer.onFinish();
    }

    private void startTimer(final int millisForTimer) {
        final TextView timeRemaining = findViewById(R.id.textView15);
        timer = new CountDownTimer(millisForTimer, 1000) {

            public void onTick(long millisUntilFinished) {
                Date date = new Date(millisUntilFinished);
                timeElapsed += 1000;
                String formattedDate = new SimpleDateFormat("mm:ss", Locale.GERMAN).format(date);
                timeRemaining.setText(formattedDate);

            }

            public void onFinish() {
                timeRemaining.setText(getResources().getString(R.string.done));
                if (playSound) {
                    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                }
                finish();
            }
        }.start();
    }
}