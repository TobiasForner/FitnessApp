package com.example.fitnessapp3;

import static com.androidplot.xy.StepMode.INCREMENT_BY_VAL;

import android.content.Context;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
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
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class WeightActivity extends AppCompatActivity {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH-mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);
        updatePastWeight();

        updatePlot();
    }

    private void updatePlot() {
        XYPlot plot = findViewById(R.id.plot);

        List<JSONObject> weights = getSortedWeightDates();
        Collections.reverse(weights);

        //aggregate weight averages
        List<Float> aggregatedWeights = new ArrayList<>();
        List<String> consideredDates = new ArrayList<>();
        try {
            String firstDateStr = weights.get(0).getString("date");
            LocalDateTime firstDate = LocalDateTime.parse(firstDateStr, dateTimeFormatter);
            LocalDateTime nextSunday = firstDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            nextSunday = nextSunday.plusHours(23 - nextSunday.getHour());
            nextSunday = nextSunday.plusMinutes(59 - nextSunday.getMinute());
            nextSunday = nextSunday.plusSeconds(59 - nextSunday.getSecond());

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());

            float currentWeightSum = Float.parseFloat(weights.get(0).getString("weight"));
            int count = 1;
            for (int i = 1; i < weights.size(); i++) {
                JSONObject currentEntry = weights.get(i);
                LocalDateTime currentDate = LocalDateTime.parse(currentEntry.getString("date"), dateTimeFormatter);
                Log.d("WeightActivity", "considering dateTime " + currentEntry.getString("date") + "; current nextSunday is " + nextSunday.format(dateTimeFormatter));
                if (nextSunday.isAfter(currentDate)) {
                    currentWeightSum += Float.parseFloat(currentEntry.getString("weight"));
                    count += 1;
                    Log.d("WeightActivity", "next Sunday " + nextSunday.format(dateTimeFormatter) + " is after current");
                } else {
                    if (currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        // the current date is after next sunday and on a Sunday
                        // This means that it must be the only entry for this week
                        consideredDates.add(nextSunday.format(dtf));
                        aggregatedWeights.add(currentWeightSum / count);

                        nextSunday = currentDate.plusHours(23 - currentDate.getHour());
                        nextSunday = nextSunday.plusMinutes(59 - nextSunday.getMinute());
                        nextSunday = nextSunday.plusSeconds(59 - nextSunday.getSecond());

                        currentWeightSum = Float.parseFloat(currentEntry.getString("weight"));
                        count = 1;
                    } else {
                        consideredDates.add(nextSunday.format(dtf));
                        aggregatedWeights.add(currentWeightSum / count);

                        currentWeightSum = Float.parseFloat(currentEntry.getString("weight"));
                        count = 1;

                        nextSunday = currentDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
                        nextSunday = nextSunday.plusHours(23 - nextSunday.getHour());
                        nextSunday = nextSunday.plusMinutes(59 - nextSunday.getMinute());
                        nextSunday = nextSunday.plusSeconds(59 - nextSunday.getSecond());
                    }
                    Log.d("WeightActivity", "next Sunday is not after current, starting new week ending at " + nextSunday.format(dateTimeFormatter));
                }
            }
            consideredDates.add(nextSunday.format(dtf));
            aggregatedWeights.add(currentWeightSum / count);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        /*List<Float> weightNumbers = weights.stream().map(x -> {
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
        }).collect(Collectors.toList());*/


        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries weightSeries = new SimpleXYSeries(
                aggregatedWeights, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Weight");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);
        series1Format.setPointLabeler((xySeries, i) -> String.format(Locale.getDefault(), "%.01f", xySeries.getY(i).floatValue()));


        plot.addSeries(weightSeries, series1Format);
        plot.getGraph().setMarginBottom(200);
        plot.getGraph().setMarginTop(50);
        plot.getGraph().setPaddingTop(50);
        plot.setDomainStep(INCREMENT_BY_VAL, 1);
        plot.setRangeBoundaries(aggregatedWeights.stream().min(Float::compareTo).orElse(70.0f) - 0.5, aggregatedWeights.stream().max(Float::compareTo).orElse(82.f) + 0.5, BoundaryMode.FIXED);
        //Log.d("WeightActivity", "" + plot.getGraph().range);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                float posF = ((Number) obj).floatValue();
                int i = Math.round(posF);
                return toAppendTo.append(consideredDates.get(i));
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return consideredDates.indexOf(source);
            }
        });
        plot.redraw();
    }

    public void logWeight(View view) {
        assert view.getId() == R.id.log_weight_button;
        EditText weightText = findViewById(R.id.editTextWeight);
        String weight = String.valueOf(weightText.getText());
        if (weight.length() == 0) {
            Log.d("WeightActivity", "empty weight, skipping");
            return;
        }

        LocalDateTime t = LocalDateTime.now();
        String date = t.format(dateTimeFormatter);

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
        updatePlot();
        View focusView = this.getCurrentFocus();
        if (focusView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
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
                newText.append(entry.getString("date")).append(":  ").append(String.format("%-5s", Util.strip(entry.getString("weight"))));
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