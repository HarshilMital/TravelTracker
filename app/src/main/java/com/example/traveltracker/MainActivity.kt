package com.example.traveltracker

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
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
    private lateinit var progressBar: ProgressBar
    private lateinit var stopsListView: ListView
    private lateinit var binding: ActivityMainBinding

    private var stops = mutableListOf<String>()
    private var distances = mutableListOf<Double>()
    private var visaRequirements = mutableListOf<String>()

    private var currentIndex = 0
    private var distanceUnit = "KM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentStopText = findViewById(R.id.currentStopText)
        switchUnitButton = findViewById(R.id.switchUnitButton)
        nextStopButton = findViewById(R.id.nextStopButton)
        progressBar = findViewById(R.id.progressBar)
        stopsListView = findViewById(R.id.listView)

        readDataFromFile()

        setupUI()

        if (stops.size <= 3) {
            // Traditional ListView
            stopsListView.visibility = View.VISIBLE
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stops)
            stopsListView.adapter = adapter
        } else {
            // Lazy List with RecyclerView
            binding.recyclerView.visibility = View.VISIBLE
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = StopAdapter(stops)
        }
    }

    private fun setupUI() {
        // Show current stop
        updateStopInfo()

        // Set progress bar max
        progressBar.max = distances.sum().toInt()

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