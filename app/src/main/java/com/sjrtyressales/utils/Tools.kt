package com.sjrtyressales.utils

import android.app.Activity
import android.os.Build
import com.sjrtyressales.R

class Tools {

    companion object{


        fun setSystemBarColor(activity: Activity) {
            if (Build.VERSION.SDK_INT >= 21) {
                val window = activity.window
                window.addFlags(Int.MIN_VALUE)
                window.statusBarColor = activity.getColor(R.color.colorPrimaryDark)
            }
        }

    }
}