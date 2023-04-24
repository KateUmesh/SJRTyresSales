package com.sjrtyressales.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.sjrtyressales.R
import com.sjrtyressales.databinding.ActivityProfileBinding
import com.sjrtyressales.utils.toolbar

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding:ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_profile)


        /**Toolbar*/
        toolbar(getString(R.string.profile),true)

        /**Hide Loader*/
        binding.includedLoader.llLoading.visibility = View.GONE

        /**Change password button clikc*/
        binding.llChangePassword.setOnClickListener {
            val intent= Intent(this,ChangePasswordActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }



    }
}