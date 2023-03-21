package com.sjrtyressales.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.sjrtyressales.R
import com.sjrtyressales.databinding.ActivityMeetingsBinding
import com.sjrtyressales.utils.toolbar

class MeetingsActivity : AppCompatActivity() {
    lateinit var binding: ActivityMeetingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_meetings)

        /**Toolbar*/
        toolbar(getString(R.string.meetings),true)
    }
}