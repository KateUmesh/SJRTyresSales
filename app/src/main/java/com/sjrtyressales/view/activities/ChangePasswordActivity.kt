package com.sjrtyressales.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityChangePasswordBinding
import com.sjrtyressales.model.ModelChangePasswordRequest
import com.sjrtyressales.utils.*
import com.sjrtyressales.viewModels.activityViewModel.ViewModelChangePassword
import com.sjrtyressales.viewModels.activityViewModel.ViewModelEndMeeting
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity(),SnackBarCallback {
    private lateinit var binding:ActivityChangePasswordBinding
    private lateinit var mViewModel:ViewModelChangePassword
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =DataBindingUtil.setContentView(this,R.layout.activity_change_password)

        /**Toolbar*/
        toolbar(getString(R.string.change_password),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelChangePassword::class.java]

        /**Hide Loader*/
        binding.includedLoader.llLoading.visibility = View.GONE

        /**Button Change Password click*/
        binding.btnChangePassword.setOnClickListener {
            hideKeyboard(binding.edtNewPassword)
            validation()
        }

        /**Response of changePassword POST api*/
        mViewModel.ChangePasswordResponse.observe(this, Observer {
            binding.includedLoader.llLoading.visibility=View.GONE
            when(it.status){
                "1"->{
                    showOkToFinishDialog(getString(R.string.change_password),it.message,this)
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

    private fun validation(){
        if(binding.edtNewPassword.text.toString().isEmpty()){
            snackBar(getString(R.string.new_password_required),this)
        }else{
            binding.includedLoader.llLoading.visibility = View.VISIBLE
            val mModelChangePasswordRequest = ModelChangePasswordRequest(binding.edtNewPassword.text.toString())
            mViewModel.changePassword(mModelChangePasswordRequest)
        }
    }

    override fun snackBarSuccessInternetConnection() {
        validation()
    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }
}