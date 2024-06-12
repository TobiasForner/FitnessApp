package com.example.fitnessapp3

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitnessapp3.ui.theme.FitnessApp3Theme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

class WeightActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val activity = this

        setContent {
            ActivityContent(activity = activity)
        }
    }
}

@Composable
fun ActivityContent(activity: WeightActivity2) {
    val pastWeights = remember {
        getSortedWeightDates(activity).toMutableStateList()
    }

    fun appendWeight(newWeightObj: JSONObject) {
        Log.d("WeightActivity2", "RELOAD")
        pastWeights.add(newWeightObj)
    }

    val xToDateMapKey = ExtraStore.Key<Map<Float, String>>()
    FitnessApp3Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column {
                WeightInput(
                    modifier = Modifier
                        .padding(innerPadding)
                        .height(100f.dp)
                        .padding(top = 10.dp, start = 10.dp),
                    activity = activity,
                    appendWeight = ::appendWeight
                )

                VicoChart(
                    Modifier
                        .padding(innerPadding)
                        .height(400.dp), weights = pastWeights, xToDateMapKey = xToDateMapKey
                )
                PastWeightText(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp), weights = pastWeights
                )
            }
        }
    }
}

@Composable
fun WeightInput(modifier: Modifier, activity: WeightActivity2, appendWeight: (JSONObject) -> Unit) {
    val pastWeights = getSortedWeightDates(activity)
    val lastWeight = pastWeights.last().getString("weight")
    var text by remember { mutableStateOf(lastWeight) }
    val weight = text.toFloat()

    val weightModifiers: MutableList<Pair<String, Float>> = mutableListOf()
    weightModifiers.add(Pair("Schwarze Jogginghose", 0.6f))
    weightModifiers.add(Pair("Kurze Schlafanzughose", 0.15f))
    weightModifiers.add(Pair("T-Shirt", 0.17f))
    weightModifiers.add(Pair("Fleece-Jacke", 0.5f))

    val activatedModifiers = remember {
        mutableStateListOf<Boolean>()
    }
    while (activatedModifiers.size < weightModifiers.size) {
        activatedModifiers.add(false)
    }

    val adjustment = roundToNDigits(activatedModifiers.toList().withIndex().map {
        if (it.value) {
            weightModifiers[it.index].second
        } else {
            0f
        }
    }.sum(), 1)

    val finalWeight = weight - adjustment

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        Row(modifier = modifier) {
            TextField(
                value = text,
                onValueChange = {
                    Log.d("WeightActivity2", "new weight: $it")
                    text = it
                },
                label = { Text(text = "Weight") },
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier.width(5.dp))
            Button(onClick = {
                logWeight(roundToNDigits(finalWeight, 1).toString(), appendWeight, activity)
                keyboardController?.hide()
                focusManager.clearFocus()
            }) {
                Text(text = "Log Weight")
            }
        }
        Text("${activatedModifiers.toList()} (-$adjustment): $finalWeight")
        LazyColumn {
            items(weightModifiers.size) { item ->
                WeightModifierCard(
                    modifier = Modifier.fillMaxWidth(),
                    text = weightModifiers[item].first,
                    activated = activatedModifiers[item],
                    onClick = { activatedModifiers[item] = activatedModifiers[item].not() })
            }
        }
    }
}

@Composable
fun WeightModifierCard(modifier: Modifier, text: String, activated: Boolean, onClick: () -> Unit) {
    val colors = if (activated) {
        CardColors(
            containerColor = androidx.compose.ui.graphics.Color.White,
            contentColor = androidx.compose.ui.graphics.Color.Black,
            disabledContentColor = androidx.compose.ui.graphics.Color.Black,
            disabledContainerColor = androidx.compose.ui.graphics.Color.Black
        )
    } else {
        CardColors(
            containerColor = androidx.compose.ui.graphics.Color.Black,
            contentColor = androidx.compose.ui.graphics.Color.White,
            disabledContentColor = androidx.compose.ui.graphics.Color.Black,
            disabledContainerColor = androidx.compose.ui.graphics.Color.Black
        )
    }
    ElevatedCard(modifier = modifier, onClick = onClick, colors = colors) {
        Text(text)
    }
}

fun roundToNDigits(num: Float, digits: Int): Float {
    val mult = 10f.pow(digits)
    return (num * mult).roundToInt().toFloat() / mult
}

fun logWeight(weight: String, appendWeight: (JSONObject) -> Unit, context: Context) {
    if (weight.isEmpty()) {
        Log.d("WeightActivity", "empty weight, skipping")
        return
    }

    val t = LocalDateTime.now()
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH-mm", Locale.getDefault())
    val date = t.format(dateTimeFormatter)

    val weightLog = WeightActivity.getPastWeights(context)

    try {
        val logs = weightLog.getJSONArray("logs")
        val newWeight = JSONObject()
        newWeight.put("date", date)
        newWeight.put("weight", weight)
        logs.put(newWeight)
        appendWeight(newWeight)
        weightLog.put("logs", logs)
        Util.writeFileOnInternalStorage(context, "weight_log.json", weightLog.toString())
        Log.d("WeightActivity", "Logged weight $weight on $date")
        updatePastWeight(context)
    } catch (e: JSONException) {
        e.printStackTrace()
    }
}

private fun updatePastWeight(context: Context) {
    val logList = getSortedWeightDates(context)
    val newText = java.lang.StringBuilder()
    for (entry in logList) {
        if (newText.isNotEmpty()) {
            newText.append("\n")
        }
        try {
            newText.append(entry.getString("date")).append(":  ")
                .append(String.format("%-5s", Util.strip(entry.getString("weight"))))
        } catch (e: JSONException) {
            throw java.lang.RuntimeException(e)
        }
    }
}

@Composable
fun VicoChart(
    modifier: Modifier,
    weights: List<JSONObject>,
    xToDateMapKey: ExtraStore.Key<Map<Float, String>>
) {

    val modelProducer = remember { CartesianChartModelProducer.build() }
    val weightData = weightData(weights)
    Log.d("Weight2", "Original Weight data: $weights")

    val data: MutableMap<String, Float> = HashMap()
    weightData.first.iterator().withIndex()
        .forEach { data[it.value] = weightData.second[it.index] }
    Log.d("Weight2", "Weight data: $data")

    val xToString = data.keys.associateBy { weightData.first.indexOf(it).toFloat() }
    Log.d("Weight2", "$xToString")


    val bottomFormatter = CartesianValueFormatter { x, chartValues, _ ->
        val es = chartValues.model.extraStore
        val x1 = es[xToDateMapKey]
        x1[x] ?: "ERROR"
    }
    LaunchedEffect(Unit) {
        modelProducer.tryRunTransaction {
            lineSeries { series(xToString.keys, data.values) }
            updateExtras { it[xToDateMapKey] = xToString }
        }
    }

    val label = TextComponent.build {
        minWidth =
            TextComponent.MinWidth { _, _, _, _, _ -> 150f }
        color = Color.WHITE
    }
    val maxY = weightData.second.max() + 1
    val minY = weightData.second.min() - 1
    val scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End)
    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                spacing = Dp(60f),
                axisValueOverrider = AxisValueOverrider.fixed(minY = minY, maxY = maxY)
            ),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                label = label,
                valueFormatter = bottomFormatter,
                labelRotationDegrees = 45f
            ),
        ),
        modelProducer,
        modifier = modifier,
        scrollState = scrollState
    )
}

@Composable
fun PastWeightText(modifier: Modifier, weights: List<JSONObject>) {
    val logList = weights.reversed()
    val newText = StringBuilder()
    for (entry in logList) {
        if (newText.isNotEmpty()) {
            newText.append("\n")
        }
        try {
            val weight = roundToNDigits(Util.strip(entry.getString("weight")).toFloat(), 1)
            newText.append(entry.getString("date")).append(":  ")
                .append(String.format("%-5s", weight.toString()))
        } catch (e: JSONException) {
            throw java.lang.RuntimeException(e)
        }
    }
    Text(text = newText.toString(), modifier = modifier.verticalScroll(rememberScrollState()))
}

fun weightData(weights: List<JSONObject?>): Pair<List<String>, List<Float>> {
    val sortedWeights = weights as MutableList
    sortedWeights.sortBy { it.toString() }

    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH-mm", Locale.getDefault())
    //aggregate weight averages
    val aggregatedWeights: MutableList<Float> = ArrayList()
    val consideredDates: MutableList<String> = ArrayList()
    try {
        val firstDateStr: String = sortedWeights[0]?.getString("date") ?: "UNKNOWN"
        val firstDate = LocalDateTime.parse(firstDateStr, dateTimeFormatter)
        var nextSunday = firstDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
        //jump to end of next sunday
        nextSunday = nextSunday.plusHours((23 - nextSunday.hour).toLong())
        nextSunday = nextSunday.plusMinutes((59 - nextSunday.minute).toLong())
        nextSunday = nextSunday.plusSeconds((59 - nextSunday.second).toLong())

        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

        var currentWeightSum: Float = sortedWeights[0]?.getString("weight")?.toFloat() ?: 0f
        var count = 1
        for (i in 1 until sortedWeights.size) {
            val currentEntry: JSONObject? = sortedWeights[i]
            val currentDate = LocalDateTime.parse(
                currentEntry?.getString("date") ?: "2024-01-01.00-00",
                dateTimeFormatter
            )
            if (currentEntry == null) {
                Log.e("WeightActivity2", "encountered null entry!")
            } else {
                Log.d(
                    "WeightActivity",
                    "considering dateTime " + currentEntry.getString("date") + "; current nextSunday is " + nextSunday.format(
                        dateTimeFormatter
                    )
                )


                if (nextSunday.isAfter(currentDate)) {
                    currentWeightSum += currentEntry.getString("weight").toFloat()
                    count += 1
                    Log.d(
                        "WeightActivity",
                        "next Sunday " + nextSunday.format(dateTimeFormatter) + " is after current"
                    )
                } else {
                    if (currentDate.dayOfWeek == DayOfWeek.SUNDAY) {
                        // the current date is after next sunday and on a Sunday
                        // This means that it must be the only entry for this week
                        consideredDates.add(nextSunday.format(dtf))
                        aggregatedWeights.add(currentWeightSum / count)
                        Log.d(
                            "WeightActivity2",
                            "next Sunday " + nextSunday.format(dateTimeFormatter) + " is not after current " + currentDate.format(
                                dateTimeFormatter
                            )
                        )

                        nextSunday = currentDate.plusHours((23 - currentDate.hour).toLong())
                        nextSunday = nextSunday.plusMinutes((59 - nextSunday.minute).toLong())
                        nextSunday = nextSunday.plusSeconds((59 - nextSunday.second).toLong())

                        currentWeightSum = currentEntry.getString("weight").toFloat()
                        count = 1
                    } else {
                        consideredDates.add(nextSunday.format(dtf))
                        aggregatedWeights.add(currentWeightSum / count)

                        currentWeightSum = currentEntry.getString("weight").toFloat()
                        count = 1

                        nextSunday = currentDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                        nextSunday = nextSunday.plusHours((23 - nextSunday.hour).toLong())
                        nextSunday = nextSunday.plusMinutes((59 - nextSunday.minute).toLong())
                        nextSunday = nextSunday.plusSeconds((59 - nextSunday.second).toLong())
                    }

                }
            }
        }
        consideredDates.add(nextSunday.format(dtf))
        aggregatedWeights.add(currentWeightSum / count)


    } catch (e: JSONException) {
        throw RuntimeException(e)
    }
    return Pair(consideredDates, aggregatedWeights)
}

fun getPastWeights(context: Context?): JSONObject {
    var weightLog: JSONObject
    if (Util.contextHasFile(context, "weight_log.json")) {
        Log.d("WeightActivity", "weight log file exists.")
        val contentsJSON = Util.readFromInternal("weight_log.json", context)
        try {
            assert(contentsJSON != null)
            weightLog = JSONObject(contentsJSON)
        } catch (e: JSONException) {
            e.printStackTrace()
            weightLog = JSONObject()
        }
    } else {
        Log.d("WeightActivity", "weight log file does not exist. Creating an empty one")
        weightLog = JSONObject()
        val emptyLogs = JSONArray()
        try {
            weightLog.put("logs", emptyLogs)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return weightLog
}

private fun getSortedWeightDates(context: Context): List<JSONObject> {
    val logList: MutableList<JSONObject> = java.util.ArrayList()
    val weightLog = getPastWeights(context)
    try {
        val logs = weightLog.getJSONArray("logs")

        for (i in 0 until logs.length()) {
            val entry = logs.getJSONObject(i)
            logList.add(entry)
        }
        logList.sortWith { o1: JSONObject, o2: JSONObject ->
            try {
                val date2 = o2.getString("date")
                val date1 = o1.getString("date")
                date1.compareTo(date2)

            } catch (e: JSONException) {
                throw java.lang.RuntimeException(e)
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return logList
}

