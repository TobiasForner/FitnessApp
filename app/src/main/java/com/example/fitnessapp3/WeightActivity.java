package com.example.fitnessapp3;

import android.content.Context;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WeightActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);
        updatePastWeight();

        XYPlot plot = (XYPlot) findViewById(R.id.plot);

        List<JSONObject> weights = getSortedWeightDates();
        Collections.reverse(weights);
        List<Float> weightNumbers = weights.stream().map(x -> {
            try {
                return Float.parseFloat(x.getString("weight"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        List<String> dateLabels = weights.stream().map(x -> {
            try {
                return x.getString("date");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries weightSeries = new SimpleXYSeries(
                weightNumbers, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Weight");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);


        // add a new series' to the xyplot:
        plot.addSeries(weightSeries, series1Format);
        plot.getGraph().setMarginBottom(200);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return toAppendTo.append(dateLabels.get(i));
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return dateLabels.indexOf(source);
            }
        });
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

    private List<JSONObject> getSortedWeightDates() {
        List<JSONObject> logList = new ArrayList<>();
        JSONObject weightLog = getPastWeights(this);
        try {
            JSONArray logs = weightLog.getJSONArray("logs");

            for (int i = 0; i < logs.length(); i++) {
                JSONObject entry = logs.getJSONObject(i);
                logList.add(entry);
            }
            logList.sort((o1, o2) -> {
                try {
                    return o2.getString("date").compareTo(o1.getString("date"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return logList;
    }

    private void updatePastWeight() {
        List<JSONObject> logList = getSortedWeightDates();
        StringBuilder newText = new StringBuilder();
        for (JSONObject entry : logList) {
            if (newText.length() > 0) {
                newText.append("\n");
            }
            try {
                newText.append(entry.getString("date")).append(":  ").append(entry.getString("weight"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        TextView pastWeightView = findViewById(R.id.pastWeightTextView);
        pastWeightView.setText(newText);

        TextView editTextWeight = findViewById(R.id.editTextWeight);
        try {
            editTextWeight.setText(logList.get(0).getString("weight"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}