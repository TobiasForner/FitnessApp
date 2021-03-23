package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DurationExerciseActivity extends AppCompatActivity {
    private boolean playSound;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duration_exercise);

        ProgressBar progressBar = findViewById(R.id.progressBar_duration_exercise);
        CurrentWorkout.setProgress(progressBar);
        playSound = getIntent().getBooleanExtra("playSound", true);
        TextView exName= findViewById(R.id.duration_exercise_name);
        exName.setText(CurrentWorkout.getWorkoutComponentName());
        timer=null;
        init();
    }

    public void init() {
        String[] prevNums = CurrentWorkout.getPrevResultsOfCurrentPosition();
        TextView dur = findViewById(R.id.editText_duration);
        if(prevNums.length>0){
            dur.setText(prevNums[0]);
        }
    }

    public void startDuration(View view) {
        final EditText durationText = findViewById(R.id.editText_duration);
        final int duration = Integer.parseInt(durationText.getText().toString());
        durationText.setEnabled(false);
        durationText.setClickable(false);
        durationText.setFocusable(false);
        Button skipButton = findViewById(R.id.duration_exercise_skip_button);
        skipButton.setVisibility(View.VISIBLE);

        timer= new CountDownTimer(duration * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                Date date = new Date(millisUntilFinished);
                String formattedDate = new SimpleDateFormat("mm:ss", Locale.GERMAN).format(date);
                durationText.setText(formattedDate);
            }

            public void onFinish() {
                logDuration(duration);

                durationText.setText(getResources().getString(R.string.done));
                if (playSound) {
                    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                }
                if (CurrentWorkout.hasNextExercise()) {
                    if( CurrentWorkout.getNextWorkoutComponent().isExercise() && ((Exercise) CurrentWorkout.getNextWorkoutComponent()).getType() == Exercise.EXTYPE.DURATION)
                    restartActivity();
                } else {
                    finishWorkout();
                }
            }
        }.start();

    }

    private void logDuration(int duration) {
        CurrentWorkout.logDuration(duration, this);
    }

    private void restartActivity() {
        Intent intent = new Intent(this, DurationExerciseActivity.class);
        startActivity(intent);
    }

    private void goToNextActivity() {
        startActivity(ActivityTransition.goToNextActivityInWorkout(this));
    }

    private void finishWorkout(){
        CurrentWorkout.finishWorkout(this);
        goToNextActivity();
    }

    public void skipTimer(View view){
        timer.onFinish();
        timer.cancel();
    }
}

