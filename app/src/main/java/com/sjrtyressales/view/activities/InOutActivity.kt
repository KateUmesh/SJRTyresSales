package com.sjrtyressales.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sjrtyressales.R
import com.sjrtyressales.utils.toolbar

class InOutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_out)

        /**Toolbar*/
        toolbar(getString(R.string.in_out),true)
    }
}