package com.example.fitnessapp3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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


}