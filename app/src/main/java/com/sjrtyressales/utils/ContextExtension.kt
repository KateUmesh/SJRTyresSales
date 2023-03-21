package com.sjrtyressales.utils

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.sjrtyressales.R

fun AppCompatActivity.toolbar(title: String, backArrow:Boolean)
{
    val toolbar = this.findViewById<View>(R.id.toolbar) as Toolbar
    this.setSupportActionBar(toolbar)
    this.supportActionBar!!.title = title
    this.supportActionBar!!.setDisplayHomeAsUpEnabled(backArrow)
    Tools.setSystemBarColor(this)
    toolbar.setNavigationOnClickListener { this.onBackPressed() }
}