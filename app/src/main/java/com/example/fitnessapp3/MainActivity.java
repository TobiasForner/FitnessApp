package com.example.fitnessapp3;


import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PositiveNegativeDialogFragment.NoticeDialogListener {
    public static final String EXTRA_MESSAGE = "com.example.fitnessapp3.MESSAGE";
    public static final String EXTRA_RETURN_DEST = "com.example.fitnessapp3.RETURN";
    private boolean do_restore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //params.setMargins(5,5,5,5);
        LinearLayout linear = findViewById(R.id.workout_linear_layout);
        linear.setGravity(Gravity.CENTER);
        for (String s : WorkoutManager.getWorkoutNamesList()) {
            TextView t = new TextView(this);
            t.setText(s);
            t.setTextSize(30);
            t.setGravity(Gravity.CENTER);
            t.setTextColor(Color.WHITE);

            MaterialCardView c = new MaterialCardView(this);

            c.setMinimumHeight(200);
            c.setStrokeColor(Color.GRAY);
            c.setStrokeWidth(3);
            c.setForegroundGravity(Gravity.CENTER);
            c.setBackgroundColor(Color.TRANSPARENT);
            Util.setMargins(c, 100, 100, 100, 100);
            LinearLayout cLinear = new LinearLayout(this);
            cLinear.setOrientation(LinearLayout.VERTICAL);
            cLinear.addView(t, params);
            c.addView(cLinear);

            TextView exercises = new TextView(this);
            exercises.setTextColor(Color.WHITE);
            exercises.setGravity(Gravity.CENTER);

            HashSet<String> foundExercises = new HashSet<>();
            ArrayList<String> exNames = new ArrayList<>();
            Workout w = WorkoutManager.getWorkout(s, this);
            for (int i = 0; i < Objects.requireNonNull(w).getLength(); i++) {
                WorkoutComponent comp = w.getComponentAt(i);
                if (comp.isExercise()) {
                    String exName = comp.getName();
                    if (!foundExercises.contains(exName)) {
                        foundExercises.add(exName);
                        exNames.add(exName);
                    }
                }
            }

            String overview = String.join(", ", exNames);
            exercises.setText(overview);
            cLinear.addView(exercises, params);


            c.setOnClickListener((v) -> startWorkout(s));
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
                    Log.d("MainActivity", "onActivityResult: " + uri.getPath());
                    try {
                        DocumentFile treeDoc = DocumentFile.fromTreeUri(this, uri);
                        assert treeDoc != null;
                        JSONObject fullBackup = fullJSONData();
                        treeDoc.createFile("application/json", "workout_backup.json");
                        DocumentFile res = treeDoc.findFile("workout_backup.json");
                        assert res != null;
                        Uri resUri = res.getUri();
                        ContentResolver contentResolver = getContentResolver();
                        OutputStream outputStream = contentResolver.openOutputStream(resUri);

                        assert outputStream != null;
                        outputStream.write(fullBackup.toString().getBytes(StandardCharsets.UTF_8));
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        Button selectDirButton = findViewById(R.id.selectDirectoryButton);
        selectDirButton.setOnClickListener(v -> {
            File docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            Uri uri = Uri.fromFile(docsDir);
            mGetContent.launch(uri);
        });

        do_restore=false;
        //register callback for restore file
        ActivityResultLauncher<String[]> restoreRes = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                uri -> {
                    // Handle the returned Uri

                    DocumentFile treeDoc = DocumentFile.fromSingleUri(this, uri);

                    assert treeDoc != null;
                    Uri resUri = treeDoc.getUri();
                    ContentResolver contentResolver = getContentResolver();

                    InputStream inputStream;
                    String contents="";
                    try {
                        inputStream = contentResolver.openInputStream(resUri);
                        InputStreamReader inputStreamReader =
                                new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                        StringBuilder stringBuilder = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                            String line = reader.readLine();
                            while (line != null) {
                                stringBuilder.append(line).append('\n');
                                line = reader.readLine();
                            }
                        } catch (IOException e) {
                            // Error occurred when opening raw file for reading.
                        } finally {
                            contents = stringBuilder.toString().trim();
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Log.d("MainActivity", "content read: "+contents);
                    //todo restore content
                    DialogFragment dialog;
                    dialog = new PositiveNegativeDialogFragment(R.string.confirm_restore, R.string.yes, R.string.cancel, "", 0);

                    dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
                    if(do_restore){
                        try {
                            JSONObject backup = new JSONObject(contents);
                            ExerciseManager exerciseManager = new ExerciseManager(this);
                            JSONArray exercises = backup.getJSONArray("exercises");
                            exerciseManager.overwriteExerciseDetailsJSON(exercises,this);
                            JSONObject workouts = backup.getJSONObject("workouts");
                            WorkoutManager.overwriteWorkouts(workouts, this);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }


                });
        Button restoreButton = findViewById(R.id.button_restore);
        restoreButton.setOnClickListener(v -> {
            String[] tmp = new String[1];
            tmp[0] = "application/json";
            restoreRes.launch(tmp);
        });
    }

    private JSONObject fullJSONData() throws JSONException {
        JSONObject res = new JSONObject();
        // store exercises
        ExerciseManager exerciseManager = new ExerciseManager(this);
        JSONArray exercises = exerciseManager.exercisesJson();
        res.put("exercises", exercises);
        // store workout details
        JSONObject workoutsJSON = WorkoutManager.workoutsJSON(this);
        res.put("workouts", workoutsJSON);
        // store results for last workouts (set strings)
        // TODO
        return res;
    }

    public void startWorkout(String workoutName) {
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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

        //overwrite internal
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}