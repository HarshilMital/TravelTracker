package com.example.traveltracker

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.traveltracker.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var currentStopText: TextView
    private lateinit var switchUnitButton: Button
    private lateinit var nextStopButton: Button
    private lateinit var resetButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var binding: ActivityMainBinding

    private var stops = mutableListOf<String>()
    private var distances = mutableListOf<Double>()
    private var visaRequirements = mutableListOf<String>()

    private var currentIndex = 0
    private var distanceUnit = "KM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentStopText = findViewById(R.id.currentStopText)
        switchUnitButton = findViewById(R.id.switchUnitButton)
        nextStopButton = findViewById(R.id.nextStopButton)
        resetButton = findViewById(R.id.resetButton)
        progressBar = findViewById(R.id.progressBar)

        readDataFromFile()
        setupUI()
    }

    private fun setupUI() {
        updateStopInfo()


        progressBar.max = distances.sum().toInt()

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = StopAdapter(getVisibleStops())

        // Switch between kilometers and miles
        switchUnitButton.setOnClickListener {
            distanceUnit = if (distanceUnit == "KM") "Miles" else "KM"
            updateStopInfo()
        }

        // Move to the next stop
        nextStopButton.setOnClickListener {
            if (currentIndex < stops.size - 1) {
                currentIndex++
                updateStopInfo()
            } else {
                Toast.makeText(this, "Journey Complete!", Toast.LENGTH_SHORT).show()
            }
        }

        // Reset journey to the first stop
        resetButton.setOnClickListener {
            currentIndex = 0
            distanceUnit = "KM"
            updateStopInfo()
            Toast.makeText(this, "Journey Reset!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStopInfo() {
        val distanceCovered = distances.take(currentIndex).sum()
        val distanceLeft = distances.drop(currentIndex).sum()

        val convertedDistanceCovered = if (distanceUnit == "KM") distanceCovered else distanceCovered * 0.621371
        val convertedDistanceLeft = if (distanceUnit == "KM") distanceLeft else distanceLeft * 0.621371

        val unitLabel = if (distanceUnit == "KM") "KM" else "Miles"

        currentStopText.text = buildString {
            append("Current Stop: ${stops[currentIndex]}\n")
            append("Distance Covered: %.2f $unitLabel\n".format(convertedDistanceCovered))
            append("Distance Left: %.2f $unitLabel".format(convertedDistanceLeft))
        }

        // Update the visa requirement text
        val visaRequirementText: TextView = findViewById(R.id.visaRequirementText)
        "Visa Requirement: ${visaRequirements[currentIndex]}".also { visaRequirementText.text = it }

        progressBar.progress = distanceCovered.toInt()

        // Refresh RecyclerView
        binding.recyclerView.adapter = StopAdapter(getVisibleStops())
    }

    private fun getVisibleStops(): List<String> {
        return when {
            stops.size <= 3 -> stops
            else -> {
                val visibleStops = mutableListOf<String>()
                if (currentIndex > 0) visibleStops.add(stops[currentIndex - 1]) // Previous stop
                visibleStops.add(stops[currentIndex])
                if (currentIndex < stops.size - 1) visibleStops.add(stops[currentIndex + 1]) // Next stop
                visibleStops
            }
        }
    }

    private fun readDataFromFile() {
        val inputStream = resources.openRawResource(R.raw.stops)
        inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(",")
                if (parts.size == 3) {
                    stops.add(parts[0].trim())
                    distances.add(parts[1].trim().toDouble())
                    visaRequirements.add(parts[2].trim())
                }
            }
        }
    }
}