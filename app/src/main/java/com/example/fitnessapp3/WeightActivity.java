package com.example.fitnessapp3;

import android.content.Context;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeightActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);
        updatePastWeight();
    }

    public void logWeight(View view) {
        assert view.getId() == R.id.log_weight_button;
        EditText weightText = findViewById(R.id.editTextWeight);
        String weight = String.valueOf(weightText.getText());
        if (weight.length() == 0) {
            Log.d("WeightActivity", "empty weight, skipping");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.HH-mm", Locale.getDefault());
        Date today = Calendar.getInstance().getTime();
        String date = dateFormat.format(today);

        JSONObject weightLog = getPastWeights(this);

        try {
            JSONArray logs = weightLog.getJSONArray("logs");
            JSONObject newWeight = new JSONObject();
            newWeight.put("date", date);
            newWeight.put("weight", weight);
            logs.put(newWeight);
            weightLog.put("logs", logs);
            Util.writeFileOnInternalStorage(this, "weight_log.json", weightLog.toString());
            Log.d("WeightActivity", "Logged weight " + weight + " on " + date);
            updatePastWeight();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getPastWeights(Context context) {
        JSONObject weightLog;
        if (Util.contextHasFile(context, "weight_log.json")) {
            Log.d("WeightActivity", "weight log file exists.");
            String contentsJSON = Util.readFromInternal("weight_log.json", context);
            try {
                assert contentsJSON != null;
                weightLog = new JSONObject(contentsJSON);
            } catch (JSONException e) {
                e.printStackTrace();
                weightLog = new JSONObject();
            }
        } else {
            Log.d("WeightActivity", "weight log file does not exist. Creating an empty one");
            weightLog = new JSONObject();
            JSONArray emptyLogs = new JSONArray();
            try {
                weightLog.put("logs", emptyLogs);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return weightLog;
    }

    private void updatePastWeight() {
        JSONObject weightLog = getPastWeights(this);
        try {
            JSONArray logs = weightLog.getJSONArray("logs");
            StringBuilder newText = new StringBuilder();
            for (int i = 0; i < logs.length(); i++) {
                if (i > 0) {
                    newText.append("\n");
                }
                JSONObject entry = logs.getJSONObject(i);
                newText.append(entry.getString("date"));
                newText.append(":  ");
                newText.append(entry.getString("weight"));

            }
            TextView pastWeightView = findViewById(R.id.pastWeightTextView);
            pastWeightView.setText(newText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}