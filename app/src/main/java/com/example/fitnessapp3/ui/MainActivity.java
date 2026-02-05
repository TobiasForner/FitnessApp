package com.example.fitnessapp3.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.fitnessapp3.data.CurrentWorkout;
import com.example.fitnessapp3.R;
import com.example.fitnessapp3.util.Util;
import com.example.fitnessapp3.WeightActivity2;
import com.example.fitnessapp3.data.Workout;
import com.example.fitnessapp3.data.WorkoutComponent;
import com.example.fitnessapp3.data.WorkoutManager;
import com.example.fitnessapp3.data.WorkoutStats;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PositiveNegativeDialogFragment.NoticeDialogListener {
    public static final String EXTRA_MESSAGE = "com.example.fitnessapp3.MESSAGE";
    public static final String EXTRA_RETURN_DEST = "com.example.fitnessapp3.RETURN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();

        int width = windowMetrics.getBounds().width();
        int buttonWidth = width / 3 - 20;

        Button addExercise = findViewById(R.id.go_to_add_exercise_button);

        addExercise.setWidth(buttonWidth);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout linear = findViewById(R.id.workout_linear_layout);
        linear.setGravity(Gravity.CENTER);
        linear.removeAllViews();
        List<String> workoutNames = WorkoutManager.getWorkoutNamesList();
        workoutNames.sort(Comparator.naturalOrder());
        Map<String, WorkoutStats> workoutToSortScore = new HashMap<>();
        //load workoutStats to determine sorting
        String workoutStats = Util.readFromInternal("workout_stats.json", this);
        JSONObject workoutStatsJSON;
        if (workoutStats == null) {
            workoutStatsJSON = new JSONObject();
        } else {
            try {
                workoutStatsJSON = new JSONObject(workoutStats);
            } catch (JSONException e) {
                workoutStatsJSON = new JSONObject();
            }
        }
        for (String s : workoutNames) {
            JSONObject currWorkoutStats;
            WorkoutStats cWorkoutStats = new WorkoutStats(0, "", 999999999, 99999999,9999999);
            cWorkoutStats.posInSortedNames = workoutNames.indexOf(s);
            if (workoutStatsJSON.has(s)) {
                try {
                    currWorkoutStats = workoutStatsJSON.getJSONObject(s);

                    if (currWorkoutStats.has("count")) {
                        cWorkoutStats.count = currWorkoutStats.getInt("count");
                    }

                    if (currWorkoutStats.has("lastCompletion")) {
                        cWorkoutStats.lastCompletedDate = currWorkoutStats.getString("lastCompletion");
                    }
                    workoutToSortScore.put(s, cWorkoutStats);

                } catch (JSONException e) {
                    workoutToSortScore.put(s, cWorkoutStats);
                }
            } else {
                workoutToSortScore.put(s, cWorkoutStats);
            }
        }
        workoutNames.sort(Comparator.comparingInt(s -> Objects.requireNonNull(workoutToSortScore.get(s)).count));
        for (String s : workoutNames) {
            WorkoutStats cWorkoutStats = workoutToSortScore.get(s);
            assert cWorkoutStats != null;
            cWorkoutStats.posInSortedCounts = workoutNames.indexOf(s);
        }
        workoutNames.sort(Comparator.comparing(s -> Objects.requireNonNull(workoutToSortScore.get(s)).lastCompletedDate));
        for (String s : workoutNames) {
            WorkoutStats cWorkoutStats = workoutToSortScore.get(s);
            assert cWorkoutStats != null;
            cWorkoutStats.posInSortedDates = workoutNames.indexOf(s);
        }

        Log.d("MainActivity", "sort scores for workout: " + workoutToSortScore);
        workoutNames.sort((s1, s2) -> Objects.requireNonNull(workoutToSortScore.get(s1)).compareTo(Objects.requireNonNull(workoutToSortScore.get(s2))));
        for (String s : workoutNames) {
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
    }

    public void startWorkout(String workoutName) {
        CurrentWorkout.init(workoutName, this);
        startWorkout();
    }

    public void goToAddExercise(View view) {
        assert view.getId() == R.id.go_to_add_exercise_button;
        Intent intent = new Intent(this, AddExerciseActivity.class);
        startActivity(intent);
    }

    public void goToSettings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startWorkout() {
        try {
            JSONObject appStatus = new JSONObject(Objects.requireNonNull(Util.readFromInternal("app_status.json", this)));
            appStatus.put("workout_is_in_progress", true);
            Util.writeFileOnInternalStorage(this, "app_status.json", appStatus.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startActivity(ActivityTransition.goToNextActivityInWorkout(this));
    }

    public void openEditWorkouts(View view) {
        assert view.getId() == R.id.go_to_edit_workouts_main_activity;
        Intent intent = new Intent(this, WorkoutEditActivity.class);
        startActivity(intent);
    }

    public void goToManageExercises(View view) {
        assert view.getId() == R.id.button_go_to_manage_exercises;
        Intent intent = new Intent(this, ManageExercisesActivity.class);
        startActivity(intent);
    }

    public void goToWeight2(View view) {
        assert view.getId() == R.id.button_to_weight2;
        Intent intent = new Intent(this, WeightActivity2.class);
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