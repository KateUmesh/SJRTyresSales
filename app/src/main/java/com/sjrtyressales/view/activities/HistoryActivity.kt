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
import com.sjrtyressales.utils.logout
import com.sjrtyressales.utils.showSnackBar
import com.sjrtyressales.utils.snackBar
import com.sjrtyressales.utils.toolbar
import com.sjrtyressales.viewModels.activityViewModel.ViewModelDashboard
import com.sjrtyressales.viewModels.activityViewModel.ViewModelHistory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryActivity : AppCompatActivity(),SnackBarCallback {
    lateinit var binding: ActivityHistoryBinding
    private lateinit var mViewModel: ViewModelHistory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_history)

        /**Toolbar*/
        toolbar(getString(R.string.history),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelHistory::class.java]

        /**Check user is log in or not*/
        if(mViewModel.token.isNullOrEmpty()){
            logout(this)
        }else{

            /**Call dashboard GET api*/
            mViewModel.getHistory()

            /**Response of dashboard GET api*/
            mViewModel.HistoryResponse.observe(this, Observer {
                binding.includedLoader.llLoading.visibility = View.GONE
                if (it.status == "1") {
                    if (it.data?.meeting_history==null || it.data?.meeting_history!!.isEmpty()) {
                        binding.llHistoryMain.visibility = View.GONE
                        binding.llHistoryNoData.visibility = View.VISIBLE
                    }else{
                        binding.llHistoryMain.visibility = View.VISIBLE
                        binding.llHistoryNoData.visibility = View.GONE
                        binding.historyData = it.data

                    }
                } else if (it.status == "0") {
                    if (it.data?.userActive == "0") {
                        logout(this)
                    }else{
                        snackBar(it.message,this)
                    }
                } else {
                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                    binding.llHistoryMain.visibility = View.GONE
                    showSnackBar(this, it.message)
                }

            })

        }
    }

    override fun snackBarSuccessInternetConnection() {
        mViewModel.getHistory()
    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }
}