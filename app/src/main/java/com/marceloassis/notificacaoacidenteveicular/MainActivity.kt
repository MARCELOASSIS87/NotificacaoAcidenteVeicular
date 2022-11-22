package com.marceloassis.notificacaoacidenteveicular

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.Response
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpRequest
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient
import com.google.firebase.firestore.util.Listener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.marceloassis.notificacaoacidenteveicular.databinding.ActivityMainBinding
import com.marceloassis.notificacaoacidenteveicular.http.HttpHelper
import com.marceloassis.notificacaoacidenteveicular.model.User
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.json.JSONException
import org.json.JSONObject
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.lang.NullPointerException
import java.net.HttpURLConnection
import java.net.URL
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.thread
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks, SensorEventListener,
    LocationListener {
    private lateinit var binding: ActivityMainBinding

    private val TAG = "MainActivity"
    private val LOCATION_PERM = 124
    private lateinit var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private lateinit var sensorEventListener: SensorEventListener

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
        permissaoChamadaTelefonica()
        createLocationRequest()

        setUpSensorStuff()

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {

                if (!isDone) {
                    val speedToInt = (locationResult.lastLocation!!.speed * 3.6).toInt()
                    binding.velocidadeTv.text = speedToInt.toString()

                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val xpart = event.values[1]
            val zpart = event.values[2]

            val inclinacao = event.values[0]
            //Calculo necessário para que o retorno da variável values se torne graus
            var magnetude =
                Math.sqrt((xpart * xpart + inclinacao * inclinacao + zpart * zpart).toDouble())
            var cosTheta = inclinacao / magnetude
            var thetaGraus = (Math.acos(cosTheta) * 180.0 / Math.PI).toInt()
            binding.grausTv.text = thetaGraus.toString()
            if (thetaGraus >= 130 && thetaGraus <= 140) {
                enviarInformacao()
                println("Dentro do if do thetha")
            }
        }
    }

    fun pararMonitoramento() {
        finish()
    }

    fun enviarInformacao() {
        val nomeCadastrado = intent?.extras?.getString("nome").toString()
        val sobrenomeCadastrado = intent?.extras?.getString("sobrenome").toString()
        val gson = Gson()


        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            var location: Location = task.result
            val usuario = User()
            usuario.name = nomeCadastrado
            usuario.lastname = sobrenomeCadastrado
            usuario.latitude = location.latitude
            usuario.longitude = location.longitude
            val userJson = gson.toJson(usuario)
            doAsync{
                val http = HttpHelper()
                http.post(userJson)
                println("###################" + userJson)
            }
//            fun thread() {
//                val thread = Thread(
//                    Runnable {
//                        val http = HttpHelper()
//                            http.post(userJson)
//                            println("###################" + userJson)
//                    }
//                )
//                thread.start()
//            }
        }
    }

    fun ligar() {
        val numero = "035998972008" //filinha
        val uri = Uri.parse("tel:" + numero)
        intent = Intent(Intent.ACTION_CALL, uri)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        startActivity(intent)
    }

    fun permissaoChamadaTelefonica() {
        ActivityCompat.requestPermissions(
            this,
            Array<String>(1) { Manifest.permission.CALL_PHONE },
            1
        )
        return
    }

    private fun setUpSensorStuff() {
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
            val interval = 1000
            var priority = LocationRequest.PRIORITY_HIGH_ACCURACY

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
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            val yes = "Permitido"
            val no = "Negado"
            Toast.makeText(this, "onActivityResult", Toast.LENGTH_LONG).show()
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

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onLocationChanged(p0: Location) {
        TODO("Not yet implemented")
    }


}

private fun FusedLocationProviderClient.requestLocationUpdates(
    locationRequest: LocationRequest,
    locationCallback: LocationCallback,
    mainLooper: Looper?
) {}





