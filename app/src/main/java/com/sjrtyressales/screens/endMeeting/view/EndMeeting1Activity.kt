package com.sjrtyressales.screens.endMeeting.view

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.CropImageContract
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityEndMeeting1Binding
import com.sjrtyressales.utils.*
import com.sjrtyressales.screens.endMeeting.viewModel.ViewModelEndMeeting
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class EndMeeting1Activity : AppCompatActivity(), SnackBarCallback {
    lateinit var binding: ActivityEndMeeting1Binding

    @Inject
    lateinit var mUploadPhoto: UploadPhoto

    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var finalUri: Uri

    private lateinit var mViewModel: ViewModelEndMeeting

    private var uriFilePath: Uri? = null

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    var REQUEST_CODE_CHECK_SETTINGS = 123

    private var latitude = ""
    private var longitude = ""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var isGPSEnabled = false
    var isNetworkEnabled = false
    var locationManager: LocationManager? = null

    private var meetingId: Int = 0

    private var fileUri: File? = null
    private var queryImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_end_meeting_1)

        /**Toolbar*/
        toolbar(getString(R.string.endMeeting), true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelEndMeeting::class.java]

        /**Get meetingId From MeetingActivity*/
        if (!intent.getStringExtra(Constant.meetingId).isNullOrEmpty()) {
            meetingId = intent.getStringExtra(Constant.meetingId)!!.toInt()
        }

        /**Hide Loader*/
        binding.includedLoader.llLoading.visibility = View.GONE

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?

        /**Initialize Camera Result Launcher*/
        cameraResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    //mUploadPhoto.setImage(fileUri?.toUri()!!, this, binding.ivCapturePhoto)
                   /* Log.e("fileUri","FileUri ->" +fileUri?.toUri()!!)
                    try {
                        if(result.data!=null) {
                            val photo: Bitmap = result.data?.extras?.get("data") as Bitmap
                            val mImageName = "IMG_" + System.currentTimeMillis().toString()
                            //val imageToUploadUri=mUploadPhoto.saveImage(photo,mImageName,contentResolver)
                            val imageToUploadUri: Uri
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                imageToUploadUri =
                                    mUploadPhoto.saveImage(photo, mImageName, contentResolver)
                                finalUri = imageToUploadUri
                                uriFilePath =
                                    ("file://" + getFilePathFromUri(
                                        this,
                                        imageToUploadUri,
                                        false
                                    )).toUri()
                                mUploadPhoto.setImage(finalUri, this, binding.ivCapturePhoto)
                            } else {
                                imageToUploadUri =
                                    mUploadPhoto.saveImage1(photo, mImageName, contentResolver)
                                finalUri = imageToUploadUri
                                uriFilePath =
                                    ("file://" + getFilePathFromUri(
                                        this,
                                        imageToUploadUri,
                                        false
                                    )).toUri()
                                mUploadPhoto.setImage(finalUri, this, binding.ivCapturePhoto)
                            }
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }*/

                    //mUploadPhoto.launchImageCrop(imageToUploadUri,cropImage)

                    binding.pbLoading.visibility = View.VISIBLE
                    try{
                        handleImageRequest(fileUri)
                    }catch(e:Exception){
                        e.printStackTrace()
                    }
                }
            }

        /**Capture photo button click*/
        /*binding.btnCapturePhoto.setOnClickListener {
            mUploadPhoto.checkPermissionsToOpenCamera(this,requestMultiplePermissions,cameraResultLauncher)
        }*/

        binding.coCapturePhoto.setOnClickListener {
            fileUri = mUploadPhoto.checkPermissionsToOpenCamera(
                this,
                requestMultiplePermissions,
                cameraResultLauncher
            )
        }


        /**Submit button click*/
        binding.btnEndMeeting.setOnClickListener {
            hideKeyboard(binding.edtConclusion)
            validation1(binding.edtConclusion.text.toString(),binding.edtDistributorName.text.toString(),binding.edtDistributorMobile.text.toString())
        }

        /**Response of endMeeting POST api*/
        mViewModel.EndMeetingResponse.observe(this, Observer {
            binding.includedLoader.llLoading.visibility = View.GONE
            when (it.status) {
                "1" -> {
                    showOkToFinishDialog(getString(R.string.endMeeting), it.message, this)
                }
                "0" -> {
                    if (it.data?.userActive == "0") {
                        logout(this)
                    } else {
                        snackBar(it.message, this)
                    }
                }
                else -> {
                    showSnackBar(this, it.message)
                }
            }
        })
    }

    /*private fun validation(title:String,amount:String) {
        if(title.isEmpty()){
            snackBar(getString(R.string.title_required),this)
        }else  if(amount.isEmpty()){
            snackBar(getString(R.string.amount_required),this)
        }else if(uriFilePath==null){
            snackBar(getString(R.string.photo_required),this)
        }else{
            val title1: RequestBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val amount1: RequestBody = amount.toRequestBody("text/plain".toMediaTypeOrNull())
            val latitude1: RequestBody = latitude.toRequestBody("text/plain".toMediaTypeOrNull())
            val longitude1: RequestBody = longitude.toRequestBody("text/plain".toMediaTypeOrNull())
            binding.includedLoader.llLoading.visibility = View.VISIBLE
            mViewModel.postAllowance(mUploadPhoto.uploadFile(uriFilePath!!,"photo"),title1,amount1,latitude1,longitude1)
        }
    }*/

    private fun validation1(conclusion: String, name: String, mobile: String) {
        if (name.isEmpty()) {
            snackBar(getString(R.string.name_required), this)
        } else if (mobile.length<10) {
            snackBar(getString(R.string.invalid_mobile_number), this)
        } else if (conclusion.isEmpty()) {
            snackBar(getString(R.string.conclusion_required), this)
        } else if (uriFilePath == null) {
            snackBar(getString(R.string.photo_required), this)
        } else {
            if (checkPermission()) {
                // getting GPS status
                isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                // getting network status
                isNetworkEnabled =
                    locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                //toast("Permission granted in result")
                if (isGPSEnabled && isNetworkEnabled) {
                    getLocation()
                } else {
                    enableLocationSettings()
                }

            } else {
                requestPermission()
            }

        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent!!
            finalUri = uriContent
            uriFilePath = ("file://" + result.getUriFilePath(this)).toUri()
            Log.e("uriFilePath", uriFilePath.toString())
            mUploadPhoto.setImage(finalUri, this, binding.ivCapturePhoto)
        } else {
            // An error occurred.
            //val exception = result.error
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
             //toast("Permissions Granted")
            fileUri = mUploadPhoto.openCamera(this,cameraResultLauncher)
            // mUploadPhoto.openCamera(cameraResultLauncher)
        } else {
            showOkDialog1(getString(R.string.camera_permission_rationale), this)
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
        validation1(binding.edtConclusion.text.toString(),binding.edtDistributorName.text.toString(),binding.edtDistributorMobile.text.toString())
    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this, getString(R.string.no_internet_connection))
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

        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            //toast("Permission granted in result")
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // getting network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (isGPSEnabled && isNetworkEnabled) {
                if(uriFilePath!=null) {
                    getLocation()
                }
            } else {
                enableLocationSettings()
            }
        } else {
            showOkDialog2(getString(R.string.location_permission_rationale), this)
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
                if(uriFilePath!=null) {
                    getLocation()
                }
            } else {
                enableLocationSettings()
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            );
        } else {

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val lat = location.latitude.toString()
                    val longi = location.longitude.toString()
                    Log.e("latitude", "Lat:" + lat + ", Long:" + longi)
                    val conclusion1: RequestBody = binding.edtConclusion.text.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val distributorName: RequestBody = binding.edtDistributorName.text.toString()
                        .toRequestBody("text/plain".toMediaTypeOrNull())
                    val distributorMobile: RequestBody =
                        binding.edtDistributorMobile.text.toString()
                            .toRequestBody("text/plain".toMediaTypeOrNull())
                    val meetingId1: RequestBody =
                        meetingId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val latitude1: RequestBody = lat.toRequestBody("text/plain".toMediaTypeOrNull())
                    val longitude1: RequestBody =
                        longi.toRequestBody("text/plain".toMediaTypeOrNull())
                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                    mViewModel.endMeeting(
                        mUploadPhoto.uploadFile(uriFilePath!!, "photo"),
                        meetingId1,
                        distributorName,
                        distributorMobile,
                        conclusion1,
                        latitude1,
                        longitude1
                    )
                } else {
                    //Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
                    getLocation()
                }
            }
        }
    }

    private fun handleImageRequest(file: File?) {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
            Toast.makeText(
                this,
                t.localizedMessage ?: getString(R.string.something_went_wrong_please_try_again_later),
                Toast.LENGTH_SHORT
            ).show()
        }

        /*GlobalScope.launch(Dispatchers.Main + exceptionHandler) {

            var imageUri1 = file?.toUri()
            queryImageUrl = imageUri1?.path!!
            queryImageUrl = compressImageFile(queryImageUrl, false, imageUri1!!)

            imageUri1 = Uri.fromFile(File(queryImageUrl))
            uriFilePath = Uri.fromFile(File(queryImageUrl))
            mUploadPhoto.setImage(imageUri1, this@EndMeeting1Activity, binding.ivCapturePhoto)
            Log.e("ImageURI","ImageURI ->"+imageUri1)
            Log.e("queryImageUrl","queryImageUrl ->"+queryImageUrl)


        }*/

        CoroutineScope(Dispatchers.Main + exceptionHandler).launch {
            var imageUri1 = file?.toUri()
            queryImageUrl = imageUri1?.path!!
            queryImageUrl = compressImageFile(queryImageUrl, false, imageUri1)
            imageUri1 = Uri.fromFile(File(queryImageUrl))
            uriFilePath = Uri.fromFile(File(queryImageUrl))
            mUploadPhoto.setImage(imageUri1, this@EndMeeting1Activity, binding.ivCapturePhoto)
            binding.pbLoading.visibility = View.GONE
        }

    }

}