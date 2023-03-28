package com.sjrtyressales.view.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.CropImageContract
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityAllowanceBinding
import com.sjrtyressales.utils.*
import com.sjrtyressales.viewModels.activityViewModel.ViewModelAllowance
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
@AndroidEntryPoint
class AllowanceActivity : AppCompatActivity(),SnackBarCallback {
    lateinit var binding:ActivityAllowanceBinding
    @Inject
    lateinit var mUploadPhoto: UploadPhoto

    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var finalUri: Uri

    private lateinit var mViewModel: ViewModelAllowance

    private var uriFilePath: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_allowance)

        /**Toolbar*/
        toolbar(getString(R.string.allowance),true)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelAllowance::class.java]

        /**Hide Loader*/
        binding.includedLoader.llLoading.visibility = View.GONE

        /**Initialize Camera Result Launcher*/
        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val photo: Bitmap = result.data?.extras?.get("data") as Bitmap
                val mImageName = "IMG_" + System.currentTimeMillis().toString()
                val imageToUploadUri=mUploadPhoto.saveImage(photo,mImageName,contentResolver)
                mUploadPhoto.launchImageCrop(imageToUploadUri,cropImage)
            }
        }

        /**Capture photo button click*/
        binding.btnCapturePhoto.setOnClickListener {
            mUploadPhoto.checkPermissionsToOpenCamera(this,requestMultiplePermissions,cameraResultLauncher)
        }

        /**Submit button click*/
        binding.btnSubmit.setOnClickListener {
            hideKeyboard(binding.edtAmount)
            validation(binding.edtTitle.text.toString(),binding.edtAmount.text.toString())
        }

        /**Response of allowance POST api*/
        mViewModel.AllowanceResponse.observe(this, Observer {
            binding.includedLoader.llLoading.visibility = View.GONE
            when(it.status){
                "1"->{
                    showOkToFinishDialog(getString(R.string.allowance),it.message,this)
                }
                "0"->{
                    snackBar(it.message,this)
                }
                else->{
                    showSnackBar(this,it.message)
                }
            }
        })
    }

    private fun validation(title:String,amount:String) {
        if(title.isEmpty()){
            snackBar(getString(R.string.title_required),this)
        }else  if(amount.isEmpty()){
            snackBar(getString(R.string.amount_required),this)
        }else if(uriFilePath==null){
            snackBar(getString(R.string.photo_required),this)
        }else{
            val title1: RequestBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val amount1: RequestBody = amount.toRequestBody("text/plain".toMediaTypeOrNull())
            binding.includedLoader.llLoading.visibility = View.VISIBLE
            mViewModel.postAllowance(mUploadPhoto.uploadFile(uriFilePath!!,"photo"),title1,amount1)
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent!!
            finalUri = uriContent
            uriFilePath = ("file://"+result.getUriFilePath(this)).toUri()
        } else {
            // An error occurred.
            //val exception = result.error
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

    override fun snackBarSuccessInternetConnection() {
        validation(binding.edtTitle.text.toString(),binding.edtAmount.text.toString())
    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }
}