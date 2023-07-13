package com.sjrtyressales.screens.allowance.view

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
import com.sjrtyressales.databinding.ActivityAllowanceBinding
import com.sjrtyressales.screens.splashScreen.model.LocationEvent
import com.sjrtyressales.utils.*
import com.sjrtyressales.screens.allowance.viewModel.ViewModelAllowance
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.Subscribe
import java.io.File
import javax.inject.Inject
@AndroidEntryPoint
class AllowanceActivity : AppCompatActivity(),SnackBarCallback {
    lateinit var binding:ActivityAllowanceBinding
    @Inject
    lateinit var mUploadPhoto: UploadPhoto

    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var finalUri: Uri

    private lateinit var mViewModel: ViewModelAllowance

    private var uriFilePath: Uri? = null

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    var REQUEST_CODE_CHECK_SETTINGS=123

    private var latitude=""
    private var longitude=""
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var isGPSEnabled = false
    var isNetworkEnabled = false
    var locationManager:LocationManager? = null
    private var fileUri: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_allowance)

        /**Toolbar*/
        toolbar(getString(R.string.allowance),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelAllowance::class.java]

        /**Hide Loader*/
        binding.includedLoader.llLoading.visibility = View.GONE

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?

        /**Initialize Camera Result Launcher*/
        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                /*mUploadPhoto.setImage(fileUri?.toUri()!!, this, binding.ivCapturePhoto)
                Log.e("path",fileUri?.path!!)
                Log.e("toUri",(fileUri?.toUri()!!).toString())
                Log.e("absolutePath",fileUri?.absolutePath.toString())
                uriFilePath = ("file://"+ getFilePathFromUri(this,fileUri?.toUri()!!,false)).toUri()
                Log.e("uriFilePath","uriFilePath : - "+uriFilePath)*/
                //uriFilePath = ("file://"+ getFilePathFromUri(this,fileUri?.absolutePath!!,false)).toUri()
                //file:///data/user/0/com.sjrtyressales/cache/temp_file_.jpg
                try {
                    if (result?.data != null) {
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
                            Log.e("uriFilePath", "uriFilePath : - " + uriFilePath)
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
                            Log.e("uriFilePath", "uriFilePath : - " + uriFilePath)
                            mUploadPhoto.setImage(finalUri, this, binding.ivCapturePhoto)
                        }
                    }
                }catch(e:Exception){
                    e.printStackTrace()
                }

                //mUploadPhoto.launchImageCrop(imageToUploadUri,cropImage)
            }
        }

        /**Capture photo button click*/
        /*binding.btnCapturePhoto.setOnClickListener {
            mUploadPhoto.checkPermissionsToOpenCamera(this,requestMultiplePermissions,cameraResultLauncher)
        }*/

        binding.coCapturePhoto.setOnClickListener {
            //fileUri=mUploadPhoto.checkPermissionsToOpenCamera(this,requestMultiplePermissions,cameraResultLauncher)
            mUploadPhoto.checkPermissionsToOpenCamera(this,requestMultiplePermissions,cameraResultLauncher)
        }


        /**Submit button click*/
        binding.btnSubmit.setOnClickListener {
            hideKeyboard(binding.edtAmount)
            validation1(binding.edtTitle.text.toString(),binding.edtAmount.text.toString())
        }

        /**Response of allowance POST api*/
        mViewModel.AllowanceResponse.observe(this, Observer {
            binding.includedLoader.llLoading.visibility = View.GONE
            when(it.status){
                "1"->{
                    showOkToFinishDialog(getString(R.string.allowance),it.message,this)
                }
                "0"->{
                    if(it.data?.userActive=="0"){
                        logout(this)
                    }else {
                        snackBar(it.message, this)
                    }
                }
                else->{
                    showSnackBar(this,it.message)
                }
            }
        })
    }

    private fun validation(title:String,amount:String) {
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
    }

    private fun validation1(title:String,amount:String) {
        if(title.isEmpty()){
            snackBar(getString(R.string.title_required),this)
        }else  if(amount.isEmpty()){
            snackBar(getString(R.string.amount_required),this)
        }else if(uriFilePath==null){
            snackBar(getString(R.string.photo_required),this)
        }else{
            if (checkPermission()) {
                // getting GPS status
                isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                // getting network status
                isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if(isGPSEnabled&&isNetworkEnabled){
                    getLocation()
                }else{
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
            uriFilePath = ("file://"+result.getUriFilePath(this)).toUri()
            Log.e("uriFilePath",uriFilePath.toString())
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
            //fileUri=mUploadPhoto.openCamera(this,cameraResultLauncher)
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
        validation1(binding.edtTitle.text.toString(),binding.edtAmount.text.toString())
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
            //toast("Permission granted in result loc ")


            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // getting network status
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if(isGPSEnabled&&isNetworkEnabled){
                if(uriFilePath!=null) {
                    getLocation()
                }
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
                    val title1: RequestBody = binding.edtTitle.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val amount1: RequestBody = binding.edtAmount.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val latitude1: RequestBody = lat.toRequestBody("text/plain".toMediaTypeOrNull())
                    val longitude1: RequestBody = longi.toRequestBody("text/plain".toMediaTypeOrNull())
                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                    mViewModel.postAllowance(mUploadPhoto.uploadFile(uriFilePath!!,"photo"),title1,amount1,latitude1,longitude1)
                } else {
                    //Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
                    getLocation()
                }
            }
        }
    }

    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent){
        Log.e("Latitude", "LatitudeAllowance -> ${locationEvent.latitude}")
        Log.e("Longitude", "LongitudeAllowance -> ${locationEvent.longitude}")
        latitude = locationEvent.latitude!!.toString()
        longitude = locationEvent.longitude!!.toString()

    }

}