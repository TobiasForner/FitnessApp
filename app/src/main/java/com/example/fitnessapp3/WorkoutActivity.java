package com.example.fitnessapp3;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp3.SetResults.SetResult;


public class WorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        this.setTitle(CurrentWorkout.workoutName);

        if (CurrentWorkout.hasNextExercise()) {
            init();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        ProgressBar progressBar = findViewById(R.id.progressBar_workout);
        CurrentWorkout.setProgress(progressBar);
    }

    public void init() {
        //TODO untangle from duration-based exercises
        TextView exName = findViewById(R.id.exerciseName);
        TextView setProg = findViewById(R.id.setProgressText);
        exName.setText(CurrentWorkout.getWorkoutComponentName());
        WorkoutComponent nextWorkoutComponent = CurrentWorkout.getNextWorkoutComponent();
        if (nextWorkoutComponent.isExercise()) {
            Exercise exercise = (Exercise) nextWorkoutComponent;
            TextView exNum = findViewById(R.id.exerciseNumberInput);
            copyPreviousSetResult();
            if (exercise.getType() == Exercise.ExType.DURATION) {
                exNum.setHint("Duration");
            } else if (!exercise.isWeighted()) {
                hideWeightElements();
            }
            setPrevResults();
        }
        setProg.setText(CurrentWorkout.getSetString());
    }

    private void copyPreviousSetResult() {
        if (!CurrentWorkout.useLastWorkout) {
            return;
        }
        SetResult setResult = CurrentWorkout.getPrevSetResultsOfCurrentPosition();
        if (setResult == null) {
            return;
        }
        if(setResult.isDuration()){
            return;
        }

        TextView repNum = findViewById(R.id.repNumberInput);
        repNum.setText(String.valueOf(setResult.getRepNr()));

        TextView exNum = findViewById(R.id.exerciseNumberInput);
        exNum.setText(String.valueOf(setResult.getAddedWeight()));
    }

    private void hideWeightElements() {
        View weight_header = findViewById(R.id.text_weight_header);
        weight_header.setVisibility(View.INVISIBLE);
        View exNum = findViewById(R.id.exerciseNumberInput);
        exNum.setVisibility(View.INVISIBLE);
        View kg = findViewById(R.id.textView6);
        kg.setVisibility(View.INVISIBLE);
    }

    private void setPrevResults() {
        String prevResults = CurrentWorkout.getPrevResultsInWorkout();
        TextView prevResultsView = findViewById(R.id.prev_results_body);
        if (prevResults.length() > 0) {
            prevResultsView.setText(prevResults);
        } else {
            TextView prevResultsHeaderView = findViewById(R.id.prev_results_header);
            prevResultsView.setVisibility(View.INVISIBLE);
            prevResultsHeaderView.setVisibility(View.INVISIBLE);
        }
    }



    public void logExercise(View view) {
        assert view.getId()==R.id.button_workout_log_exercise;
        Exercise exercise = (Exercise) CurrentWorkout.getNextWorkoutComponent();
        TextView repNum = findViewById(R.id.repNumberInput);
        String repNumText = repNum.getText().toString();
        int repNumInt;
        try {
            repNumInt = Integer.parseInt(repNumText);
        } catch (NumberFormatException e) {
            showPopupWindowClick(repNum, getString(R.string.popup_unweighted));
            return;
        }
        if (exercise.isWeighted()) {
            TextView exNum = findViewById(R.id.exerciseNumberInput);
            String exNumText = exNum.getText().toString();
            try {
                int exNumInt = Integer.parseInt(exNumText);
                CurrentWorkout.logExercise(exNumInt, repNumInt, this);
            } catch (NumberFormatException e) {
                showPopupWindowClick(exNum, getString(R.string.popup));
                return;
            }
        } else {
            CurrentWorkout.logExercise(0, repNumInt, this);
        }

        if (CurrentWorkout.hasNextExercise()) {
            startActivity(ActivityTransition.goToNextActivityInWorkout(this));
        } else {
            CurrentWorkout.finishWorkout(this);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void onBackPressed() {
        CurrentWorkout.goBack();
        super.onBackPressed();
    }

    public void showPopupWindowClick(View view, String text) {

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
        popUpText.setText(text);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {
            v.performClick();
            popupWindow.dismiss();
            return true;
        });
    }
}