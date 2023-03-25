package com.sjrtyressales.utils

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*

class GpsTracker : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var isGPSEnabled = false
    var isNetworkEnabled = false
    var canGetLocation = false
    var locationManager:LocationManager? = null
    lateinit var mLocationCallback: LocationCallback

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    companion object{

        private const val PACKAGE_NAME = "com.sjrtyressales"
        const val ACTION_BROADCAST = PACKAGE_NAME + ".broadcast"
        const val EXTRA_LOCATION =PACKAGE_NAME + ".location"
        const val EXTRA_STATUS =PACKAGE_NAME + ".status"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?
        // getting GPS status
        isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // getting network status
        isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (isGPSEnabled && isNetworkEnabled) {
            // no network provider is enabled
            canGetLocation = true
            requestLocationUpdates()
        } else {
            canGetLocation = false
            val intent = Intent(ACTION_BROADCAST)
            intent.putExtra(EXTRA_STATUS, canGetLocation)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }

    }

    fun requestLocationUpdates(){
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

        val request = LocationRequest()
        request.interval = 10000
        request.fastestInterval = 5000
        request.priority = Priority.PRIORITY_HIGH_ACCURACY
        mLocationCallback = object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                if (location != null) {
                    // Notify anyone listening for broadcasts about the new location.
                    val intent = Intent(ACTION_BROADCAST)
                    intent.putExtra(EXTRA_STATUS, canGetLocation)
                    intent.putExtra(EXTRA_LOCATION, location)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(request,mLocationCallback, Looper.myLooper()!!)

    }



}