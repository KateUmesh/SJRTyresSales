package com.sjrtyressales.view.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.sjrtyressales.BuildConfig
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivitySplashScreenBinding
import com.sjrtyressales.model.LocationEvent
import com.sjrtyressales.model.ModelAppStatusRequest
import com.sjrtyressales.model.ModelUpdateLiveLocationRequest
import com.sjrtyressales.utils.*
import com.sjrtyressales.viewModels.activityViewModel.ViewModelSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity(),SnackBarCallback {
    var mLocationService: LocationService1 = LocationService1()
    lateinit var mServiceIntent: Intent
    private lateinit var mViewModel:ViewModelSplashScreen

    var isGPSEnabled = false
    var isNetworkEnabled = false
    var locationManager: LocationManager? = null
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34



    private var service: Intent?=null

    var REQUEST_CODE_CHECK_SETTINGS = 123

    private val backgroundLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {

            }
        }

    private val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            backgroundLocation.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    }

                }
                it.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

                }
            }
        }

    private var _binding: ActivitySplashScreenBinding? = null
    private val binding: ActivitySplashScreenBinding
        get() = _binding!!

    private var latitude:Double=0.0
    private var longitude:Double=0.0
    private var callback = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_splash_screen)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?
        service = Intent(this, LocationService::class.java)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelSplashScreen::class.java]


        checkPermission1()

        /*if (checkPermission()) {
            // getting GPS status
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // getting network status
            isNetworkEnabled =
                locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            //toast("Permission granted in result")
            if (isGPSEnabled && isNetworkEnabled) {
                //getLocation()
                callAppStatusGetApi()
            } else {
                enableLocationSettings()
            }

        } else {
            requestPermission()
        }*/



        /**Response of appStatus GET api*/
        mViewModel.AppStatusResponse.observe(this, Observer {
            startService(service)
            when(it.status){
                "1"->{
                    val inTimeDialog=it.data?.inTimeDialog!!
                    val inTimeDialogMessage=it.data?.inTimeDialogMessage!!
                    if(it.data?.appStatus=="1"){
                        showForceUpdateDialog(it.data?.appStatusMessage!!)
                    }else{
                        if(mViewModel.token.isNullOrEmpty()) {
                            callLoginActivity(this,inTimeDialog,inTimeDialogMessage)
                        }else{
                            callHomeActivity(this,inTimeDialog,inTimeDialogMessage)
                        }
                    }
                }
                "0"->{
                    snackBar(it.message,this)
                }
                else->{
                    showSnackBar(this,it.message)
                }
            }
        })



        /*Handler(Looper.getMainLooper()).postDelayed({
            if(localSharedPreferences.getStringValue(Constant.token).isNullOrEmpty()) {
                callLoginActivity(this)
            }else{
                callHomeActivity(this)
            }
        }, 1000)*/

    }

    private fun showForceUpdateDialog(message:String) {
        val builder =
            AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.new_version_available) as CharSequence)
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.update)) { _, _ ->
            /*startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(Constant.appUrl + packageName)
                )
            )*/
            finish()
        }
        builder.show()
    }





    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
    }

    fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissions.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }else{

                /**Call appStatus GET api*/
                val mModelAppStatusRequest = ModelAppStatusRequest(mViewModel.token!!,BuildConfig.VERSION_NAME)
                mViewModel.appStatus(mModelAppStatusRequest)

            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        /*stopService(service)
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }*/
    }

    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent){
        binding.tvLatitude.text = "Latitude -> ${locationEvent.latitude}"
        binding.tvLongitude.text = "Longitude -> ${locationEvent.longitude}"
        Log.e("Latitude", "Latitude -> ${locationEvent.latitude}")
        Log.e("Longitude", "Longitude -> ${locationEvent.longitude}")
        latitude = locationEvent.latitude!!
        longitude = locationEvent.longitude!!
        /*val mModelUpdateLiveLocationRequest = ModelUpdateLiveLocationRequest(locationEvent.latitude.toString(),locationEvent.longitude.toString() )
        mViewModel.updateLiveLocation(mModelUpdateLiveLocationRequest)*/

        /*Handler(Looper.getMainLooper()).postDelayed({
            val mModelUpdateLiveLocationRequest = ModelUpdateLiveLocationRequest(locationEvent.latitude.toString(),locationEvent.longitude.toString() )
            mViewModel.updateLiveLocation(mModelUpdateLiveLocationRequest)
        }, 180000)*/

    }

    override fun snackBarSuccessInternetConnection() {
        val mModelAppStatusRequest = ModelAppStatusRequest(mViewModel.token!!,BuildConfig.VERSION_NAME)
        mViewModel.appStatus(mModelAppStatusRequest)
    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }

    private fun checkPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            return false
        }
        return true
    }

    private fun checkPermission1(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                    starServiceFunc()
                }else{
                    requestBackgroundLocationPermission()
                }
            }else{
                requestFineLocationPermission()
            }

        }else{
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestFineLocationPermission()
            }else{
                starServiceFunc()
            }
        }
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            ), REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),MY_BACKGROUND_LOCATION_REQUEST
        )
    }

    private fun requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,),
            MY_FINE_LOCATION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            MY_FINE_LOCATION_REQUEST->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestBackgroundLocationPermission()
                    }else{
                        starServiceFunc()
                    }
                }else{
                    Toast.makeText(this, "ACCESS_FINE_LOCATION permission denied", Toast.LENGTH_LONG).show()
                }
            }
            MY_BACKGROUND_LOCATION_REQUEST->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Background location Permission Granted", Toast.LENGTH_LONG).show()
                    starServiceFunc()
                }else{
                    Toast.makeText(this, "Background location permission denied", Toast.LENGTH_LONG).show()
                }
            }

        }

        /*if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            //toast("Permission granted in result")
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // getting network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (isGPSEnabled && isNetworkEnabled) {
                callAppStatusGetApi()
            } else {
                enableLocationSettings()
            }
        } else {
            showOkDialog2(getString(R.string.location_permission_rationale), this)
        }*/


    }

    fun showOkDialog2(message: String, context: Context) {
        val builder =
            AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.allow_permission) as CharSequence)
        builder.setMessage(message)
        builder.setPositiveButton(
            R.string.Ok
        ) { _, _ ->
            requestPermission()

        }

        builder.show()
    }

    fun enableLocationSettings() {
        val locationRequest = LocationRequest.create()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(
                this
            ) { response: LocationSettingsResponse? -> }
            .addOnFailureListener(
                this
            ) { ex: java.lang.Exception? ->
                if (ex is ResolvableApiException) {
                    // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                        ex.startResolutionForResult(
                            this,
                            REQUEST_CODE_CHECK_SETTINGS
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CODE_CHECK_SETTINGS == requestCode) {
            if (RESULT_OK == resultCode) {
                callAppStatusGetApi()
            } else {
                enableLocationSettings()
            }
        }
    }

    fun callAppStatusGetApi(){
        /**Call appStatus GET api*/
        val mModelAppStatusRequest = ModelAppStatusRequest(mViewModel.token!!,BuildConfig.VERSION_NAME)
        mViewModel.appStatus(mModelAppStatusRequest)
    }

    companion object {
        private const val MY_FINE_LOCATION_REQUEST = 99
        private const val MY_BACKGROUND_LOCATION_REQUEST = 100
    }

    private fun starServiceFunc(){
        mLocationService = LocationService1()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (!Util.isMyServiceRunning(mLocationService.javaClass, this)) {
            startService(mServiceIntent)

            Toast.makeText(this, getString(R.string.service_start_successfully), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.service_already_running), Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopServiceFunc(){
        mLocationService = LocationService1()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (Util.isMyServiceRunning(mLocationService.javaClass, this)) {
            stopService(mServiceIntent)
            Toast.makeText(this, "Service stopped!!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Service is already stopped!!", Toast.LENGTH_SHORT).show()
        }
    }

}