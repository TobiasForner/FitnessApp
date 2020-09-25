package com.example.fitnessapp3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


public class WorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        if (CurrentWorkout.hasNextExercise()) {
            refreshExercise();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    public void refreshExercise() {
        TextView exName = findViewById(R.id.exerciseName);
        TextView setProg = findViewById(R.id.setProgressText);
        exName.setText(CurrentWorkout.exercises[CurrentWorkout.position].getName());
        if (CurrentWorkout.getNextExercise().getType() == Exercise.EXTYPE.WEIGHT) {
            if (CurrentWorkout.useLastWorkout) {
                String[] prevNums = CurrentWorkout.lastWorkout[CurrentWorkout.position].split(",");
                if (prevNums.length == 2) {
                    TextView exNum = findViewById(R.id.exerciseNumberInput);
                    exNum.setText(prevNums[0]);
                    TextView repNum = findViewById(R.id.repNumberInput);
                    repNum.setText(prevNums[1]);
                }
            }
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
        if (CurrentWorkout.getNextExercise().getType() == Exercise.EXTYPE.DURATION) {
            TextView exNum = findViewById(R.id.exerciseNumberInput);
            exNum.setHint("Duration");
        }
        setProg.setText(CurrentWorkout.setStrings[CurrentWorkout.position]);
    }

    public void logExercise(View view) {

        TextView exNum = findViewById(R.id.exerciseNumberInput);
        TextView repNum = findViewById(R.id.repNumberInput);
        if (exNum.getText().toString().equals("") || repNum.getText().toString().equals("")) {
            showPopupWindowClick(exNum);
            return;
        }
        CurrentWorkout.logExercise(exNum.getText().toString(), repNum.getText().toString());
        if (CurrentWorkout.hasNextExercise()) {
            if (CurrentWorkout.getNextExercise().getType() == Exercise.EXTYPE.REST) {
                Intent intent = new Intent(this, RestActivity.class);
                int time = CurrentWorkout.getNextExercise().getParameter();
                intent.putExtra(MainActivity.EXTRA_MESSAGE, time);
                intent.putExtra(MainActivity.EXTRA_RETURN_DEST, "WorkoutActivity");
                startActivity(intent);
            } else {
                refreshExercise();
            }

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

    public void showPopupWindowClick(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        TextView popUpText = findViewById(R.id.popup_text);
        //popUpText.setText(text);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }


}