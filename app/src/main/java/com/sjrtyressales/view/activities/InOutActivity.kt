package com.sjrtyressales.view.activities

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityInOutBinding
import com.sjrtyressales.utils.*
import com.sjrtyressales.viewModels.activityViewModel.ViewModelInOut
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.notifyAll

@AndroidEntryPoint
class InOutActivity : AppCompatActivity(),SnackBarCallback {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    var REQUEST_CODE_CHECK_SETTINGS=123
    var mServiceIntent: Intent? = null
    private var myReceiver: MyReceiver? = null

    private lateinit var mViewModel:ViewModelInOut
    private lateinit var binding:ActivityInOutBinding
    private var callback = "0"
    private var latitude:Double=0.0
    private var longitude:Double=0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myReceiver = MyReceiver()
        binding = DataBindingUtil.setContentView(this,R.layout.activity_in_out)

        mServiceIntent = Intent(this, GpsTracker::class.java)

        /**Toolbar*/
        toolbar(getString(R.string.in_out),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelInOut::class.java]


        /**Check user is log in or not*/
        if(mViewModel.token.isNullOrEmpty()){
            logout(this)
        }else {
            /**Call attendance  GET Api*/
            mViewModel.getAttendance()

            /**Response of attendance  GET Api*/
            mViewModel.AttendanceResponse.observe(this) {
                binding.includedLoader.llLoading.visibility = View.GONE
                when (it.status) {
                    "1" -> {
                        binding.llAttendance.visibility = View.VISIBLE
                        if (it.data != null) {
                            if (!it.data?.currentTime.isNullOrEmpty()) {
                                binding.tvCurrentTime.visibility = View.VISIBLE
                                binding.tvCurrentTime.text =
                                    getString(R.string.current_time) + it.data?.currentTime
                            } else {
                                binding.tvCurrentTime.visibility = View.GONE
                            }

                            if (!it.data?.inTime.isNullOrEmpty()) {
                                binding.tvInTime.visibility = View.VISIBLE
                                binding.tvInTime.text =
                                    getString(R.string.in_time) + " - " + it.data?.inTime
                            } else {
                                binding.tvInTime.visibility = View.GONE
                            }

                            if (!it.data?.outTime.isNullOrEmpty()) {
                                binding.tvOutTime.visibility = View.VISIBLE
                                binding.tvOutTime.text =
                                    getString(R.string.out_time) + " - " + it.data?.outTime
                            } else {
                                binding.tvOutTime.visibility = View.GONE
                            }

                            if (it.data?.inTimeButton == 1) {
                                binding.btnInTime.visibility = View.VISIBLE
                                binding.btnInTime.setOnClickListener {
                                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                                    mViewModel.submitInTime(latitude, longitude)
                                }
                            } else {
                                binding.btnInTime.visibility = View.GONE
                            }

                            if (it.data?.outTimeButton == 1) {
                                binding.btnOutTime.visibility = View.VISIBLE
                                binding.btnOutTime.setOnClickListener {
                                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                                    mViewModel.submitOutTime(latitude,longitude)
                                }
                            } else {
                                binding.btnOutTime.visibility = View.GONE
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
            }

            /**Response of submitInTime or submitOutTime GET api*/
            mViewModel.SubmitInOutTimeResponse.observe(this) {
                binding.includedLoader.llLoading.visibility = View.GONE
                when (it.status) {
                    "1" -> {
                        showOkDialog(it.message)
                    }
                    "0" -> {
                        snackBar(it.message, this)
                    }
                    else -> {
                        callback = it.status
                        showSnackBar(this, it.message)
                    }
                }
            }
        }


    }

    fun showOkDialog(message: String) {
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name) as CharSequence)
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(
            R.string.ok
        ) { _, _ ->
            binding.includedLoader.llLoading.visibility = View.VISIBLE
            mViewModel.getAttendance()
        }
        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        if (checkPermission()) {
            startMyNavigationService()
        } else {
            requestPermission()
        }

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            myReceiver!!, IntentFilter(GpsTracker.ACTION_BROADCAST)
        )
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

    private fun requestPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            ), REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMyNavigationService()
        } else if (grantResults.isNotEmpty() && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            startMyNavigationService()
        } else {

        }


    }

    fun startMyNavigationService(){
        if(mServiceIntent!=null){
            startService(mServiceIntent)
        }
    }

    fun stopMyNavigationService(){
        if(mServiceIntent!=null){
            stopService(mServiceIntent)
        }
    }

    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val status = intent.getBooleanExtra(GpsTracker.EXTRA_STATUS, false)
            if (status) {
                val location = intent.getParcelableExtra<Location>(GpsTracker.EXTRA_LOCATION)
                LocalBroadcastManager.getInstance(this@InOutActivity).unregisterReceiver(myReceiver!!)
                stopMyNavigationService()
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.e("latitude","Lat:"+latitude+", Long:"+longitude)
                } else {

                }
            } else {
                LocalBroadcastManager.getInstance(this@InOutActivity).unregisterReceiver(myReceiver!!)
                stopMyNavigationService()
                enableLocationSettings()
            }

        }
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

    override fun snackBarSuccessInternetConnection() {

    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CODE_CHECK_SETTINGS == requestCode) {
            if (RESULT_OK == resultCode) {
                startMyNavigationService()
            } else {

            }
        }
    }

}