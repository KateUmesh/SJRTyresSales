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
import androidx.lifecycle.ViewModelProvider
import com.canhub.cropper.CropImageContract
import com.sjrtyressales.R
import com.sjrtyressales.callbacks.SnackBarCallback
import com.sjrtyressales.databinding.ActivityHomeBinding
import com.sjrtyressales.utils.UploadPhoto
import com.sjrtyressales.utils.logout
import com.sjrtyressales.utils.showSnackBar
import com.sjrtyressales.utils.toast
import com.sjrtyressales.viewModels.activityViewModel.ViewModelHome
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(),UploadPhoto.OnClickListener,SnackBarCallback {
    lateinit var binding : ActivityHomeBinding
    @Inject
    lateinit var mUploadPhoto: UploadPhoto
    private var profileImage=""
    var flag=0
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var finalUri: Uri
    private lateinit var mViewModel:ViewModelHome
    var privateProfileDialogue=1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_home)

        /**Initialize View Model*/
        mViewModel = ViewModelProvider(this)[ViewModelHome::class.java]

        /**Hide Loader*/
        binding.includeEditPhotoLoader.llLoadingEditPhoto.visibility = View.GONE


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

        /**Image Click*/
        binding.image.setOnClickListener {
            mUploadPhoto.uploadPhotoDialog1(this,this,profileImage,flag)
        }

        /**Initialize Gallery launcher*/
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == Activity.RESULT_OK){
                result?.data?.data?.let {  uri->
                    mUploadPhoto.launchImageCrop(uri,cropImage)
                }
            }

        }

        /**Initialize Camera Result Launcher*/
        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val photo: Bitmap = result.data?.extras?.get("data") as Bitmap
                val mImageName = "IMG_" + System.currentTimeMillis().toString()
                val imageToUploadUri=mUploadPhoto.saveImage(photo,mImageName,contentResolver)
                mUploadPhoto.launchImageCrop(imageToUploadUri,cropImage)
            }
        }

        /**Response of uploadProfilePhoto POST api*/
        mViewModel.profilePhotoResponse.observe(this) {
            when(it.status){
                "1"->{
                    toast(it.message)
                    binding.includeEditPhotoLoader.llLoadingEditPhoto.visibility = View.GONE
                    mUploadPhoto.setImage(finalUri, this, binding.image)
                    privateProfileDialogue=0
                    profileImage = finalUri.toString()
                    flag=1
                }
                "0"->{}
                else->{
                    showSnackBar(this,it.message)
                }
            }

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

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent!!
            finalUri = uriContent
            val uriFilePath = ("file://"+result.getUriFilePath(this)).toUri()
            binding.includeEditPhotoLoader.llLoadingEditPhoto.visibility = View.VISIBLE
            mViewModel.uploadProfilePhoto(mUploadPhoto.uploadFile(uriFilePath,"photo"))
        } else {
            // An error occurred.
            //val exception = result.error
        }
    }

    override fun onItemClick(item: String) {
        if(item=="1"){
            galleryLauncher.launch(mUploadPhoto.pickFromGallery())
        }else if(item=="2"){
            mUploadPhoto.checkPermissionsToOpenCamera(this,requestMultiplePermissions,cameraResultLauncher)
        }
    }

    override fun snackBarSuccessInternetConnection() {

    }

    override fun snackBarfFailInternetConnection() {
        showSnackBar(this,getString(R.string.no_internet_connection))
    }
}