package com.sjrtyressales.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityHistoryBinding
import com.sjrtyressales.databinding.ActivityMeetingDetailsBinding
import com.sjrtyressales.utils.*
import com.sjrtyressales.viewModels.activityViewModel.ViewModelHistory
import com.sjrtyressales.viewModels.activityViewModel.ViewModelMeetingDetails
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeetingDetailsActivity : AppCompatActivity(),SnackBarCallback {
   private lateinit var binding: ActivityMeetingDetailsBinding
    private lateinit var mViewModel: ViewModelMeetingDetails
    private var meetingId:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_meeting_details)

        /**Toolbar*/
        toolbar(getString(R.string.meeting_details),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelMeetingDetails::class.java]

        /**Get meetingId From HistoryActivity*/
        if(!intent.getStringExtra(Constant.meetingId).isNullOrEmpty()) {
            meetingId =intent.getStringExtra(Constant.meetingId)!!.toInt()
        }

        /**Check user is log in or not*/
        if(mViewModel.token.isNullOrEmpty()){
            logout(this)
        }else{
            /**Call viewMeetingDetails GET Api*/
            mViewModel.viewMeetingDetails(meetingId)

            /**Response of viewMeetingDetails GET Api*/
            mViewModel.MeetingDetailsResponse.observe(this, Observer {
                binding.includedLoader.llLoading.visibility = View.GONE
                if (it.status == "1") {
                    binding.llMeetingDetailsMain.visibility = View.VISIBLE
                    binding.data = it.data
                } else if (it.status == "0") {
                    if (it.data?.userActive == "0") {
                        logout(this)
                    }else{
                        snackBar(it.message,this)
                    }
                } else {
                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                    binding.llMeetingDetailsMain.visibility = View.GONE
                    showSnackBar(this, it.message)
                }

            })
        }
    }

    override fun snackBarSuccessInternetConnection() {
        mViewModel.viewMeetingDetails(meetingId)
    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }
}