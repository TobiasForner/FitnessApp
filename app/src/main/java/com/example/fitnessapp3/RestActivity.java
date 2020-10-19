package com.example.fitnessapp3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class RestActivity extends AppCompatActivity {
    CountDownTimer timer;
    int timeElapsed = 0;
    boolean playSound;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        playSound = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);
        if (savedInstanceState != null) {
            timeElapsed = savedInstanceState.getInt("timeElapsed");
        }
        Intent intent = getIntent();
        startTimer(intent.getIntExtra(MainActivity.EXTRA_MESSAGE, 30000) - timeElapsed);

        ProgressBar progressBar = findViewById(R.id.progressbar_rest);
        progressBar.setMin(0);
        progressBar.setMax(CurrentWorkout.getWorkoutLength());
        progressBar.setIndeterminate(false);
        progressBar.setProgress(CurrentWorkout.getWorkoutPosition() + 1);
    }

    public void skipTimer(View view) {
        timer.cancel();
        playSound = false;
        timer.onFinish();
    }

    public void onBackPressed() {
        CurrentWorkout.goBack();
        super.onBackPressed();
    }

    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt("timeElapsed", timeElapsed);

    }

    private void restartActivity() {
        Intent nextIntent = new Intent(this, RestActivity.class);
        startActivity(nextIntent);
    }

    private void logRest(int millis) {
        CurrentWorkout.logRest(millis, this);
    }

    private void goToNextActivity() {
        Intent intent = getIntent();
        if ("MainActivity".equals(Objects.requireNonNull(intent.getStringExtra(MainActivity.EXTRA_RETURN_DEST)))) {
            Intent nextIntent = new Intent(this, MainActivity.class);
            startActivity(nextIntent);
        } else if (CurrentWorkout.hasNextExercise()) {
            if (CurrentWorkout.getNextWorkoutComponent().isRest()) {
                restartActivity();
            } else {
                Intent nextIntent = new Intent(this, WorkoutActivity.class);
                startActivity(nextIntent);
            }
        }
    }

    private void startTimer(final int millisForTimer) {
        final TextView timeRemaining = findViewById(R.id.textView5);
        timer = new CountDownTimer(millisForTimer, 1000) {

            public void onTick(long millisUntilFinished) {
                Date date = new Date(millisUntilFinished);
                timeElapsed += 1000;
                String formattedDate = new SimpleDateFormat("mm:ss", Locale.GERMAN).format(date);
                timeRemaining.setText(formattedDate);

            }

            public void onFinish() {

                if ("WorkoutActivity".equals(Objects.requireNonNull(getIntent().getStringExtra(MainActivity.EXTRA_RETURN_DEST)))) {
                    logRest(millisForTimer);
                }
                timeRemaining.setText(getResources().getString(R.string.done));
                if (playSound) {
                    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                }
                if (CurrentWorkout.hasNextExercise() && CurrentWorkout.getNextWorkoutComponent().isRest()) {
                    restartActivity();
                }
                goToNextActivity();
            }
        }.start();
    }


}