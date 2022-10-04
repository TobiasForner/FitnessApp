package com.example.fitnessapp3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.fitnessapp3.SetResults.SetResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CurrentWorkout {
    public static String[] lastWorkout;
    public static String workoutName;
    public static boolean useLastWorkout;
    public static String[] currentWorkout;
    public static List<String> setStrings;
    protected static List<Integer> numberOfExercise;
    protected static Map<String, ArrayList<SetResult>> exToResults;
    private static Workout workout;

    private static int currentWorkoutEnqueuePos;

    public static void finishWorkout(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        Set<String> lastWorkoutDef = new HashSet<>();
        Set<String> workoutResults = Objects.requireNonNull(sharedPreferences.getStringSet(workoutName + "_results", lastWorkoutDef));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd.hh-mm:", Locale.getDefault());
        Date today = Calendar.getInstance().getTime();
        String date = dateFormat.format(today);
        workoutResults.add(date + String.join(";", currentWorkout));
        String lastResults = String.join(";", currentWorkout);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(workoutName + "last_result", lastResults);
        editor.putStringSet(workoutName + "_results", workoutResults);
        editor.apply();

        disableWorkoutInProgress(activity);
        Util.writeFileOnInternalStorage(activity, workoutName + "last_result.txt", lastResults);
    }

    private static void disableWorkoutInProgress(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("workout_is_in_progress", false);
        editor.putString("workout_in_progress", "");
        editor.apply();
    }

    public static void init(String workoutName, Activity activity) {
        CurrentWorkout.workoutName = workoutName;
        workout = WorkoutManager.getWorkoutFromFile(workoutName, activity);
        if (workout == null) {
            handleWorkoutDoesNotExist();
            return;
        }
        int workoutLength = workout.getLength();
        initFields(workoutLength);

        StringOccurrenceCounter counter = new StringOccurrenceCounter();

        numberOfExercise = workout.getCompNamesStream().mapToInt(counter).boxed().collect(Collectors.toList());

        Map<String, Integer> exCounts = counter.getCountMap();
        setStrings = IntStream.range(0, numberOfExercise.size()).mapToObj(i -> formatSetString(i, exCounts)).collect(Collectors.toList());

        exToResults = exCounts.entrySet().stream().filter(e -> !e.getKey().equals("Rest"))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>()));
        for (Map.Entry<String, Integer> pair : exCounts.entrySet()) {
            if (!pair.getKey().equals("Rest")) {
                for (int i = 0; i < pair.getValue(); i++) {
                    Objects.requireNonNull(exToResults.get(pair.getKey())).add(new SetResult(0, 0));
                }
            }

        }
        tryInitLastWorkout(activity, workoutLength);
        saveProgress(activity);
    }

    private static String formatSetString(int workoutPos, Map<String, Integer> exCounts) {
        int setNum = numberOfExercise.get(workoutPos) + 1;
        String compName = workout.getComponentAt(workoutPos).getName();
        Object finalSetNum = exCounts.get(compName);
        int finalSetNumber;
        if (finalSetNum == null) {
            finalSetNumber = -1;
        } else {
            finalSetNumber = (int) finalSetNum;
        }
        return setNum + "/" + finalSetNumber;
    }

    private static void tryInitLastWorkout(Activity activity, int workoutLength) {
        useLastWorkout = false;
        String lastWorkoutString = Util.readFromInternal(workoutName + "last_result.txt", activity);
        if (lastWorkoutString == null) {
            return;
        }
        if (lastWorkoutString.length() > 0) {
            lastWorkout = lastWorkoutString.split(";");
            useLastWorkout = lastWorkout.length == workoutLength;
        }
    }

    private static void handleWorkoutDoesNotExist() {
        //TODO handle sensibly
    }

    private static void initFields(int workoutLength) {
        currentWorkout = new String[workoutLength];
        setStrings = new ArrayList<>(workoutLength);
        numberOfExercise = new ArrayList<>(workoutLength);
    }

    public static boolean hasNextExercise() {
        return workout.hasNextExercise();
    }

    public static WorkoutComponent getNextWorkoutComponent() throws IllegalArgumentException {
        if (!hasNextExercise()) {
            throw new IllegalArgumentException("No next exercise!");
        }
        return workout.getCurrentComponent();
    }

    public static void goBack() {
        workout.goBack();
    }

    public static void logExercise(int exNum, int repNum, Activity activity) {
        currentWorkout[workout.getPosition()] = exNum + "," + repNum;
        String compName = workout.getCurrentComponent().getName();
        ArrayList<SetResult> setResults = exToResults.get(compName);
        assert setResults != null;
        SetResult currentSetResult = setResults.get(numberOfExercise.get(workout.getPosition()));
        currentSetResult.setRepNr(repNum);
        currentSetResult.setAddedWeight(exNum);
        workout.proceed();
        saveProgress(activity);
    }

    public static void logRest(int millis, Activity activity) {
        currentWorkout[workout.getPosition()] = "" + millis;
        workout.proceed();
        saveProgress(activity);
    }


    public static void logDuration(int duration, Activity activity) {
        currentWorkout[workout.getPosition()] = "" + duration;
        String compName = workout.getCurrentComponent().getName();
        ArrayList<SetResult> setResults = exToResults.get(compName);
        assert setResults != null;
        SetResult currentSetResult = setResults.get(numberOfExercise.get(workout.getPosition()));
        currentSetResult.setRepNr(duration);
        currentSetResult.setAddedWeight(0);
        currentSetResult.setIsDuration(true);
        workout.proceed();
        saveProgress(activity);
    }

    public static void logWeightedDuration(int duration, int weight, Activity activity) {
        currentWorkout[workout.getPosition()] = "" + duration + "," + weight + "," + true;
        String compName = workout.getCurrentComponent().getName();
        ArrayList<SetResult> setResults = exToResults.get(compName);
        assert setResults != null;
        SetResult currentSetResult = setResults.get(numberOfExercise.get(workout.getPosition()));
        currentSetResult.setRepNr(duration);
        currentSetResult.setAddedWeight(weight);
        currentSetResult.setIsDuration(true);
        workout.proceed();
        saveProgress(activity);
    }

    public static String getPrevResultsInWorkout() {
        String compName = workout.getCurrentComponent().getName();
        ArrayList<SetResult> prevResults = exToResults.getOrDefault(compName, null);
        if (prevResults == null || prevResults.get(0) == null) {
            return "";
        }
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < numberOfExercise.get(workout.getPosition()); i++) {

            res.append("Set ").append(i + 1).append(":\t");
            SetResult ithResult = prevResults.get(i);
            int weightNum = ithResult.getAddedWeight();
            if (weightNum > 0) {
                res.append("+").append(weightNum).append("kg x ");
            }
            if (ithResult.isDuration()) {
                res.append(ithResult.getRepNr()).append(" s");
            } else {
                res.append(ithResult.getRepNr()).append(" Reps");
            }

            res.append(System.getProperty("line.separator"));
        }
        return res.toString();
    }

    public static String getWorkoutComponentName() {
        return workout.getCurrentComponent().getName();
    }

    public static String[] getPrevResultsOfCurrentPosition() {
        return lastWorkout[workout.getPosition()].split(",");
    }

    public static String getSetString() {
        return setStrings.get(workout.getPosition());
    }

    private static void saveProgress(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("workout_is_in_progress", true);
        editor.apply();

        JSONObject workoutProgress = new JSONObject();
        try {
            workoutProgress.put("name", workoutName);

            JSONObject exerciseResults = new JSONObject();
            for (String ex : exToResults.keySet()) {
                ArrayList<SetResult> setResults = exToResults.get(ex);
                ArrayList<JSONObject> repr = new ArrayList<>();
                assert setResults != null;
                for (SetResult sr : setResults) {
                    repr.add(sr.toJSON());
                }

                exerciseResults.put(ex, new JSONArray(repr));
            }
            workoutProgress.put("exResults", exerciseResults);
            workoutProgress.put("position", workout.getPosition());

            workoutProgress.put("currentWorkout", new JSONArray(currentWorkout));
            Util.writeFileOnInternalStorage(activity, Util.WORKOUT_IN_PROGRESS_JSON, workoutProgress.toString());
        } catch (JSONException e) {
            Log.e("CurrentWorkout", "saveProgress: failed to store set results");
        }
    }

    public static String getWorkoutNameInProgress(Activity activity) {
        String jsonStr = Util.readFromInternal(Util.WORKOUT_IN_PROGRESS_JSON, activity);
        try {
            assert jsonStr != null;
            JSONObject workoutInProgress = new JSONObject(jsonStr);
            return (String) workoutInProgress.get("name");
        } catch (JSONException e) {
            Log.e("CurrentWorkout", "getWorkoutNameInProgress: could not retrieve JSON");
            return "";
        }
    }

    public static void restoreWorkoutInProgress(Activity activity) {
        if (workoutIsInProgress(activity)) {
            String contentsJSON = Util.readFromInternal(Util.WORKOUT_IN_PROGRESS_JSON, activity);
            if (contentsJSON != null) {
                Log.d("CurrentWorkout", "restoreWorkoutInProgress: Restoring from JSON");
                try {
                    JSONObject progress = new JSONObject(contentsJSON);
                    init((String) progress.get("name"), activity);
                    JSONArray wip = (JSONArray) progress.get("currentWorkout");
                    currentWorkoutEnqueuePos = 0;
                    for (int i = 0; i < wip.length(); i++) {
                        tryAddToWorkout(wip.getString(i));
                    }

                    int workoutPos = (int) progress.get("position");
                    Log.d("CurrentWorkout", "restoreWorkoutInProgress: restoring from position " + workoutPos);
                    while (workout.getPosition() < workoutPos) {
                        workout.proceed();
                    }
                    JSONObject exToRes = (JSONObject) progress.get("exResults");
                    for (Iterator<String> it = exToRes.keys(); it.hasNext(); ) {
                        String key = it.next();
                        JSONArray exResults = (JSONArray) exToRes.get(key);
                        ArrayList<SetResult> currResults = exToResults.get(key);
                        for (int i = 0; i < exResults.length(); i++) {
                            SetResult ithRes = SetResult.fromJSON((JSONObject) exResults.get(i));
                            assert currResults != null;
                            currResults.set(i, ithRes);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("CurrentWorkout", "restoreWorkoutInProgress: failed to load from JSON");
                }
            } else {
                Log.e("CurrentWorkout", "restoreWorkoutInProgress: failed to load from JSON; JSON content null");
            }
        }
    }

    private static boolean workoutIsInProgress(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPreferences.getBoolean("workout_is_in_progress", false);
    }

    private static void tryAddToWorkout(String resString) {
        if (!resString.equals("null")) {
            currentWorkout[currentWorkoutEnqueuePos] = resString;
        }
        currentWorkoutEnqueuePos++;
    }

    public static int getWorkoutLength() {
        return workout.getLength();
    }

    public static int getWorkoutPosition() {
        return workout.getPosition();
    }

    public static void setProgress(ProgressBar progressBar) {
        progressBar.setMin(0);
        progressBar.setMax(getWorkoutLength());
        progressBar.setIndeterminate(false);
        progressBar.setProgress(getWorkoutPosition() + 1);
    }

    public static void assureNotInProgress(String workoutName, Activity activity) {
        if (workoutIsInProgress(activity)) {
            String contentsJSON = Util.readFromInternal(Util.WORKOUT_IN_PROGRESS_JSON, activity);
            if (contentsJSON != null) {
                try {
                    JSONObject progress = new JSONObject(contentsJSON);
                    String inProgressName = (String) progress.get("name");
                    if (workoutName.equals(inProgressName)) {
                        disableWorkoutInProgress(activity);
                    }
                } catch (JSONException e) {
                    Log.e("CurrentWorkout", "assureNotInProgress: failed to check JSON");
                    disableWorkoutInProgress(activity);
                }
            }
        }
    }
}
