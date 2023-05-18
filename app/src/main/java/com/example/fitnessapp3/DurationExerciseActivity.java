package com.example.fitnessapp3;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp3.SetResults.SetResult;

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
        this.setTitle(CurrentWorkout.workoutName);
        playSound = getIntent().getBooleanExtra("playSound", true);
        TextView exName = findViewById(R.id.duration_exercise_name);
        exName.setText(CurrentWorkout.getWorkoutComponentName());
        timer = null;
        init();
    }

    public void init() {
        setSetProgress();
        setPrevResults();
        copyPreviousSetResult();
        setWeightInvisibleIfUnweighted();
    }


    private void setSetProgress() {
        TextView setProg = findViewById(R.id.duration_exercise_set_progress);
        setProg.setText(CurrentWorkout.getSetString());
    }


    private void copyPreviousSetResult() {
        SetResult setResult;
        if (!CurrentWorkout.useLastWorkout) {
            setResult = CurrentWorkout.getPrevSetResultsOfCurrentExercise();
        } else {
            setResult = CurrentWorkout.getPrevSetResultsOfCurrentPosition();
        }
        if (setResult == null) {
            return;
        }
        if (!setResult.isDuration()) {
            return;
        }

        TextView dur = findViewById(R.id.editText_duration);
        String repNr = String.valueOf(setResult.getRepNr());
        dur.setText(repNr);


        EditText weight_text = findViewById(R.id.duration_exercise_weight_edit_text);
        weight_text.setText(String.valueOf(setResult.getAddedWeight()));
    }

    private void setPrevResults() {
        String prevResults = CurrentWorkout.getPrevResultsInWorkout();
        TextView prevResultsView = findViewById(R.id.duration_exercise_prev_results_body);
        if (prevResults.length() > 0) {
            prevResultsView.setText(prevResults);
        } else {
            TextView prevResultsHeaderView = findViewById(R.id.duration_exercise_prev_results_header);
            prevResultsView.setVisibility(View.INVISIBLE);
            prevResultsHeaderView.setVisibility(View.INVISIBLE);
        }
    }

    private void setWeightInvisibleIfUnweighted() {
        if (!((Exercise) CurrentWorkout.getNextWorkoutComponent()).isWeighted()) {
            View weight_text = findViewById(R.id.duration_exercise_weight_edit_text);
            View weight_header = findViewById(R.id.duration_exercise_weight_header);
            weight_header.setVisibility(View.INVISIBLE);
            weight_text.setVisibility(View.INVISIBLE);
        }
    }

    public void startDuration(View view) {
        assert view.getId() == R.id.button_start_duration;
        final EditText durationText = findViewById(R.id.editText_duration);
        final int duration;
        try {
            duration = Integer.parseInt(durationText.getText().toString());
            if (duration == 0) {
                showPopupWindowClick(durationText);
                return;
            }
        } catch (NumberFormatException e) {
            showPopupWindowClick(durationText);
            return;
        }

        durationText.setEnabled(false);
        durationText.setClickable(false);
        durationText.setFocusable(false);
        Button skipButton = findViewById(R.id.duration_exercise_skip_button);
        skipButton.setVisibility(View.VISIBLE);
        Button startButton = findViewById(R.id.button_start_duration);
        startButton.setVisibility(View.INVISIBLE);

        createTimer(duration);
        timer.start();
    }

    private void createTimer(int duration) {
        final EditText durationText = findViewById(R.id.editText_duration);

        timer = new CountDownTimer(duration * 1000L, 1000) {

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
                    goToNextActivity();
                } else {
                    finishWorkout();
                }
            }
        };
    }

    private void logDuration(int duration) {
        if (((Exercise) CurrentWorkout.getNextWorkoutComponent()).isWeighted()) {
            TextView weight_text = findViewById(R.id.duration_exercise_weight_edit_text);
            int weight = Integer.parseInt(weight_text.getText().toString());
            CurrentWorkout.logWeightedDuration(duration, weight, this);
        } else {
            CurrentWorkout.logDuration(duration, this);
        }
    }

    private void finishWorkout() {
        CurrentWorkout.finishWorkout(this);
        goToNextActivity();
    }

    private void goToNextActivity() {
        if (!CurrentWorkout.hasNextExercise()) {
            CurrentWorkout.finishWorkout(this);
            startActivity(ActivityTransition.goToNextActivityInWorkout(this));
            finish();
        }
        startActivity(ActivityTransition.goToNextActivityInWorkout(this));
    }

    public void skipTimer(View view) {
        assert view.getId() == R.id.duration_exercise_skip_button;
        playSound = false;
        timer.onFinish();
        timer.cancel();
    }

    private void showPopupWindowClick(View view) {

        // inflate the popup_continue_previous_workout.xml of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        TextView popUpText = popupView.findViewById(R.id.popup_text);
        popUpText.setText("Please enter a valid duration.");

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {
            v.performClick();
            popupWindow.dismiss();
            return true;
        });
    }
}

