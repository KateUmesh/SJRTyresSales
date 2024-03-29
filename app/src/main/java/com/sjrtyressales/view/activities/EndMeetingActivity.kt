package com.sjrtyressales.view.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityEndMeetingBinding
import com.sjrtyressales.utils.*
import com.sjrtyressales.screens.endMeeting.viewModel.ViewModelEndMeeting
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


@AndroidEntryPoint
class EndMeetingActivity : AppCompatActivity(),SnackBarCallback {
    private lateinit var binding:ActivityEndMeetingBinding
    private lateinit var mViewModel: ViewModelEndMeeting
    @Inject
    lateinit var mUploadPhoto: UploadPhoto

    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var finalUri: Uri

    private var meetingId:Int=0

    private var uriFilePath: Uri? = null

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    var REQUEST_CODE_CHECK_SETTINGS=123

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var isGPSEnabled = false
    var isNetworkEnabled = false
    var locationManager:LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_end_meeting)

        /**Toolbar*/
        toolbar(getString(R.string.endMeeting),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelEndMeeting::class.java]

        /**Hide Loader*/
        binding.includedLoader.llLoading.visibility = View.GONE

        /**Get meetingId From MeetingActivity*/
        if(!intent.getStringExtra(Constant.meetingId).isNullOrEmpty()) {
            meetingId =intent.getStringExtra(Constant.meetingId)!!.toInt()
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?


        /**Initialize Camera Result Launcher*/
        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val photo: Bitmap = result.data?.extras?.get("data") as Bitmap
                val mImageName = "IMG_" + System.currentTimeMillis().toString()
                val imageToUploadUri:Uri
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                     imageToUploadUri = mUploadPhoto.saveImage(photo, mImageName, contentResolver)
                    finalUri=imageToUploadUri
                    uriFilePath = ("file://"+ getFilePathFromUri(this,imageToUploadUri,true)).toUri()
                    mUploadPhoto.setImage(finalUri, this, binding.ivCapturePhoto)
                }else{
                    imageToUploadUri = mUploadPhoto.saveImage1(photo,mImageName,contentResolver)
                    finalUri=imageToUploadUri
                    uriFilePath = ("file://"+ getFilePathFromUri(this,imageToUploadUri,true)).toUri()
                    mUploadPhoto.setImage(finalUri, this, binding.ivCapturePhoto)
                }

            }
        }

        /**Capture photo button click*/
        binding.coCapturePhoto.setOnClickListener {
            mUploadPhoto.checkPermissionsToOpenCamera(this,requestMultiplePermissions,cameraResultLauncher)
        }

        /**Submit button click*/
        binding.btnEndMeeting.setOnClickListener {
            hideKeyboard(binding.edtConclusion)
            validation1()

            /*if (checkPermission()) {
                // getting GPS status
                isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                // getting network status
                isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                //toast("Permission granted in result")
                if(isGPSEnabled&&isNetworkEnabled){
                    toast("GPSEnabled")
                    getLocation()
                }else{
                    toast("GPS Disabled")
                    enableLocationSettings()
                }

            } else {
                requestPermission()
            }*/

        }

        /**Response of endMeeting POST api*/
        mViewModel.EndMeetingResponse.observe(this, Observer {
            binding.includedLoader.llLoading.visibility = View.GONE
            when(it.status){
                "1"->{
                    showOkToFinishDialog(getString(R.string.endMeeting),it.message,this)
                }
                "0"->{
                    if(it.data?.userActive=="0"){
                        logout(this)
                    }else {
                        snackBar(it.message, this)
                    }
                }
                else->{
                    showSnackBar(this,it.status)
                }
            }
        })

    }

    private fun validation(conclusion:String,latitude:String,longitude:String) {
        if(conclusion.isEmpty()){
            snackBar(getString(R.string.conclusion_required),this)
        }else if(uriFilePath==null){
            snackBar(getString(R.string.photo_required),this)
        }else{
            val conclusion1: RequestBody = conclusion.toRequestBody("text/plain".toMediaTypeOrNull())
            val meetingId1: RequestBody = meetingId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val latitude1: RequestBody = latitude.toRequestBody("text/plain".toMediaTypeOrNull())
            val longitude1: RequestBody = longitude.toRequestBody("text/plain".toMediaTypeOrNull())
            val distributorName: RequestBody = binding.edtDistributorName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val distributorMobile: RequestBody = binding.edtDistributorMobile.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            binding.includedLoader.llLoading.visibility = View.VISIBLE
            mViewModel.endMeeting(mUploadPhoto.uploadFile(uriFilePath!!,"photo"),meetingId1,distributorName,distributorMobile,conclusion1,latitude1,longitude1)
        }
    }

    private fun validation1() {
        if(binding.edtConclusion.text.toString().isEmpty()){
            snackBar(getString(R.string.conclusion_required),this)
        }else if(uriFilePath==null){
            snackBar(getString(R.string.photo_required),this)
        }else{
            if (checkPermission()) {
                // getting GPS status
                isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                // getting network status
                isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                //toast("Permission granted in result")
                if(isGPSEnabled&&isNetworkEnabled){
                    toast("GPSEnabled")
                    getLocation()
                }else{
                    toast("GPS Disabled")
                    enableLocationSettings()
                }

            } else {
                requestPermission()
            }
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            mUploadPhoto.openCamera(cameraResultLauncher)
        }else{
            showOkDialog1(getString(R.string.camera_permission_rationale),this)
        }
    }

    fun showOkDialog1(message: String, context: Context) {
        val builder =
            AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.allow_permission) as CharSequence)
        builder.setMessage(message)
        builder.setPositiveButton(
            R.string.Ok
        ) { _, _ ->

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        builder.setNegativeButton(
            R.string.Cancel
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        builder.show()
    }

    override fun snackBarSuccessInternetConnection() {
        validation1()
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
        if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)&&( grantResults[1] == PackageManager.PERMISSION_GRANTED)){
            //toast("Permission granted in result")
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // getting network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if(isGPSEnabled&&isNetworkEnabled){
                getLocation()
            }else{
                enableLocationSettings()
            }
        }else{
            showOkDialog2(getString(R.string.location_permission_rationale),this)
        }


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

            /* val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
             val uri = Uri.fromParts("package", packageName, null)
             intent.data = uri
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
             startActivity(intent)*/
        }

        builder.show()
    }

    fun enableLocationSettings() {
        val locationRequest = LocationRequest.create().setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        /*val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setIntervalMillis(500)
                .build()*/
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
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
                getLocation()
            } else {
                enableLocationSettings()
            }
        }
    }


    fun getLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE);
        }else{

            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    location : Location? ->
                if (location != null) {
                    val lat = location.latitude.toString()
                    val longi = location.longitude.toString()
                    Log.e("latitude","Lat:"+lat+", Long:"+longi)
                    /*validation(binding.edtConclusion.text.toString(),lat.toString(),longi.toString())*/
                    val conclusion = binding.edtConclusion.text.toString()
                    val conclusion1: RequestBody = conclusion.toRequestBody("text/plain".toMediaTypeOrNull())
                    val meetingId1: RequestBody = meetingId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val distributorName: RequestBody = binding.edtDistributorName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val distributorMobile: RequestBody = binding.edtDistributorMobile.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val latitude1: RequestBody = lat.toRequestBody("text/plain".toMediaTypeOrNull())
                    val longitude1: RequestBody = longi.toRequestBody("text/plain".toMediaTypeOrNull())
                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                    mViewModel.endMeeting(mUploadPhoto.uploadFile(uriFilePath!!,"photo"),meetingId1,distributorName,distributorMobile,conclusion1,latitude1,longitude1)
                } else {
                    //Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
                    getLocation()
                }
            }

           /* val locationGPS = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (locationGPS != null) {
                val lat = locationGPS.latitude
                val longi = locationGPS.longitude
                Log.e("latitude","Lat:"+lat+", Long:"+longi)
            } else {
                //Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }*/
        }
    }
}