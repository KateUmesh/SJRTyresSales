package com.sjrtyressales.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.sjrtyressales.R
import com.sjrtyressales.databinding.ActivityAllowanceBinding
import com.sjrtyressales.utils.toolbar

class AllowanceActivity : AppCompatActivity() {
    lateinit var binding:ActivityAllowanceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_allowance)

        /**Toolbar*/
        toolbar(getString(R.string.allowance),true)
    }
}