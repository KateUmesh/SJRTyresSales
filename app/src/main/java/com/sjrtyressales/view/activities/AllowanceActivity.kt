package com.sjrtyressales.view.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.sjrtyressales.R
import com.sjrtyressales.databinding.ActivityAllowanceBinding
import com.sjrtyressales.utils.UploadPhoto
import com.sjrtyressales.utils.toolbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class AllowanceActivity : AppCompatActivity() {
    lateinit var binding:ActivityAllowanceBinding
    @Inject
    lateinit var mUploadPhoto: UploadPhoto

    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_allowance)

        /**Toolbar*/
        toolbar(getString(R.string.allowance),true)

        /**Initialize Camera Result Launcher*/
        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val photo: Bitmap = result.data?.extras?.get("data") as Bitmap
                val mImageName = "IMG_" + System.currentTimeMillis().toString()
                val imageToUploadUri=mUploadPhoto.saveImage(photo,mImageName,contentResolver)

            }
        }

        binding.btnCapturePhoto.setOnClickListener {
            mUploadPhoto.checkPermissionsToOpenCamera(this,requestMultiplePermissions,cameraResultLauncher)
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            mUploadPhoto.openCamera(cameraResultLauncher)
        }else{
            showOkDialog1(getString(R.string.camera_permission_rationale),this)
        }
    }

    fun showOkDialog1(message: String, context: Context) {
        val builder =
            AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.allow_permission) as CharSequence)
        builder.setMessage(message)
        builder.setPositiveButton(
            R.string.Ok
        ) { _, _ ->

            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        builder.setNegativeButton(
            R.string.Cancel
        ) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        builder.show()
    }
}