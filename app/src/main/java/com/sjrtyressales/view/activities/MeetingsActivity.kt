package com.sjrtyressales.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityMeetingsBinding
import com.sjrtyressales.model.ModelLoginRequest
import com.sjrtyressales.model.ModelStartMeetingRequest
import com.sjrtyressales.utils.*
import com.sjrtyressales.viewModels.activityViewModel.ViewModelMeetings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeetingsActivity : AppCompatActivity(),SnackBarCallback {
    private lateinit var binding: ActivityMeetingsBinding
    private lateinit var mViewModel:ViewModelMeetings
    private var latitude=""
    private var longitude=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_meetings)

        /**Toolbar*/
        toolbar(getString(R.string.meetings),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelMeetings::class.java]

        /**Hide Loader*/
        binding.includedLoader.llLoading.visibility = View.GONE

        /**Check user is log in or not*/
        if(mViewModel.token.isNullOrEmpty()){
            logout(this)
        }else{


            /**Submit button click*/
            binding.btnSubmit.setOnClickListener {
                hideKeyboard(binding.edtDistributorMobile)
                validation(binding.edtShopName.text.toString().trim(),binding.edtDistributorName.text.toString().trim(),binding.edtDistributorMobile.text.toString().trim(),latitude, longitude)
            }

            /**Response of StartMeeting POST api*/
            mViewModel.StartMeetingResponse.observe(this, Observer {
                binding.includedLoader.llLoading.visibility = View.GONE
                if (it.status == "1") {
                   showOkToFinishDialog(getString(R.string.meetings),it.message,this)
                } else if (it.status == "0") {
                    /*if (it.data?.userActive == "0") {
                        logout()
                    }*/
                } else {
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
            val mModelStartMeetingRequest = ModelStartMeetingRequest(shopName, distributorName,distributorMobile,latitude,longitude)
            mViewModel.postStartMeeting(mModelStartMeetingRequest)
        }
    }

    override fun snackBarSuccessInternetConnection() {

    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }
}