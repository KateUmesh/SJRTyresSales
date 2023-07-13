package com.sjrtyressales.screens.splashScreen.view

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sjrtyressales.BuildConfig
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.screens.splashScreen.model.ModelAppStatusRequest
import com.sjrtyressales.utils.*
import com.sjrtyressales.screens.splashScreen.viewModel.ViewModelSplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SnackBarCallback {
    var mLocationService: LocationService1 = LocationService1()
    lateinit var mServiceIntent: Intent
    private lateinit var mViewModel: ViewModelSplashScreen


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelSplashScreen::class.java]


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                        AlertDialog.Builder(this).apply {
                            setTitle("Background permission")
                            setCancelable(false)
                            setMessage(R.string.background_location_permission_message)
                            /*setPositiveButton("Start service anyway",
                                DialogInterface.OnClickListener { dialog, id ->
                                    callAppStatusApi()
                                })*/
                            setPositiveButton("Grant background Permission",
                                DialogInterface.OnClickListener { dialog, id ->
                                    requestBackgroundLocationPermission()
                                })
                        }.create().show()

                    }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
                        callAppStatusApi()
                    }
                }else{
                    callAppStatusApi()
                }
            }else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                requestFineLocationPermission()
            }


        /**Response of appStatus GET api*/
        if(this::mViewModel.isInitialized) {
            mViewModel.AppStatusResponse.observe(this, Observer {
                starServiceFunc()
                when (it.status) {
                    "1" -> {
                        val inTimeDialog = it.data?.inTimeDialog!!
                        val inTimeDialogMessage = it.data?.inTimeDialogMessage!!
                        if (it.data?.appStatus == "1") {
                            showForceUpdateDialog(it.data?.appStatusMessage!!)
                        } else {
                            if (mViewModel.token.isNullOrEmpty()) {
                                callLoginActivity(this, inTimeDialog, inTimeDialogMessage)
                            } else {
                                callHomeActivity(this, inTimeDialog, inTimeDialogMessage)
                            }
                        }
                    }
                    "0" -> {
                        snackBar(it.message, this)
                    }
                    else -> {
                        showSnackBar(this, it.message)
                    }
                }
            })
        }
    }

    fun callAppStatusApi(){
        val mModelAppStatusRequest = ModelAppStatusRequest(mViewModel.token!!,BuildConfig.VERSION_NAME)
        mViewModel.appStatus(mModelAppStatusRequest)
    }

    private fun starServiceFunc(){
        mLocationService = LocationService1()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (!Util.isMyServiceRunning(mLocationService.javaClass, this)) {
            startService(mServiceIntent)

           // Toast.makeText(this, getString(R.string.service_start_successfully), Toast.LENGTH_SHORT).show()
        } else {
           // Toast.makeText(this, getString(R.string.service_already_running), Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopServiceFunc(){
        mLocationService = LocationService1()
        mServiceIntent = Intent(this, mLocationService.javaClass)
        if (Util.isMyServiceRunning(mLocationService.javaClass, this)) {
            stopService(mServiceIntent)
            //Toast.makeText(this, "Service stopped!!", Toast.LENGTH_SHORT).show()
        } else {
            //Toast.makeText(this, "Service is already stopped!!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        /*  if (::mServiceIntent.isInitialized) {
              stopService(mServiceIntent)
          }*/
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), MY_BACKGROUND_LOCATION_REQUEST
        )
    }

    private fun requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,), MY_FINE_LOCATION_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_FINE_LOCATION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            requestBackgroundLocationPermission()
                        }else{
                            callAppStatusApi()
                        }
                    }

                } else {
                    AlertDialog.Builder(this)
                        .setTitle("ACCESS_FINE_LOCATION")
                        .setMessage("Location permission required")
                        .setPositiveButton(
                            "OK"
                        ) { _, _ ->
                            requestFineLocationPermission()
                        }
                        .create()
                        .show()
                }
                return
            }
            MY_BACKGROUND_LOCATION_REQUEST -> {

                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        callAppStatusApi()
                    }
                } else {
                    AlertDialog.Builder(this).apply {
                        setTitle("Background permission")
                            setCancelable(false)
                        setMessage(R.string.background_location_permission_message)
                        /*setPositiveButton("Start service anyway",
                            DialogInterface.OnClickListener { dialog, id ->
                                callAppStatusApi()
                            })*/
                        setPositiveButton("Grant background Permission",
                            DialogInterface.OnClickListener { dialog, id ->
                                requestBackgroundLocationPermission()
                            })
                    }.create().show()
                }
                return
            }
        }
    }

    private fun showForceUpdateDialog(message:String) {
        val builder =
            androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.new_version_available) as CharSequence)
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.update)) { _, _ ->
            finish()
        }
        builder.show()
    }

    companion object {
        private const val MY_FINE_LOCATION_REQUEST = 99
        private const val MY_BACKGROUND_LOCATION_REQUEST = 100
    }

    override fun snackBarSuccessInternetConnection() {
        val mModelAppStatusRequest = ModelAppStatusRequest(mViewModel.token!!, BuildConfig.VERSION_NAME)
        mViewModel.appStatus(mModelAppStatusRequest)
    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }

}