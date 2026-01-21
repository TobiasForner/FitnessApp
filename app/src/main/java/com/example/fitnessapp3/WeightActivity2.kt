package com.example.fitnessapp3

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fitnessapp3.Util.readFromInternal
import com.example.fitnessapp3.Util.writeFileOnInternalStorage
import com.example.fitnessapp3.ui.theme.FitnessApp3Theme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
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
        val wm = savedInstanceState?.getBooleanArray("weightModifiers")?.toMutableList()



        setContent {
            ActivityContent(activity = activity, activatedModifiers = wm)
        }
    }
}

@Composable
fun ActivityContent(activity: WeightActivity2, activatedModifiers: MutableList<Boolean>?) {
    val pastWeights = remember {
        getSortedWeightDates(activity).toMutableStateList()
    }

    fun appendWeight(newWeightObj: JSONObject) {
        Log.d("WeightActivity2", "RELOAD")
        pastWeights.add(newWeightObj)
    }

    val xToDateMapKey = ExtraStore.Key<Map<Double, String>>()
    FitnessApp3Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column {
                WeightInput(
                    modifier = Modifier
                        .padding(innerPadding)
                        .height(120f.dp)
                        .padding(top = 10.dp, start = 10.dp),
                    activity = activity,
                    appendWeight = ::appendWeight,
                    activatedModifiers=activatedModifiers
                )

                VicoChart(
                    Modifier
                        .padding(innerPadding)
                        .height(250.dp), weights = pastWeights, xToDateMapKey = xToDateMapKey
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
fun WeightInput(modifier: Modifier, activity: WeightActivity2, appendWeight: (JSONObject) -> Unit, activatedModifiers: MutableList<Boolean>?) {
    val pastWeights = getSortedWeightDates(activity)

    val lastWeight =
        if (pastWeights.last().has("original_weight")) {
            pastWeights.last().getString("original_weight")
        } else {
            pastWeights.last().getString("weight")
        }

    var text by remember { mutableStateOf(lastWeight) }
    val weight = try {
        text.toFloat()
    } catch (_: NumberFormatException) {
        0f
    }


    val weightModifiers = loadWeightModifiers(activity).toMutableList()


    val boolStateListSaver= Saver<SnapshotStateList<Boolean>, String>(save={
        var res = ""
        for (e in it){
            res += if(e){
                "1"
            }else{
                "0"
            }
        }
        res

    }, restore = {
        val res = mutableStateListOf<Boolean>()
        for (e in it.chars()){
            if (e == '0'.code){res.add(false)}else{res.add(true)}

        }
        res

    })
    val activatedModifiers = rememberSaveable(saver=boolStateListSaver) {
        if (activatedModifiers.isNullOrEmpty()) mutableStateListOf() else activatedModifiers.toMutableStateList()

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

    val openModifierDialog = remember {
        mutableStateOf(false)
    }

    val openModifierEditDialog = remember {
        mutableStateOf(false)
    }

    val editPosition = remember {
        mutableIntStateOf(1)
    }
    Column(modifier = Modifier.padding(top = 30.dp)) {
        if (openModifierDialog.value) {
            AddWeightModifierDialog(openModifierDialog, weightModifiers, context = activity)
        } else if (openModifierEditDialog.value) {
            EditWeightModifierDialog(
                openModifierEditDialog,
                weightModifiers,
                position = editPosition.intValue,
                context = activity
            )
        }

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
                logWeight(
                    weight,
                    adjustment,
                    appendWeight,
                    activity
                )
                keyboardController?.hide()
                focusManager.clearFocus()
            }) {
                Text(text = "Log Weight")
            }
        }
        Text("${activatedModifiers.toList()} (-$adjustment): $finalWeight")
        LazyColumn(modifier = Modifier.height(200.dp)) {
            items(weightModifiers.size) { item ->
                WeightModifierCard(
                    modifier = Modifier.fillMaxWidth(),
                    text = "${weightModifiers[item].first}: ${weightModifiers[item].second}",
                    activated = activatedModifiers[item],
                    onClick = { activatedModifiers[item] = activatedModifiers[item].not() },
                    onLongClick = {
                        Log.d("Weight2", "long click for position $item")
                        editPosition.intValue = item
                        openModifierEditDialog.value = true
                    })
            }
        }
        Button(
            modifier = Modifier.padding(top = 10.dp),
            onClick = { openModifierDialog.value = true }) {
            Text("Add Modifier!")
        }
    }
}

fun loadWeightModifiers(context: Context): List<Pair<String, Float>> {
    val weightModifiers = readFromInternal("weight_modifiers.json", context)
    val res = emptyList<Pair<String, Float>>().toMutableList()
    if (weightModifiers == null) {
        return res
    }
    val wm = JSONObject(weightModifiers)

    for (name in wm.keys()) {
        val value = wm.getString(name).toFloat()
        res.add(Pair(name, value))
    }
    return res
}

fun storeWeightModifiers(context: Context, weightModifiers: List<Pair<String, Float>>) {
    val obj = JSONObject()
    for (p in weightModifiers) {
        obj.put(p.first, p.second.toString())
    }
    writeFileOnInternalStorage(context, "weight_modifiers.json", obj.toString())
}

@Composable
fun EditWeightModifierDialog(
    openModifierDialog: MutableState<Boolean>,
    weightModifiers: MutableList<Pair<String, Float>>,
    position: Int,
    context: Context
) {
    val pair = weightModifiers[position]
    var name = pair.first
    var weight = pair.second
    val focusManager = LocalFocusManager.current
    Dialog(onDismissRequest = { openModifierDialog.value = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(5.dp),
            shape = RoundedCornerShape(5.dp),
        ) {
            Column {
                Button(onClick = {
                    weightModifiers.removeAt(position)
                    storeWeightModifiers(context, weightModifiers)
                    openModifierDialog.value = false
                }) {
                    Text(text = "Delete Modifier")
                }

                TextField(
                    modifier = Modifier.wrapContentSize(),
                    value = name,
                    onValueChange = {
                        Log.d("WeightActivity2", "new name: $it")
                        name = it
                    },
                    label = { Text(text = "Modifier Name") },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                TextField(
                    modifier = Modifier.wrapContentSize(),
                    value = weight.toString(),
                    onValueChange = {
                        Log.d("WeightActivity2", "edited weight: $it")
                        weight = it.toFloat()
                    },
                    label = { Text(text = "Weight deduction") },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    Button(onClick = { openModifierDialog.value = false }) {
                        Text(text = "Dismiss")
                    }
                    Button(onClick = {
                        weightModifiers[position] = Pair(name, weight)
                        storeWeightModifiers(context, weightModifiers)
                        openModifierDialog.value = false
                    }) {
                        Text(text = "Overwrite")
                    }
                }
            }
        }
    }
}

@Composable
fun AddWeightModifierDialog(
    openModifierDialog: MutableState<Boolean>,
    weightModifiers: MutableList<Pair<String, Float>>,
    context: Context
) {
    var name by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Dialog(onDismissRequest = { openModifierDialog.value = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(unbounded = true)
                .padding(5.dp),
            shape = RoundedCornerShape(5.dp),
        ) {
            Column {

                TextField(
                    modifier = Modifier.height(70.dp),
                    value = name,
                    onValueChange = {
                        Log.d("WeightActivity2", "new weight: $it")
                        name = it
                    },
                    label = { Text(text = "Modifier Name") },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                TextField(
                    modifier = Modifier.height(70.dp),
                    value = weight,
                    onValueChange = {
                        Log.d("WeightActivity2", "new weight: $it")
                        weight = it
                    },
                    label = { Text(text = "Weight deduction") },
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    Button(onClick = { openModifierDialog.value = false }) {
                        Text(text = "Dismiss")
                    }
                    Button(onClick = {
                        val modValue = weight.toFloat()
                        weightModifiers.add(Pair(name, modValue))
                        storeWeightModifiers(context, weightModifiers)
                        openModifierDialog.value = false
                    }) {
                        Text(text = "Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeightModifierCard(
    modifier: Modifier,
    text: String,
    activated: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
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
    ElevatedCard(
        modifier = modifier
            .combinedClickable(
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(5.dp), colors = colors
    ) {
        Text(text)
    }
}

fun roundToNDigits(num: Float, digits: Int): Float {
    val multiplier = 10f.pow(digits)
    return (num * multiplier).roundToInt().toFloat() / multiplier
}

fun logWeight(
    originalWeight: Float,
    weightModifier: Float,
    appendWeight: (JSONObject) -> Unit,
    context: Context
) {
    val t = LocalDateTime.now()
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH-mm", Locale.getDefault())
    val date = t.format(dateTimeFormatter)

    val weightLog = WeightActivity.getPastWeights(context)

    try {
        val logs = weightLog.getJSONArray("logs")
        val newWeight = JSONObject()
        newWeight.put("date", date)
        newWeight.put("original_weight", originalWeight.toString())
        val weight = originalWeight - weightModifier
        newWeight.put("weight", weight.toString())
        newWeight.put("weight_modifier", weightModifier.toString())
        logs.put(newWeight)
        appendWeight(newWeight)
        weightLog.put("logs", logs)
        writeFileOnInternalStorage(context, "weight_log.json", weightLog.toString())
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
    xToDateMapKey: ExtraStore.Key<Map<Double, String>>
) {

    val modelProducer = remember { CartesianChartModelProducer() }
    val weightData = weightData(weights)
    Log.d("Weight2", "Original Weight data: $weights")

    val data: MutableMap<String, Float> = HashMap()
    weightData.first.iterator().withIndex()
        .forEach { data[it.value] = weightData.second[it.index] }
    Log.d("Weight2", "Weight data: $data")

    val xToString = data.keys.associateBy { weightData.first.indexOf(it).toDouble() }
    Log.d("Weight2", "$xToString")


    val bottomFormatter = CartesianValueFormatter { context, x, _ ->
        val es = context.model.extraStore
        val x1 = es[xToDateMapKey]
        x1[x] ?: "ERROR"
        //context.model.extraStore[xToDateMapKey][x.toInt()] as CharSequence
    }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            lineSeries { series(xToString.keys, data.values) }
            extras { it[xToDateMapKey] = xToString }
        }
    }

    val label = TextComponent(
        color = Color.WHITE,
        minWidth =
            TextComponent.MinWidth { _, _, _, _, _ -> 150f }
    )
    val maxY = weightData.second.max() + 1.toDouble()
    val minY = weightData.second.min() - 1.toDouble()
    val scrollState = rememberVicoScrollState(initialScroll = Scroll.Absolute.End)
    CartesianChartHost(
        rememberCartesianChart(
            rememberLineCartesianLayer(
                pointSpacing = Dp(60f),
                rangeProvider = CartesianLayerRangeProvider.fixed(minY=minY, maxY = maxY),
                //axisValueOverrider = AxisValueOverrider.fixed(minY = minY, maxY = maxY)
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
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
        val contentsJSON = readFromInternal("weight_log.json", context)
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

