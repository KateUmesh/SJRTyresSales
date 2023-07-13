package com.sjrtyressales.utils

import android.app.Service
import com.google.android.gms.location.FusedLocationProviderClient
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.sjrtyressales.screens.splashScreen.model.LocationEvent
import com.sjrtyressales.screens.splashScreen.model.ModelUpdateLiveLocationRequest
import com.sjrtyressales.screens.splashScreen.model.ModelUpdateLiveLocationResponse
import com.sjrtyressales.network.ApiClient1
import com.sjrtyressales.network.ApiClient2
import com.sjrtyressales.network.ApiInterface1
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class LocationService : Service()
{
    companion object {
        const val CHANNEL_ID = "12345"
        const val NOTIFICATION_ID=12345
    }

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null

    private var notificationManager: NotificationManager? = null

    private var location:Location?=null

    override fun onCreate() {
        super.onCreate()


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        /*locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setIntervalMillis(500)
                .build()*/

         locationRequest = LocationRequest()
        locationRequest!!.interval = 5000
        locationRequest!!.fastestInterval = 5000
        locationRequest!!.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult)
            }
        }
        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, "locations", NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    @Suppress("MissingPermission")
    fun createLocationRequest(){
        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!,locationCallback!!,null
            )
        }catch (e:Exception){
            e.printStackTrace()
        }

    }



    private fun removeLocationUpdates(){
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        stopForeground(true)
        stopSelf()
    }

    private fun onNewLocation(locationResult: LocationResult) {
        location = locationResult.lastLocation
        EventBus.getDefault().post(
            LocationEvent(
            latitude = location?.latitude,
            longitude = location?.longitude
        )
        )
        toast("Lat"+location?.latitude.toString()+"\n Long"+location?.longitude.toString())
        val localSharedPreferences = LocalSharedPreferences(this)
        /*val remoteHeaders = HashMap<String, String>()
        remoteHeaders[Constant.REMOTE_MSG_AUTHORIZATION] = Constant.Bearer+localSharedPreferences.getStringValue(Constant.token)
        remoteHeaders[Constant.API_KEY] = Constant.KEY
        callUpdateLiveLocationPostApi(remoteHeaders,location?.latitude.toString(),location?.longitude.toString())*/
        callUpdateLiveLocationPostApi(location?.latitude.toString(),location?.longitude.toString(),localSharedPreferences.getStringValue(Constant.token)!!)
        //startForeground(NOTIFICATION_ID,getNotification())
    }

    fun getNotification():Notification{
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Updates")
            .setContentText(
                "Latitude--> ${location?.latitude}\nLongitude --> ${location?.longitude}"
            )
            .setSmallIcon(com.sjrtyressales.R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notification.setChannelId(CHANNEL_ID)
        }
        return notification.build()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createLocationRequest()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        //removeLocationUpdates()
    }

    private fun callUpdateLiveLocationPostApi(
        remoteHeaders: HashMap<String, String>,
        latitude:String,
        longitude:String
    ) {
        val modelUpdateLiveLocationRequest = ModelUpdateLiveLocationRequest(latitude, longitude)

        ApiClient1.getClient()?.create(ApiInterface1::class.java)!!
            .updateLiveLocation(remoteHeaders,modelUpdateLiveLocationRequest)!!
            .enqueue(object : Callback<ModelUpdateLiveLocationResponse> {
                override fun onResponse(call: Call<ModelUpdateLiveLocationResponse>, response: Response<ModelUpdateLiveLocationResponse>) {
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

    private fun callUpdateLiveLocationPostApi(
        latitude:String,
        longitude:String,token:String
    ) {


        val modelUpdateLiveLocationRequest = ModelUpdateLiveLocationRequest(latitude, longitude)

        ApiClient2.build(token)?.updateLiveLocation1(modelUpdateLiveLocationRequest)?.enqueue(object: Callback<ModelUpdateLiveLocationResponse>{
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