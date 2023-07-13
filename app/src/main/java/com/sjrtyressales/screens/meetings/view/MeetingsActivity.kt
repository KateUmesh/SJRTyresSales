package com.sjrtyressales.screens.meetings.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityMeetingsBinding
import com.sjrtyressales.screens.meetings.model.ModelStartMeetingRequest
import com.sjrtyressales.screens.endMeeting.view.EndMeeting1Activity
import com.sjrtyressales.utils.*
import com.sjrtyressales.screens.meetings.viewModel.ViewModelMeetings
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MeetingsActivity : AppCompatActivity(),SnackBarCallback {
    private lateinit var binding: ActivityMeetingsBinding
    private lateinit var mViewModel: ViewModelMeetings
    private var latitude=""
    private var longitude=""
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    var REQUEST_CODE_CHECK_SETTINGS=123
    var callback="0"
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var isGPSEnabled = false
    var isNetworkEnabled = false
    var locationManager: LocationManager? = null

    var type = ""
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_meetings)


        /**Toolbar*/
        toolbar(getString(R.string.meetings),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelMeetings::class.java]

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager?


        /**Check user is log in or not*/
        if(mViewModel.token.isNullOrEmpty()){
            logout(this)
        }else{

            /**Call checkMeetingNotEnd GET Api*/
            mViewModel.checkMeetingNotEnd()

            /**Response of checkMeetingNotEnd GET Api*/
            mViewModel.CheckMeetingNotEndResponse.observe(this, Observer {
                binding.includedLoader.llLoading.visibility = View.GONE
                when(it.status){
                    "1"->{
                       if(it.data?.meeting==null){
                           binding.llMeeting1.visibility=View.VISIBLE
                           binding.llMeeting2.visibility=View.GONE
                       }else{
                           binding.llMeeting2.visibility=View.VISIBLE
                           binding.llMeeting1.visibility=View.GONE
                           binding.data = it.data
                           val meetingId = it.data?.meeting?.id_meeting
                           binding.btnEndMeeting.setOnClickListener {
                               val intent = Intent(this, EndMeeting1Activity::class.java)
                               intent.putExtra(Constant.meetingId, meetingId)
                               startActivity(intent)
                               finish()
                           }
                       }
                    }
                    "0"->{
                        if (it.data?.userActive == "0") {
                            logout(this)
                        }else{
                            snackBar(it.message,this)
                        }
                    }
                    else->{
                        callback = it.status
                        showSnackBar(this,it.message)
                    }
                }
            })


            /**Select Type*/
            binding.edtType.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    openSingleChoiceDialog(1)
                }
                true
            }


            /**Submit button click*/
            binding.btnCreateMeeting.setOnClickListener {
                hideKeyboard(binding.edtDistributorMobile)
                validation1()
            }


            /**Response of StartMeeting POST api*/
            mViewModel.StartMeetingResponse.observe(this, Observer {
                binding.includedLoader.llLoading.visibility = View.GONE
                if (it.status == "1") {
                   showOkToFinishDialog(getString(R.string.meetings),it.message,this)
                } else if (it.status == "0") {
                    if (it.data?.userActive == "0") {
                        logout(this)
                    }else{
                        snackBar(it.message,this)
                    }
                } else {
                    callback = it.status
                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                    showSnackBar(this, it.message)
                }

            })

        }
    }

    fun validation(shopName:String,distributorName:String,distributorMobile:String,latitude:String,longitude:String){
        if(shopName.isEmpty()){
            snackBar(getString(R.string.shop_name_required),this)
        }else if(distributorName.isEmpty()){
            snackBar(getString(R.string.distributor_name_required),this)
        }else if(distributorMobile.length<10){
            snackBar(getString(R.string.distributor_mobile_required),this)
        }else{
            binding.includedLoader.llLoading.visibility = View.VISIBLE
           // val mModelStartMeetingRequest = ModelStartMeetingRequest(shopName, distributorName,distributorMobile,latitude,longitude)
           // mViewModel.postStartMeeting(mModelStartMeetingRequest)
        }
    }
    fun validation1(){
        if(binding.edtType.text.toString().isEmpty()){
            snackBar(getString(R.string.type_required),this)
        }else if(binding.edtShopName.text.toString().isEmpty()){
            snackBar(getString(R.string.shop_name_required),this)
        }else{
            if (checkPermission()) {
                // getting GPS status
                isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                // getting network status
                isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                //toast("Permission granted in result")
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

    override fun snackBarSuccessInternetConnection() {
        if(callback=="2"){
            /*validation(binding.edtShopName.text.toString().trim(),
                binding.edtDistributorName.text.toString().trim(),
                binding.edtDistributorMobile.text.toString().trim(),
                latitude, longitude)*/
            validation1()
        }else{
            mViewModel.checkMeetingNotEnd()
        }
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
            showOkDialog1(getString(R.string.location_permission_rationale),this)
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
                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                    val mModelStartMeetingRequest = ModelStartMeetingRequest(type,
                        binding.edtShopName.text.toString().trim(),
                        lat,
                        longi)
                    mViewModel.postStartMeeting(mModelStartMeetingRequest)
                } else {
                    //Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
                    getLocation()
                }
            }

        }
    }

    private fun openSingleChoiceDialog(flag: Int){
        when(flag){
            1->{
                editTextClearFocus()
                val listItems = resources.getStringArray(R.array.type_array)
                singleChoiceAlertDialog(listItems,type,1,getString(R.string.select_type))
            }

        }
    }

    private fun editTextClearFocus(){
        binding.edtShopName.clearFocus()
        hideKeyboard(binding.edtShopName)
    }

    private fun singleChoiceAlertDialog(values:Array<String>,id:String,flag:Int,title:String){
        val keys = ArrayList<String>()
        if(flag==4){
            keys.addAll(values)
        }else {
            for (i in values.indices) {
                keys.add((i + 1).toString())
            }
        }

        val mBuilder = android.app.AlertDialog.Builder(this)
        mBuilder.setTitle(title)
        mBuilder.setSingleChoiceItems(values, keys.indexOf(id)) { dialogInterface, i ->

            when(flag){
                1->{
                    type = (i+1).toString()
                    binding.edtType.setText(values[i])
                }
            }
            dialogInterface.dismiss()
        }

        val mDialog = mBuilder.create()
        mDialog.show()
    }
}