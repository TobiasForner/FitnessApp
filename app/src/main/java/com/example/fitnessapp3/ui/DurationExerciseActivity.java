package com.example.fitnessapp3.ui;

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
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp3.data.CurrentWorkout;
import com.example.fitnessapp3.data.Exercise;
import com.example.fitnessapp3.R;
import com.example.fitnessapp3.SetResults.SetResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DurationExerciseActivity extends AppCompatActivity {
    private boolean playSound;

    private CountDownTimer timer;
    private NumberPicker minutesPicker;
    private NumberPicker secondsPicker;

    private int pos;

    private long millisRemaining = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duration_exercise);

        minutesPicker = findViewById(R.id.activity_duration_minute_picker);
        secondsPicker = findViewById(R.id.activity_duration_seconds_picker);

        ProgressBar progressBar = findViewById(R.id.progressBar_duration_exercise);
        CurrentWorkout.setProgress(progressBar);
        this.setTitle(CurrentWorkout.workoutName);
        playSound = getIntent().getBooleanExtra("playSound", true);
        TextView exName = findViewById(R.id.duration_exercise_name);
        exName.setText(CurrentWorkout.getWorkoutComponentName());
        timer = null;
        init();
        pos = CurrentWorkout.getWorkoutPosition();

        getOnBackPressedDispatcher().addCallback( new OnBackPressedCallback(true){
            @Override
            public void handleOnBackPressed(){
                CurrentWorkout.goBack();
                finish();
            }
        });
        millisRemaining = -1;

        if(savedInstanceState!=null){
        if(savedInstanceState.containsKey("minutesPicked")){
            minutesPicker.setValue(savedInstanceState.getInt("minutesPicked"));
        }
        if(savedInstanceState.containsKey("secondsPicked")){
            secondsPicker.setValue(savedInstanceState.getInt("secondsPicked"));
        }
            if(savedInstanceState.containsKey("millisRemaining")){
                millisRemaining = savedInstanceState.getLong("millisRemaining");
                if(millisRemaining>=0){
                    startCountdown(millisRemaining);
                }
            }
        }



    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("minutesPicked", minutesPicker.getValue());
        outState.putInt("secondsPicked", secondsPicker.getValue());
        outState.putLong("millisRemaining", millisRemaining);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(timer!=null){
            timer.cancel();
        }
    }

    public void init() {
        setSetProgress();
        setPrevResults();
        copyPreviousSetResult();
        setWeightInvisibleIfUnweighted();
        setDurationPicker();
    }

    private void setDurationPicker() {
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        minutesPicker.setTextSize(50f);


        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);
        secondsPicker.setTextSize(50f);

        SetResult prevResult = getPrevSetResult();
        if (prevResult == null) {
            return;
        }
        minutesPicker.setValue(prevResult.getRepNr() / 60);
        secondsPicker.setValue(prevResult.getRepNr() % 60);
    }


    private void setSetProgress() {
        TextView setProg = findViewById(R.id.duration_exercise_set_progress);
        setProg.setText(CurrentWorkout.getSetString());
    }

    private SetResult getPrevSetResult() {
        SetResult setResult;
        if (!CurrentWorkout.useLastWorkout) {
            setResult = CurrentWorkout.getPrevSetResultsOfCurrentExercise();
        } else {
            setResult = CurrentWorkout.getPrevSetResultsOfCurrentPosition();
        }
        return setResult;
    }


    private void copyPreviousSetResult() {
        SetResult setResult = getPrevSetResult();
        if (setResult == null) {
            return;
        }
        if (!setResult.isDuration()) {
            return;
        }

        TextView dur = findViewById(R.id.editText_duration);
        String repNr = String.valueOf(setResult.getRepNr());
        dur.setText(repNr);
        dur.setVisibility(View.INVISIBLE);


        EditText weight_text = findViewById(R.id.duration_exercise_weight_edit_text);
        weight_text.setText(String.valueOf(setResult.getAddedWeight()));
    }

    private void setPrevResults() {
        String prevResults = CurrentWorkout.getPrevResultsInWorkout();
        TextView prevResultsView = findViewById(R.id.duration_exercise_prev_results_body);
        if (!prevResults.isEmpty()) {
            prevResultsView.setText(prevResults);
        } else {
            TextView prevResultsHeaderView = findViewById(R.id.duration_exercise_prev_results_header);
            prevResultsView.setVisibility(View.INVISIBLE);
            prevResultsHeaderView.setVisibility(View.INVISIBLE);
        }
    }

    private void setWeightInvisibleIfUnweighted() {
        if (!((Exercise) CurrentWorkout.getCurrentWorkoutComponent()).isWeighted()) {
            View weight_text = findViewById(R.id.duration_exercise_weight_edit_text);
            View weight_header = findViewById(R.id.duration_exercise_weight_header);
            weight_header.setVisibility(View.INVISIBLE);
            weight_text.setVisibility(View.INVISIBLE);
        }
    }

    public void startDuration(View view) {
        assert view.getId() == R.id.button_start_duration;
        final int duration = minutesPicker.getValue() * 60 + secondsPicker.getValue();
        millisRemaining = duration * 1000L;
        startCountdown(millisRemaining);
    }

    private void startCountdown(long millis){
        final TextView durationText = findViewById(R.id.editText_duration);
        try {
            if (millis == 0) {
                showPopupWindowClick(durationText);
                return;
            }
        } catch (NumberFormatException e) {
            showPopupWindowClick(durationText);
            return;
        }

        durationText.setVisibility(View.VISIBLE);
        durationText.setEnabled(false);
        durationText.setClickable(false);
        durationText.setFocusable(false);
        durationText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        minutesPicker.setEnabled(false);
        minutesPicker.setClickable(false);
        minutesPicker.setFocusable(false);
        minutesPicker.setVisibility(View.INVISIBLE);
        secondsPicker.setEnabled(false);
        secondsPicker.setClickable(false);
        secondsPicker.setFocusable(false);
        secondsPicker.setVisibility(View.INVISIBLE);

        TextView minHeader = findViewById(R.id.duration_exercise_min_header);
        minHeader.setEnabled(false);
        minHeader.setVisibility(View.INVISIBLE);
        TextView secHeader = findViewById(R.id.duration_exercise_sec_header);
        secHeader.setEnabled(false);
        secHeader.setVisibility(View.INVISIBLE);

        Button skipButton = findViewById(R.id.duration_exercise_skip_button);
        skipButton.setVisibility(View.VISIBLE);
        Button startButton = findViewById(R.id.button_start_duration);
        startButton.setVisibility(View.INVISIBLE);

        startTimer(millis/1000);
    }

    private void startTimer(long seconds) {
        final TextView durationText = findViewById(R.id.editText_duration);

        timer = new CountDownTimer(seconds * 1000L, 1000) {

            public void onTick(long millisUntilFinished) {
                Date date = new Date(millisUntilFinished);
                String formattedDate = new SimpleDateFormat("mm:ss", Locale.GERMAN).format(date);
                durationText.setText(formattedDate);
                millisRemaining = millisUntilFinished;
            }

            public void onFinish() {
                logDuration((int)seconds);

                durationText.setText(getResources().getString(R.string.done));
                if (playSound) {
                    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                }
                if (CurrentWorkout.hasCurrentExercise()) {
                    goToNextActivity();
                } else {
                    finishWorkout();
                }
            }
        };
        timer.start();
    }

    private void logDuration(int duration) {
        if (((Exercise) CurrentWorkout.getCurrentWorkoutComponent()).isWeighted()) {
            TextView weight_text = findViewById(R.id.duration_exercise_weight_edit_text);
            int weight = Integer.parseInt(weight_text.getText().toString());
            CurrentWorkout.logWeightedDuration(duration, weight, this, pos);
        } else {
            CurrentWorkout.logDuration(duration, this, pos);
        }
    }

    private void finishWorkout() {
        CurrentWorkout.finishWorkout(this);
        goToNextActivity();
    }

    private void goToNextActivity() {
        if (!CurrentWorkout.hasCurrentExercise()) {
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

