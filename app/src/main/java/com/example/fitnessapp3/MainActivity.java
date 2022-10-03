package com.example.fitnessapp3;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.card.MaterialCardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.fitnessapp3.MESSAGE";
    public static final String EXTRA_RETURN_DEST = "com.example.fitnessapp3.RETURN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout linear = findViewById(R.id.workout_linear_layout);
        linear.setGravity(Gravity.CENTER);
        for (String s : WorkoutManager.getWorkoutNames()) {
            TextView t = new TextView(this);
            t.setText(s);
            t.setTextSize(30);
            t.setGravity(Gravity.CENTER);
            t.setTextColor(Color.WHITE);

            CardView c = new MaterialCardView(this);
            c.setForegroundGravity(Gravity.CENTER);
            c.setBackgroundColor(Color.TRANSPARENT);
            Util.setMargins(c, 100, 100, 100, 100);
            LinearLayout cLinear = new LinearLayout(this);
            cLinear.setOrientation(LinearLayout.VERTICAL);
            cLinear.addView(t, params);
            c.addView(cLinear);

            /*TextView test = new TextView(this);
            test.setTextColor(Color.GREEN);
            test.setGravity(Gravity.CENTER);
            test.setText("blabla");
            cLinear.addView(test, params);*/


            c. setOnClickListener((v) -> startWorkout(s));
            linear.addView(c);
            Space space = new Space(this);
            linear.addView(space);
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int buttonWidth = width / 3 - 20;

        Button addExercise = findViewById(R.id.go_to_add_exercise_button);
        Button timerButton = findViewById(R.id.timerButton);
        Button initWorkoutsButton = findViewById(R.id.initWorkoutsButton);
        addExercise.setWidth(buttonWidth);
        timerButton.setWidth(buttonWidth);
        initWorkoutsButton.setWidth(buttonWidth);


        //register callback for backup directory
        ActivityResultLauncher<Uri> mGetContent = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
                uri -> {
                    // Handle the returned Uri
                    Log.d("MainActivity", "onActivityResult: "+uri.getPath());
                    try {

                        String treePath = uri.getPath();

                        assert treePath != null;
                        String[] parts = treePath.split(":");
                        String relativeDirName = parts[parts.length-1];
                        File docsDir= Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOCUMENTS);
                        File treeRoot=new File(docsDir.getPath(), relativeDirName);

                        if (!treeRoot.exists()) {
                            boolean success=treeRoot.mkdirs();
                            if(!success){
                                //TODO: surface warning to user
                            }
                        }
                        File backupFile = new File(treeRoot, "workoutBackup.json");
                        Log.d("MainActivity", "onActivityResult: writing to "+backupFile.getPath());
                        FileWriter writer = new FileWriter(backupFile);
                        BufferedWriter bw = new BufferedWriter(writer);
                        JSONObject fullBackup = fullJSONData();
                        bw.write(fullBackup.toString());
                        bw.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                });

        Button selectDirButton = findViewById(R.id.selectDirectoryButton);
        selectDirButton.setOnClickListener(v -> {
            File docsDir= Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOCUMENTS);
            Uri uri = Uri.fromFile(docsDir);
            mGetContent.launch(uri);
        });
    }

    private JSONObject fullJSONData() throws JSONException {
        JSONObject res = new JSONObject();
        // store exercises
        ExerciseManager exerciseManager=new ExerciseManager(this);
        JSONArray exercises = exerciseManager.exercisesJson();
        res.put("exercises", exercises);
        JSONObject abbreviations = exerciseManager.abbreviationsJson();
        res.put("abbreviations", abbreviations);
        // store workout details
        JSONArray workoutsJSON = WorkoutManager.workoutsJSON(this);
        res.put("workouts", workoutsJSON);
        // store results for last workouts (set strings)
        // TODO
        return res;
    }

    public void startWorkout( String workoutName) {
        CurrentWorkout.init(workoutName, this);
        startWorkout();
    }

    public void startTimer(View view) {
        Intent intent = new Intent(this, TimerActivity.class);
        int time = 180000;
        intent.putExtra(EXTRA_MESSAGE, time);
        intent.putExtra(EXTRA_RETURN_DEST, "MainActivity");
        startActivity(intent);
    }

    public void goToAddExercise(View view) {
        Intent intent = new Intent(this, AddExerciseActivity.class);
        startActivity(intent);
    }

    public void initWorkoutNamesFile(View view) {
        Startup.initWorkoutNamesFile(this);
    }

    private void startWorkout() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("workout_is_in_progress", true);
        editor.apply();
        startActivity(ActivityTransition.goToNextActivityInWorkout(this));
    }

    public void openEditWorkouts(View view) {
        Intent intent = new Intent(this, WorkoutEditActivity.class);
        startActivity(intent);
    }

    public void goToManageExercises(View v) {
        Intent intent = new Intent(this, ManageExercisesActivity.class);
        startActivity(intent);
    }
}