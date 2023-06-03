package com.example.fitnessapp3;

import android.app.Activity;
import android.content.Context;
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
import java.util.HashMap;
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
    public static String workoutName;
    public static boolean useLastWorkout;
    public static String[] currentWorkout;
    public static List<String> setStrings;

    //has an entry for each position that states the number of sets of this exercise completed so
    // far. The first occurrence of each exercise is marked with a zero
    protected static List<Integer> numberOfExercise;
    protected static Map<String, ArrayList<SetResult>> exToResults;

    protected static Map<String, ArrayList<SetResult>> lastWorkoutExToResults;
    private static Workout workout;

    private static int currentWorkoutEnqueuePos;

    private static final String WorkoutIsInProgressFieldName = "is_in_progress";


    public static void finishWorkout(Activity activity) {
        //TODO store as JSON
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

        setInProgress(false, activity);
        saveFinalResults(activity);
        Util.writeFileOnInternalStorage(activity, workoutName + "last_result.txt", lastResults);
    }

    public static void init(String workoutName, Activity activity) {
        CurrentWorkout.workoutName = workoutName;
        workout = WorkoutManager.getWorkout(workoutName, activity);
        if (workout.getLength() == 0) {
            handleWorkoutDoesNotExist();
            return;
        }
        int workoutLength = workout.getLength();
        initFields(workoutLength);

        StringOccurrenceCounter counter = new StringOccurrenceCounter();

        numberOfExercise = workout.getCompNamesStream().mapToInt(counter).boxed().collect(Collectors.toList());

        Map<String, Integer> exCounts = counter.getCountMap();
        setStrings = IntStream.range(0, numberOfExercise.size()).mapToObj(i -> formatSetString(i, exCounts)).collect(Collectors.toList());

        exToResults = exCounts.entrySet().stream().filter(e -> !e.getKey().equals("Rest")).collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>()));
        for (Map.Entry<String, Integer> pair : exCounts.entrySet()) {
            if (!pair.getKey().equals("Rest")) {
                for (int i = 0; i < pair.getValue(); i++) {
                    Objects.requireNonNull(exToResults.get(pair.getKey())).add(new SetResult(0, 0));
                }
            }

        }
        tryInitLastWorkoutJSON(activity);
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


    private static void tryInitLastWorkoutJSON(Activity activity) {
        String contentsJSON = Util.readFromInternal(workoutName + "last_result.json", activity);
        if (contentsJSON == null) {
            Log.d("CurrentWorkout", "contents of previous workout are null.");
            return;
        }
        try {
            JSONObject lastWorkout = new JSONObject(contentsJSON);
            JSONObject lastResults = lastWorkout.getJSONObject("exResults");
            lastWorkoutExToResults = new HashMap<>();
            for (String ex : exToResults.keySet()) {
                JSONArray repr = lastResults.getJSONArray(ex);
                ArrayList<SetResult> setResults = new ArrayList<>();
                for (int i = 0; i < repr.length(); i++) {
                    SetResult setResult = SetResult.fromJSON(repr.getJSONObject(i));
                    setResults.add(setResult);
                }
                lastWorkoutExToResults.put(ex, setResults);
            }
            useLastWorkout = true;

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static void saveFinalResults(Activity activity) {
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
            Util.writeFileOnInternalStorage(activity, workoutName + "last_result.json", workoutProgress.toString());
        } catch (JSONException e) {
            Log.e("CurrentWorkout", "saveFinalResults: failed to store set results");
            e.printStackTrace();
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

    public static boolean hasCurrentExercise() {
        return workout.hasNextExercise();
    }

    public static boolean hasNextExercise() {
        return workout.getPosition()<workout.getLength()-1;
    }

    public static WorkoutComponent getCurrentWorkoutComponent() throws IllegalArgumentException {
        if (!hasCurrentExercise()) {
            throw new IllegalArgumentException("No next exercise!");
        }
        return workout.getCurrentComponent();
    }

    public static WorkoutComponent getNextWorkoutComponent() throws IllegalArgumentException {
        if (hasNextExercise()) {
            return workout.getComponentAt(workout.getPosition()+1);
        }else{
            throw new IllegalArgumentException("No next exercise!");
        }

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

    public static SetResult getPrevSetResultsOfCurrentPosition() {
        String compName = workout.getCurrentComponent().getName();
        if (lastWorkoutExToResults.containsKey(compName)) {
            ArrayList<SetResult> setResults = lastWorkoutExToResults.get(compName);
            int setPos = numberOfExercise.get(workout.getPosition());
            assert setResults != null;
            int usePos = Math.min(setPos, setResults.size() - 1);
            return setResults.get(usePos);
        }
        return null;
    }

    public static SetResult getPrevSetResultsOfCurrentExercise() {
        String compName = workout.getCurrentComponent().getName();
        if (exToResults.containsKey(compName)) {
            ArrayList<SetResult> setResults = exToResults.get(compName);
            int setPos = numberOfExercise.get(workout.getPosition());
            assert setResults != null;
            if (setPos > 0) {
                return setResults.get(setPos - 1);
            }
        }
        return null;
    }

    public static String getSetString() {
        return setStrings.get(workout.getPosition());
    }

    private static void saveProgress(Activity activity) {
        setInProgress(true, activity);

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
                    setInProgress(true, activity);
                } catch (JSONException e) {
                    Log.e("CurrentWorkout", "restoreWorkoutInProgress: failed to load from JSON");
                }
            } else {
                Log.e("CurrentWorkout", "restoreWorkoutInProgress: failed to load from JSON; JSON content null");
            }
        }
    }

    public static boolean workoutIsInProgress(Context context) {
        String progressText = Util.readFromInternal(Util.WORKOUT_IS_IN_PROGRESS_JSON, context);
        try {
            assert progressText != null;
            JSONObject progress = new JSONObject(progressText);
            return progress.getBoolean(WorkoutIsInProgressFieldName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
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
                        setInProgress(false, activity);
                    }
                } catch (JSONException e) {
                    Log.e("CurrentWorkout", "assureNotInProgress: failed to check JSON");
                    setInProgress(false, activity);
                }
            }
        }
    }

    public static void setInProgress(boolean b, Context context) {
        JSONObject workout_in_progress = new JSONObject();
        try {
            workout_in_progress.put("is_in_progress", b);
            Util.writeFileOnInternalStorage(context, Util.WORKOUT_IS_IN_PROGRESS_JSON, workout_in_progress.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
