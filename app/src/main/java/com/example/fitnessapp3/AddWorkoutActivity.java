package com.example.fitnessapp3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddWorkoutActivity extends AppCompatActivity implements PositiveNegativeDialogFragment.NoticeDialogListener {
    public static final String EXNAME = "com.example.fitnessapp3.EXNAME";
    public static final String EDIT = "com.example.fitnessapp3.EDIT";
    public static final String WORKOUT_NAME = "com.example.fitnessapp3.WORKOUT_NAME";
    // [bla,jskfjsdk,ddfgdg]  x5
    private final Pattern workoutGroupP = Pattern.compile("\\[(.*)]\\s*x?\\s*(\\d+)?$");
    private boolean edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);
        Intent intent = getIntent();
        edit = intent.getBooleanExtra(EDIT, false);

        //hide keyboard if edit text not focussed
        EditText editText = findViewById(R.id.editText_workout_body);
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
        if (edit) {
            switchToEdit(intent.getStringExtra(WORKOUT_NAME));
        }
    }

    public void addWorkout(View view) {
        assert view.getId()==R.id.add_edit_workout_button;
        TextView workoutName = findViewById(R.id.text_workout_name);
        if (workoutName.getText().equals("")) {
            return;
        }
        TextView workoutText = findViewById(R.id.editText_workout_body);
        if (workoutText.getText().equals("")) {
            return;
        }
        if (!edit && WorkoutManager.workoutExists(workoutName.getText().toString())) {
            openDialog(workoutName.getText().toString(), 0);
            return;
        }
        JSONObject workout = parseWorkout(workoutName.getText().toString(), workoutText.getText().toString());

        if (workout != null) {
            WorkoutManager.addWorkoutJSON(workout, this);
        }
    }

    private JSONObject parseWorkout(String name, String text) {
        JSONObject result = new JSONObject();
        try {
            result.put("name", name);
            String[] lines = text.split(Objects.requireNonNull(System.getProperty("line.separator")));
            List<JSONObject> componentGroups = new ArrayList<>();
            for (String line : lines) {
                JSONObject group = parseWorkoutLine(line);
                if (group == null) {
                    return null;
                } else {
                    componentGroups.add(group);
                }
            }
            result.put("componentGroups", new JSONArray(componentGroups));
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject parseWorkoutLine(String line) {
        line = Util.strip(line);
        if (line.equals("")) {
            return null;
        }
        Matcher m = workoutGroupP.matcher(line);
        if (m.matches()) {
            String g = m.group(1);
            assert g != null;
            String[] parts = g.split(",");
            int times = 1;
            if (m.groupCount() == 2) {
                String timesStr = m.group(2);
                try {
                    assert timesStr != null;
                    times = Integer.parseInt(timesStr);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }
            return parseGroup(parts, times);

        } else {
            String[] parts = line.split(",");
            return parseGroup(parts, 1);
        }
    }

    private JSONObject parseGroup(String[] parts, int repetitions) {
        List<String> groupComponents = new ArrayList<>();
        for (String exName : parts) {
            String strippedName = Util.strip(exName);
            groupComponents.add(strippedName);
        }
        try {
            JSONObject group = new JSONObject();
            group.put("components", new JSONArray(groupComponents));
            group.put("repetitions", repetitions);
            if (validExerciseNames(parts)) {
                return group;
            } else {
                return null;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean validExerciseNames(String[] exNames) {
        for (String part : exNames) {
            String tmp = Util.strip(part);
            if (!WorkoutManager.exerciseExists(tmp)) {
                openDialog(tmp, 1);
                return false;
            }
        }
        return true;
    }


    public void goToAddExercise(String exName) {
        Intent intent = new Intent(this, AddExerciseActivity.class);
        intent.putExtra(EXNAME, exName);
        startActivity(intent);
    }

    private void switchToEdit(String workoutName) {
        edit = true;
        TextView title = findViewById(R.id.text_add_title);
        title.setText(R.string.edit_workout);
        TextView workoutNameText = findViewById(R.id.text_workout_name);
        workoutNameText.setText(workoutName);
        TextView workoutText = findViewById(R.id.editText_workout_body);
        String workoutBody = WorkoutManager.getWorkoutText(workoutName, this);
        workoutText.setText(workoutBody);

        Button add_edit = findViewById(R.id.add_edit_workout_button);
        add_edit.setText(R.string.edit);
    }

    public void onDialogPositiveClick(DialogFragment dialog) {
        PositiveNegativeDialogFragment fragment = (PositiveNegativeDialogFragment) dialog;
        if (fragment.getVersion() == 0) {
            switchToEdit(fragment.getMessageExtra());
        } else {
            goToAddExercise(fragment.getMessageExtra());
        }
    }

    public void onDialogNegativeClick(DialogFragment dialog) {
        PositiveNegativeDialogFragment fragment = (PositiveNegativeDialogFragment) dialog;
        if (fragment.getVersion() == 0) {
            //Overwrite
            TextView workoutName = findViewById(R.id.text_workout_name);
            TextView workoutText = findViewById(R.id.editText_workout_body);
            JSONObject workout = parseWorkout(workoutName.getText().toString(), workoutText.getText().toString());

            if (workout != null) {
                WorkoutManager.addWorkoutJSON(workout, this);
                CurrentWorkout.assureNotInProgress(workoutName.getText().toString(), this);

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                showPopupWindowClick(workoutName, getString(R.string.workout_invalid));
            }
        }
        //do nothing if version is 1 or something else
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

    private void openDialog(String messageExtra, int dialogVersion) {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog;
        if (dialogVersion == 0) {
            dialog = new PositiveNegativeDialogFragment(R.string.workout_name_already_exists, R.string.yes, R.string.no_edit, messageExtra, dialogVersion);
        } else {
            dialog = new PositiveNegativeDialogFragment(R.string.exercise_does_not_exists, R.string.yes, R.string.cancel, messageExtra, dialogVersion);
        }
        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}