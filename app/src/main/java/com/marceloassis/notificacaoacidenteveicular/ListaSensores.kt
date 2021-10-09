package com.marceloassis.notificacaoacidenteveicular

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.marceloassis.notificacaoacidenteveicular.databinding.ActivityMainBinding
import com.marceloassis.notificacaoacidenteveicular.databinding.ListaSensoresBinding

class ListadeSensores() : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ListaSensoresBinding
    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ListaSensoresBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val list = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val adapter = ArrayAdapter<Sensor>(applicationContext,R.layout.support_simple_spinner_dropdown_item,list)
        binding.listview.adapter = adapter
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
    override fun onSensorChanged(event: SensorEvent?) {

    }
}