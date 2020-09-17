package com.example.fitnessapp3;

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

    public void refreshExercise(){
        TextView exName = findViewById(R.id.exerciseName);
        exName.setText(CurrentWorkout.exercises[CurrentWorkout.position].getName());
        if(CurrentWorkout.getNextExercise().getType()== Exercise.EXTYPE.DURATION){
            TextView exNum = findViewById(R.id.exerciseNumberInput);
            exNum.setHint("Duration");
        }
    }

    public void logExercise(View view) {
        CurrentWorkout.position += 1;
        if (CurrentWorkout.hasNextExercise()) {
            if(CurrentWorkout.getNextExercise().getType()== Exercise.EXTYPE.REST){
                Intent intent = new Intent(this, RestActivity.class);
                int time = CurrentWorkout.getNextExercise().getParameter();
                intent.putExtra(MainActivity.EXTRA_MESSAGE, time);
                intent.putExtra(MainActivity.EXTRA_RETURN_DEST, "WorkoutActivity");
                startActivity(intent);
            }else{
                refreshExercise();
            }

        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}