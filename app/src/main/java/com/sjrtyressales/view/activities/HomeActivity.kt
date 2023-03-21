package com.sjrtyressales.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.sjrtyressales.R
import com.sjrtyressales.databinding.ActivityHomeBinding
import com.sjrtyressales.utils.logout

class HomeActivity : AppCompatActivity() {
    lateinit var binding : ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_home)

        /**In Out*/
        binding.fabInOut.setOnClickListener {
            val intent= Intent(this,InOutActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**Meetings*/
        binding.fabMeetings.setOnClickListener {
            val intent= Intent(this,MeetingsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**Dashboard*/
        binding.fabDashboard.setOnClickListener {
            val intent= Intent(this,DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**History*/
        binding.fabHistory.setOnClickListener {
            val intent= Intent(this,HistoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**Allowance*/
        binding.fabAllowance.setOnClickListener {
            val intent= Intent(this,AllowanceActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        /**Logout*/
        binding.fabLogout.setOnClickListener {
            showLogoutAlertDialog()
        }
    }


    fun showLogoutAlertDialog() {
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.logout) as CharSequence)
        builder.setMessage("Are you sure? Do you want to logout?")
        builder.setPositiveButton(
            R.string.logout
        ) { _, _ ->
            logout(this)
        }
        builder.setNegativeButton(
            R.string.cancel
        ) { _, _ ->

        }
        builder.show()
    }
}