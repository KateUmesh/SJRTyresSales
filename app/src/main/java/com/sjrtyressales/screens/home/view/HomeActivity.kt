package com.sjrtyressales.screens.home.view

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
import android.os.Bundle
import android.provider.Settings
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
import com.canhub.cropper.CropImageContract
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityHomeBinding
import com.sjrtyressales.screens.allowance.view.AllowanceActivity
import com.sjrtyressales.screens.dashboard.view.DashboardActivity
import com.sjrtyressales.screens.history.view.HistoryActivity
import com.sjrtyressales.utils.*
import com.sjrtyressales.screens.inOut.view.InOutActivity
import com.sjrtyressales.screens.meetings.view.MeetingsActivity
import com.sjrtyressales.screens.profile.view.ProfileActivity
import com.sjrtyressales.screens.home.viewModel.ViewModelHome
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), UploadPhoto.OnClickListener, SnackBarCallback {
    lateinit var binding: ActivityHomeBinding

    @Inject
    lateinit var mUploadPhoto: UploadPhoto
    private var profileImage = ""
    var flag = 0
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var finalUri: Uri
    private lateinit var mViewModel: ViewModelHome
    var privateProfileDialogue = 1
    var callback = "0"
    private var inTimeDialog = "0"
    private var inTimeDialogMessage = "0"

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    var REQUEST_CODE_CHECK_SETTINGS = 123

    private var latitude = 0.0
    private var longitude = 0.0

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var isGPSEnabled = false
    var isNetworkEnabled = false
    var locationManager: LocationManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)


        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelHome::class.java]

        /**Hide Loader*/
        binding.includeEditPhotoLoader.llLoadingEditPhoto.visibility = View.GONE

        /**Get inTimeDialog from*/
        if (!intent.getStringExtra(Constant.inTimeDialog).isNullOrEmpty()) {
            inTimeDialog = intent.getStringExtra(Constant.inTimeDialog)!!
        }
        if (!intent.getStringExtra(Constant.inTimeDialogMessage).isNullOrEmpty()) {
            inTimeDialogMessage = intent.getStringExtra(Constant.inTimeDialogMessage)!!
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?

        if (inTimeDialog == "1") {
            showUpdateInTimeDialog(inTimeDialogMessage)
        }


        /**In Out*/
        binding.fabInOut.setOnClickListener {
            val intent = Intent(this, InOutActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**Meetings*/
        binding.fabMeetings.setOnClickListener {
            val intent = Intent(this, MeetingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**Dashboard*/
        binding.fabDashboard.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**History*/
        binding.fabHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**Allowance*/
        binding.fabAllowance.setOnClickListener {
            val intent = Intent(this, AllowanceActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**Allowance*/
        binding.fabProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**Logout*/
        binding.fabLogout.setOnClickListener {
            showLogoutAlertDialog()
        }

        /**Image Click*/
        binding.image.setOnClickListener {
            mUploadPhoto.uploadPhotoDialog1(this, this, profileImage, flag)
        }

        /**Initialize Gallery launcher*/
        galleryLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result?.data?.data?.let { uri ->
                        mUploadPhoto.launchImageCrop(uri, cropImage)
                    }
                }

            }

        /**Initialize Camera Result Launcher*/
        cameraResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val photo: Bitmap = result.data?.extras?.get("data") as Bitmap
                    val mImageName = "IMG_" + System.currentTimeMillis().toString()
                    val imageToUploadUri =
                        mUploadPhoto.saveImage(photo, mImageName, contentResolver)
                    mUploadPhoto.launchImageCrop(imageToUploadUri, cropImage)
                }
            }

        /**Response of uploadProfilePhoto POST api*/
        mViewModel.profilePhotoResponse.observe(this) {
            binding.includedLoader.llLoading.visibility = View.GONE
            binding.includeEditPhotoLoader.llLoadingEditPhoto.visibility = View.GONE
            when (it.status) {
                "1" -> {
                    toast(it.message)
                    mUploadPhoto.setImage(finalUri, this, binding.image)
                    privateProfileDialogue = 0
                    profileImage = finalUri.toString()
                    flag = 1
                }
                "0" -> {
                    if (it.data?.userActive == "0") {
                        logout(this)
                    } else {
                        snackBar(it.message, this)
                    }
                }
                else -> {
                    callback = it.status
                    showSnackBar(this, it.message)
                }
            }

        }

        /**Call myProfile GET api*/
        mViewModel.myProfile()

        /**Response of myProfile GET api*/
        mViewModel.MyProfileResponse.observe(this, Observer {
            binding.includedLoader.llLoading.visibility = View.GONE
            when (it.status) {
                "1" -> {
                    binding.nsHome.visibility = View.VISIBLE
                    binding.data = it.data
                    flag = 0
                    profileImage = it.data?.sales_employee?.profile_image!!
                }
                "0" -> {
                    if (it.data?.userActive == "0") {
                        logout(this)
                    } else {
                        snackBar(it.message, this)
                    }
                }
                else -> {
                    callback = it.status
                    showSnackBar(this, it.message)
                }
            }
        })

        /**Response of submitInTime or submitOutTime GET api*/
        mViewModel.SubmitInOutTimeResponse.observe(this) {
            binding.includedLoader.llLoading.visibility = View.GONE
            when (it.status) {
                "1" -> {
                    inTimeDialog = "0"
                    showUpdateInTimeOkDialog(it.message)
                }
                "0" -> {
                    if (it.data?.userActive == "0") {
                        logout(this)
                    } else {
                        snackBar(it.message, this)
                    }
                }
                else -> {
                    callback = it.status
                    showSnackBar(this, it.message)
                }
            }
        }
    }

    private fun showUpdateInTimeDialog(message: String) {
        val builder =
            AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle(getString(R.string.mark_in_time) as CharSequence)
        builder.setMessage(message)
        builder.setPositiveButton(getString(R.string.mark_in_time)) { dialogInterface, _ ->
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
            dialogInterface.dismiss()
        }
        builder.show()
    }

    private fun showUpdateInTimeOkDialog(message: String) {
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.app_name) as CharSequence)
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(
            R.string.ok
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        builder.show()
    }


    fun showLogoutAlertDialog() {
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.logout) as CharSequence)
        builder.setMessage("Are you sure? Do you want to logout?")
        builder.setPositiveButton(
            R.string.logout
        ) { _, _ ->
            logout(this)
        }
        builder.setNegativeButton(
            R.string.cancel
        ) { _, _ ->

        }
        builder.show()
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            mUploadPhoto.openCamera(cameraResultLauncher)
        } else {
            showOkDialog1(getString(R.string.camera_permission_rationale), this)
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
        }

        builder.show()
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

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent!!
            finalUri = uriContent
            val uriFilePath = ("file://" + result.getUriFilePath(this)).toUri()
            binding.includeEditPhotoLoader.llLoadingEditPhoto.visibility = View.VISIBLE
            mViewModel.uploadProfilePhoto(mUploadPhoto.uploadFile(uriFilePath, "photo"))
        } else {
            // An error occurred.
            //val exception = result.error
        }
    }

    override fun onItemClick(item: String) {
        if (item == "1") {
            galleryLauncher.launch(mUploadPhoto.pickFromGallery())
        } else if (item == "2") {
            mUploadPhoto.checkPermissionsToOpenCamera(
                this,
                requestMultiplePermissions,
                cameraResultLauncher
            )
        }
    }

    override fun snackBarSuccessInternetConnection() {
        if (callback == "2") {
            mViewModel.myProfile()
        } else {

        }
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
                getLocation()
            } else {
                enableLocationSettings()
            }
        } else {
            showOkDialog2(getString(R.string.location_permission_rationale), this)
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


    fun getLocation() {
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
                    val lat = location.latitude
                    val longi = location.longitude
                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                    mViewModel.submitInTime(lat, longi)
                } else {
                    //Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
                    getLocation()
                }
            }

        }
    }


}