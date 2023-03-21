package com.sjrtyressales.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.sjrtyressales.R
import com.sjrtyressales.databinding.ActivityHistoryBinding
import com.sjrtyressales.utils.toolbar

class HistoryActivity : AppCompatActivity() {
    lateinit var binding: ActivityHistoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_history)

        /**Toolbar*/
        toolbar(getString(R.string.history),true)
    }
}