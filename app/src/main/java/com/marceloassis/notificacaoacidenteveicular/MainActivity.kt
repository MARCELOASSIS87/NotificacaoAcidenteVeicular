package com.marceloassis.notificacaoacidenteveicular

import android.Manifest
import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.google.android.gms.location.*
import com.marceloassis.notificacaoacidenteveicular.databinding.ActivityMainBinding
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks, SensorEventListener {
    private lateinit var binding: ActivityMainBinding

    private val TAG = "MainActivity"
    private val LOCATION_PERM = 124
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var sensorEventListener: SensorEventListener
    //private lateinit var square: TextView

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var isDone: Boolean by Delegates.observable(false) { property, oldValue, newValue ->
        if (newValue == true) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        askForLocationPermission()
        createLocationRequest()
        //square = binding.grausTv

        setUpSensorStuff()

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (!isDone){
                    val speedToInt = ( locationResult.lastLocation.speed * 3.6).toInt()
                    //calcSpeed(speedToInt)
                    binding.velocidadeTv.text = speedToInt.toString()
                }
            }
        }
    }
    private fun setUpSensorStuff(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun askForLocationPermission() {
        if (hasLocationPermission()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location: Location? ->

                }
        } else {
            EasyPermissions.requestPermissions(
                this,
                "É preciso que voce permita o uso da localização e calcule sua velocidade",
                LOCATION_PERM,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){
            val yes="Permitido"
            val no = "Negado"
            Toast.makeText(this,"onActivityResult",Toast.LENGTH_LONG).show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onRationaleAccepted(requestCode: Int) {
        TODO("Not yet implemented")
    }

    override fun onRationaleDenied(requestCode: Int) {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            val xpart = event.values[1]
            val zpart = event.values[2]

            val inclinacao = event.values[0]
            //Calculo necessário para que o retorno da variável values se torne graus
            var magnetude = Math.sqrt((xpart*xpart + inclinacao*inclinacao + zpart*zpart).toDouble())
            var cosTheta = inclinacao/magnetude
            var thetaGraus = (Math.acos(cosTheta) * 180.0/Math.PI).toInt()
            binding.grausTv.text = thetaGraus.toString()
            if (thetaGraus>=130){
                ligar()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    fun getPermissionCall(mainActivity: MainActivity): Boolean {
        val REQUEST_PERMISSION_CALL = 221
        var res = true
        var string = "permissão nao obtida"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                res = false
                ActivityCompat.requestPermissions(this, arrayOf(CALL_PHONE), REQUEST_PERMISSION_CALL)
            }
        }
        return res
    }
    fun ligar(){
        if(getPermissionCall(this)){
            val telefone = "035998870879"

                val uri = Uri.parse("telefone:" + telefone)
                val intent = Intent(Intent.ACTION_CALL,uri)
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                return
            }
            startActivity(intent)
            }
        }
    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
    }





