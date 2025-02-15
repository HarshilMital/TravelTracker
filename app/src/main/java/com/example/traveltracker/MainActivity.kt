package com.example.traveltracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelTrackerApp(
                stops = readDataFromFile(),
                showToast = { message -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
            )
        }
    }

    private fun readDataFromFile(): List<Triple<String, Double, String>> {
        val inputStream = resources.openRawResource(R.raw.stops)
        val stopsList = mutableListOf<Triple<String, Double, String>>()
        BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 3) {
                    stopsList.add(Triple(parts[0].trim(), parts[1].trim().toDouble(), parts[2].trim()))
                }
            }
        }
        return stopsList
    }
}

@Composable
fun TravelTrackerApp(stops: List<Triple<String, Double, String>>, showToast: (String) -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    var distanceUnit by remember { mutableStateOf("KM") }

    val totalDistance = stops.sumOf { it.second }
    val distanceCovered = stops.take(currentIndex).sumOf { it.second }
    val distanceLeft = totalDistance - distanceCovered

    val convertedDistanceCovered = if (distanceUnit == "KM") distanceCovered else distanceCovered * 0.621371
    val convertedDistanceLeft = if (distanceUnit == "KM") distanceLeft else distanceLeft * 0.621371
    val unitLabel = if (distanceUnit == "KM") "KM" else "Miles"

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Current Stop: ${stops[currentIndex].first}", fontSize = 18.sp)
        Text("Distance Covered: %.2f $unitLabel".format(convertedDistanceCovered), fontSize = 16.sp)
        Text("Distance Left: %.2f $unitLabel".format(convertedDistanceLeft), fontSize = 16.sp)
        Text("Visa Requirement: ${stops[currentIndex].third}", fontSize = 16.sp)

        LinearProgressIndicator(
            progress = { (distanceCovered / totalDistance).toFloat() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        )

        StopList(stops, currentIndex)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { distanceUnit = if (distanceUnit == "KM") "Miles" else "KM" }) {
                Text("Switch Unit")
            }
            Button(onClick = {
                if (currentIndex < stops.size - 1) {
                    currentIndex++
                } else {
                    showToast("Journey Complete!")
                }
            }) {
                Text("Next Stop")
            }
        }

        Button(
            onClick = {
                currentIndex = 0
                distanceUnit = "KM"
                showToast("Journey Reset!")
            },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        ) {
            Text("Reset")
        }
    }
}

@Composable
fun StopList(stops: List<Triple<String, Double, String>>, currentIndex: Int) {
    val visibleStops = when {
        stops.size <= 3 -> stops
        else -> {
            val list = mutableListOf<Triple<String, Double, String>>()
            if (currentIndex > 0) list.add(stops[currentIndex - 1])
            list.add(stops[currentIndex])
            if (currentIndex < stops.size - 1) list.add(stops[currentIndex + 1])
            list
        }
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 16.dp)) {
        items(visibleStops) { stop ->
            Text(text = stop.first, fontSize = 16.sp, modifier = Modifier.padding(8.dp))
        }
    }
}
