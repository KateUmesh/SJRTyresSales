package com.sjrtyressales.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.view.activities.HomeActivity
import com.sjrtyressales.view.activities.LoginActivity


fun Context.toast(message: CharSequence) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

fun callHomeActivity(activity:Activity) {
    val intent = Intent(activity, HomeActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
    activity.startActivity(intent)
    activity.finish()
}

fun logout(activity: Activity){
    val localSharedPreferences = LocalSharedPreferences(activity)
    localSharedPreferences.clear()
    callLoginActivity(activity)
}

 fun isEmailValid(email: CharSequence?): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email!!).matches()
}

fun snackBar(message: CharSequence, activity: Activity) {
    val snackBar = Snackbar.make(
        activity.findViewById(android.R.id.content),
        message,
        Snackbar.LENGTH_SHORT
    )

    snackBar.show()
}

fun hideKeyboard(view: View) {
    view.apply {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun showOkDialog(title: String,message: String, context: Context) {
    val builder =
        AlertDialog.Builder(context)
    builder.setTitle(title as CharSequence)
    builder.setMessage(message)
    builder.setPositiveButton(
        R.string.Ok
    ) { _, _ -> }
    builder.show()
}

fun showOkToFinishDialog(title: String,message: String, context: Context) {
    val builder =
        AlertDialog.Builder(context)
    builder.setTitle(title as CharSequence)
    builder.setMessage(message)
    builder.setPositiveButton(
        R.string.Ok
    ) { _, _ ->
        (context as Activity).finish()
    }
    builder.show()
}

fun Activity.showSnackBar(snackBarCallback: SnackBarCallback, message: CharSequence) {

    val snackBar = Snackbar.make(
        this.findViewById(android.R.id.content),
        message,
        Snackbar.LENGTH_INDEFINITE
    )
    snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.Yellow))
    snackBar.setAction(this.getString(R.string.retry)) {
        val networkConnection = NetworkConnection(applicationContext)
        if (networkConnection.isNetworkConnected()) {
            snackBarCallback.snackBarSuccessInternetConnection()
        } else {
            this.showSnackBar(
                snackBarCallback,
                this.getString(R.string.no_internet_connection)
            )
        }
        snackBar.dismiss()
    }
    snackBar.show()
}
