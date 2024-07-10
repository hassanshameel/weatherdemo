package edu.cs.weatherdemo

import android.app.Activity
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import java.util.*

class MainActivity : Activity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var temperatureTextView: TextView
    private lateinit var pressureTextView: TextView
    private lateinit var lightTextView: TextView

    private var currentTemperature: Float = Float.NaN
    private var currentPressure: Float = Float.NaN
    private var currentLight: Float = Float.NaN

    private val tempSensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            currentTemperature = event.values[0]
        }
    }

    private val pressureSensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            currentPressure = event.values[0]
        }
    }

    private val lightSensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            currentLight = event.values[0]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temperatureTextView = findViewById(R.id.temperature)
        pressureTextView = findViewById(R.id.pressure)
        lightTextView = findViewById(R.id.light)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        val updateTimer = Timer("weatherUpdate")
        updateTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateGUI()
            }
        }, 0, 1000)
    }

    override fun onResume() {
        super.onResume()
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) {
            sensorManager.registerListener(lightSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            lightTextView.text = "Light Sensor Unavailable"
        }

        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        if (pressureSensor != null) {
            sensorManager.registerListener(pressureSensorEventListener, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            pressureTextView.text = "Barometer Unavailable"
        }

        val temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        if (temperatureSensor != null) {
            sensorManager.registerListener(tempSensorEventListener, temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            temperatureTextView.text = "Thermometer Unavailable"
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(pressureSensorEventListener)
        sensorManager.unregisterListener(tempSensorEventListener)
        sensorManager.unregisterListener(lightSensorEventListener)
    }

    private fun updateGUI() {
        runOnUiThread {
            if (!currentPressure.isNaN()) {
                pressureTextView.text = "$currentPressure (mBars)"
                pressureTextView.invalidate()
            }
            if (!currentLight.isNaN()) {
                val lightStr = when {
                    currentLight <= SensorManager.LIGHT_CLOUDY -> "Cloudy"
                    currentLight <= SensorManager.LIGHT_OVERCAST -> "Overcast"
                    currentLight <= SensorManager.LIGHT_SUNLIGHT -> "Sunny"
                    else -> "Night"
                }
                lightTextView.text = lightStr
                lightTextView.invalidate()
            }
            if (!currentTemperature.isNaN()) {
                temperatureTextView.text = "$currentTemperature °C"
                temperatureTextView.invalidate()
            }
        }
    }
}
