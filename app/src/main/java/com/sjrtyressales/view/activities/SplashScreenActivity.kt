package com.sjrtyressales.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.sjrtyressales.R
import com.sjrtyressales.utils.Constant
import com.sjrtyressales.utils.LocalSharedPreferences
import com.sjrtyressales.utils.callHomeActivity
import com.sjrtyressales.utils.callLoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    lateinit var localSharedPreferences: LocalSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        localSharedPreferences = LocalSharedPreferences(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if(localSharedPreferences.getStringValue(Constant.token).isNullOrEmpty()) {
                callLoginActivity(this)
            }else{
                callHomeActivity(this)
            }
        }, 1000)

    }
}