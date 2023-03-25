package com.sjrtyressales.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityLoginBinding
import com.sjrtyressales.model.ModelLoginRequest
import com.sjrtyressales.utils.*
import com.sjrtyressales.viewModels.activityViewModel.ViewModelLogin
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(),SnackBarCallback {
    lateinit var mViewModel:ViewModelLogin
    lateinit var binding:ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelLogin::class.java]

        /**Hide Loader*/
        binding.includedLoader.llLoading.visibility = View.GONE

        /**FAB button click*/
        binding.fab.setOnClickListener {
            hideKeyboard(binding.edtPassword)
            validation()
        }

        /**Response of login POST api*/
        mViewModel.loginResponse.observe(this, Observer {
            binding.includedLoader.llLoading.visibility = View.GONE
            when(it.status){
                "1"->{
                    mViewModel.putToken(it.data?.token!!)
                    callHomeActivity(this)
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
        if(binding.edtEmail.text.toString().isNullOrEmpty()&& !isEmailValid(binding.edtEmail.text.toString())){
            snackBar(getString(R.string.invalid_email_address),this)
        }else if(binding.edtPassword.text.toString().isNullOrEmpty()){
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