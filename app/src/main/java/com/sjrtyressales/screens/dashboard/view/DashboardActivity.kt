package com.sjrtyressales.screens.dashboard.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityDashboardBinding
import com.sjrtyressales.utils.logout
import com.sjrtyressales.utils.showSnackBar
import com.sjrtyressales.utils.snackBar
import com.sjrtyressales.utils.toolbar
import com.sjrtyressales.screens.dashboard.viewModel.ViewModelDashboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity(),SnackBarCallback {
    lateinit var binding: ActivityDashboardBinding
    private lateinit var mViewModel: ViewModelDashboard
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_dashboard)

        /**Toolbar*/
        toolbar(getString(R.string.dashboard),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelDashboard::class.java]

        /**Check user is log in or not*/
        if(mViewModel.token.isNullOrEmpty()){
            logout(this)
        }else{

            /**Call dashboard GET api*/
            mViewModel.getDashboard()

            /**Response of dashboard GET api*/
            mViewModel.dashboardResponse.observe(this, Observer {
                binding.includedLoader.llLoading.visibility = View.GONE
                if (it.status == "1") {
                    if (it.data?.recent_meetings==null || it.data?.recent_meetings!!.isEmpty()) {
                        binding.llDashboardMain.visibility = View.GONE
                        binding.llDashboardNoData.visibility = View.VISIBLE
                    }else{
                        binding.llDashboardMain.visibility = View.VISIBLE
                        binding.llDashboardNoData.visibility = View.GONE
                        binding.dashboardData = it.data

                    }
                } else if (it.status == "0") {
                    if (it.data?.userActive == "0") {
                        logout(this)
                    }else{
                        snackBar(it.message,this)
                    }
                } else {
                    binding.includedLoader.llLoading.visibility = View.VISIBLE
                    binding.llDashboardMain.visibility = View.GONE
                    showSnackBar(this, it.message)
                }

            })

        }
    }

    override fun snackBarSuccessInternetConnection() {
        mViewModel.getDashboard()
    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }
}