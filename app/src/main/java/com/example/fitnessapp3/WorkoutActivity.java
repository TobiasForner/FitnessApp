package com.example.fitnessapp3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;


public class WorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

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
        TextView exName = findViewById(R.id.exerciseName);
        TextView setProg = findViewById(R.id.setProgressText);
        exName.setText(CurrentWorkout.getWorkoutComponentName());
        WorkoutComponent nextWorkoutComponent = CurrentWorkout.getNextWorkoutComponent();
        if (nextWorkoutComponent.isExercise()) {
            Exercise exercise = (Exercise) nextWorkoutComponent;
            TextView exNum = findViewById(R.id.exerciseNumberInput);
            if (CurrentWorkout.useLastWorkout) {
                    String[] prevNums = CurrentWorkout.getPrevResultsOfCurrentPosition();
                    if (prevNums.length >= 1) {
                        if(exercise.isWeighted() && prevNums.length==2){
                            exNum.setText(prevNums[0]);
                        }
                        TextView repNum = findViewById(R.id.repNumberInput);
                        repNum.setText(prevNums[1]);
                    }
                }
            if (exercise.getType() == Exercise.EXTYPE.DURATION) {
                exNum.setHint("Duration");
            } else if(!exercise.isWeighted()) {
                TextView weight_header = findViewById(R.id.text_weight_header);
                weight_header.setVisibility(View.GONE);
                exNum.setVisibility(View.GONE);
                View kg = findViewById(R.id.textView6);
                kg.setVisibility(View.GONE);
            }
            setPrevResults();
        }
        setProg.setText(CurrentWorkout.getSetString());
    }

    private void setPrevResults(){
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
        Exercise exercise = (Exercise) CurrentWorkout.getNextWorkoutComponent();
        TextView repNum = findViewById(R.id.repNumberInput);
        if(exercise.isWeighted()){
            TextView exNum = findViewById(R.id.exerciseNumberInput);
            if (!CurrentWorkout.logExercise(exNum.getText().toString(), repNum.getText().toString(), this)) {
                showPopupWindowClick(exNum, getString(R.string.popup));
                return;
            }
        }else{
            if (!CurrentWorkout.logExercise("0", repNum.getText().toString(), this)){
                showPopupWindowClick(repNum, getString(R.string.popup_unweighted));
                return;
            }
        }

        if (CurrentWorkout.hasNextExercise()) {
            startActivity(ActivityTransition.goToNextActivityInWorkout(this));
        } else {
            CurrentWorkout.finishWorkout(this);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
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
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        TextView popUpText = popupView.findViewById(R.id.popup_text);
        popUpText.setText(text);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                popupWindow.dismiss();
                return true;
            }
        });
    }


}