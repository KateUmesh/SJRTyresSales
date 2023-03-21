package com.sjrtyressales.utils

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.sjrtyressales.R
import com.sjrtyressales.view.activities.LoginActivity

fun AppCompatActivity.toolbar(title: String, backArrow:Boolean)
{
    val toolbar = this.findViewById<View>(R.id.toolbar) as Toolbar
    this.setSupportActionBar(toolbar)
    this.supportActionBar!!.title = title
    this.supportActionBar!!.setDisplayHomeAsUpEnabled(backArrow)
    Tools.setSystemBarColor(this)
    toolbar.setNavigationOnClickListener { this.onBackPressed() }
}

fun callLoginActivity(activity:Activity) {
    val intent = Intent(activity, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    activity.startActivity(intent)
    activity.finish()
}

fun logout(activity: Activity){
    //val localSharedPreferences = LocalSharedPreferences(activity)
    //localSharedPreferences.clear()
    callLoginActivity(activity)
}