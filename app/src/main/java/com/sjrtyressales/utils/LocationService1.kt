package com.sjrtyressales.utils

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.*
import com.sjrtyressales.screens.splashScreen.model.ModelUpdateLiveLocationRequest
import com.sjrtyressales.screens.splashScreen.model.ModelUpdateLiveLocationResponse
import com.sjrtyressales.network.ApiClient2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LocationService1 : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var localSharedPreferences: LocalSharedPreferences
    private val locationRequest: LocationRequest = create().apply {
        interval = 300000
        fastestInterval = 300000
        priority = Priority.PRIORITY_HIGH_ACCURACY
        maxWaitTime = 300000
    }
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                /*Toast.makeText(this@LocationService1, "Latitude: " + location.latitude.toString() + '\n' +
                        "Longitude: "+ location.longitude, Toast.LENGTH_LONG).show()
                Log.d("Location d", location.latitude.toString())
                Log.i("Location i", location.longitude.toString())*/

                if(localSharedPreferences.getStringValue(Constant.token)!!.isNotEmpty()) {
                    callUpdateLiveLocationPostApi(
                        location.latitude.toString(),
                        location.longitude.toString(),
                        localSharedPreferences.getStringValue(Constant.token)!!
                    )
                }
            }
        }
    }
    override fun onCreate() {
        super.onCreate()
        localSharedPreferences = LocalSharedPreferences(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel() else startForeground(1, Notification())

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(applicationContext, "Permission required", Toast.LENGTH_LONG).show()
            return
        }else{
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val notificationChannelId = "Location channel id"
        val channelName = "Background Service"
        val chan = NotificationChannel(notificationChannelId, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(this, notificationChannelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("Location updates:")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
        //fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun callUpdateLiveLocationPostApi(
        latitude:String,
        longitude:String,token:String
    ) {


        val modelUpdateLiveLocationRequest = ModelUpdateLiveLocationRequest(latitude, longitude)

        ApiClient2.build(token)?.updateLiveLocation1(modelUpdateLiveLocationRequest)?.enqueue(object:
            Callback<ModelUpdateLiveLocationResponse> {
            override fun onResponse(
                call: Call<ModelUpdateLiveLocationResponse>,
                response: Response<ModelUpdateLiveLocationResponse>
            ) {
                if (response.isSuccessful) {

                    Log.e("location:", response.body()?.message!!)
                } else {
                    Log.e("error:", "Error :" + response.toString())
                }
            }

            override fun onFailure(call: Call<ModelUpdateLiveLocationResponse>, t: Throwable) {
                Log.e("error:", "Error :" + t.localizedMessage)
            }

        })
    }
}
