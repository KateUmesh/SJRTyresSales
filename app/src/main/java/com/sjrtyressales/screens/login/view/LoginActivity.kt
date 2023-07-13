package com.sjrtyressales.screens.login.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityLoginBinding
import com.sjrtyressales.screens.login.model.ModelLoginRequest
import com.sjrtyressales.utils.*
import com.sjrtyressales.screens.login.viewModel.ViewModelLogin
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(),SnackBarCallback {
    lateinit var mViewModel: ViewModelLogin
    lateinit var binding:ActivityLoginBinding
    private var inTimeDialog ="0"
    private var inTimeDialogMessage ="0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelLogin::class.java]

        /**Hide Loader*/
        binding.includedLoader.llLoading.visibility = View.GONE

        /**Get inTimeDialog from*/
        /*if(!intent.getStringExtra(Constant.inTimeDialog).isNullOrEmpty()){
           inTimeDialog =  intent.getStringExtra(Constant.inTimeDialog)!!
        }
        if(!intent.getStringExtra(Constant.inTimeDialogMessage).isNullOrEmpty()){
            inTimeDialogMessage =  intent.getStringExtra(Constant.inTimeDialogMessage)!!
        }*/

        /**FAB button click*/
        binding.fab.setOnClickListener {
            hideKeyboard(binding.edtPassword)
            validation()
        }

        /**Response of login POST api*/
        mViewModel.loginResponse.observe(this, Observer {
            binding.includedLoader.llLoading.visibility = View.GONE
            Log.e("status",it.status)
            when(it.status){
                "1"->{
                    mViewModel.putToken(it.data?.token!!)
                    callHomeActivity(this,it.data?.inTimeDialog!!,it.data?.inTimeDialogMessage!!)
                }
                "0"->{
                    showOkDialog(getString(R.string.login), it.message, this)
                }
                else->{
                    showSnackBar(this,it.message)
                }
            }
        })
    }

    fun validation(){
        if(binding.edtEmail.text.toString().isEmpty()&& !isEmailValid(binding.edtEmail.text.toString())){
            snackBar(getString(R.string.invalid_email_address),this)
        }else if(binding.edtPassword.text.toString().isEmpty()){
            snackBar(getString(R.string.password_required),this)
        }else{
            binding.includedLoader.llLoading.visibility = View.VISIBLE
            val mModelLoginRequest = ModelLoginRequest(binding.edtEmail.text.toString(), binding.edtPassword.text.toString())
            mViewModel.postLogin(mModelLoginRequest)
        }
    }

    override fun snackBarSuccessInternetConnection() {
        validation()
    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }
}