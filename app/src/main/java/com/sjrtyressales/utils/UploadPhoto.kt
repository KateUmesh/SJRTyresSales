package com.sjrtyressales.utils

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sjrtyressales.R
import com.sjrtyressales.databinding.DialogUploadProfilePhotoBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject

class UploadPhoto @Inject constructor() {

    fun checkPermissionsToOpenCamera(
        context: Context,
        requestMultiplePermissions: ActivityResultLauncher<Array<String>>,
        cameraResultLauncher: ActivityResultLauncher<Intent>
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) +
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) +
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) +
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                requestPermission1(requestMultiplePermissions)
            } else {
                openCamera(cameraResultLauncher)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) +
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) +
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                requestPermission(requestMultiplePermissions)
            } else {
                openCamera(cameraResultLauncher)
            }
        }
    }

    fun requestPermission(requestMultiplePermissions: ActivityResultLauncher<Array<String>>) {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    fun requestPermission1(requestMultiplePermissions: ActivityResultLauncher<Array<String>>) {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        )
    }

    fun openCamera(cameraResultLauncher: ActivityResultLauncher<Intent>) {
        val chooserIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultLauncher.launch(chooserIntent)
    }

    fun saveImage(bitmap: Bitmap, name: String, resolver: ContentResolver): Uri {
        var imageCollection: Uri? = null
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }else{
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
        val imageUri = resolver.insert(imageCollection,contentValues)
        try{
            val outputStream = resolver.openOutputStream(imageUri!!)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
            Objects.requireNonNull(outputStream)?.close()

        }catch(e:Exception){
            e.printStackTrace()
        }

        return imageUri!!
    }


    fun uploadPhotoDialog1(
        context: Context,
        onClickListener: OnClickListener,
        url: String?,
        flag: Int
    ) {
        val halfScreenHeight=(context.resources.displayMetrics.heightPixels/1.33).toInt()

        val view: DialogUploadProfilePhotoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_upload_profile_photo, null, false
        )


        val dialog = BottomSheetDialog(context)
        dialog.setContentView(view.root)

        val params = view.clUploadProfilePhoto.layoutParams

        params?.height = halfScreenHeight

        view.clUploadProfilePhoto.layoutParams = params

        view.includeEditPhotoLoader.llLoadingEditPhoto.visibility = View.GONE

        view.tvBottomSheetTitle.text = context.getString(R.string.profile_photo)

        if (flag == 1) {
            if (url != null) {
                val uri = url.toUri()
                setImage(uri, context, view.ivProfileEdit)
            }
        } else {
            if (!url.isNullOrEmpty()) {
                Glide.with(context)
                    .load(url)
                    .placeholder(R.color.grey_10)
                    .into(view.ivProfileEdit)
            }
        }



        view.llGalleryBottomSheet.setOnClickListener {
            onClickListener.onItemClick("1")

            dialog.dismiss()
        }
        view.llCameraBottomSheet.setOnClickListener {
            onClickListener.onItemClick("2")
            dialog.dismiss()
        }
        dialog.show()



    }

    fun setImage(uri: Uri, context: Context, imageView: ImageView) {
        Glide.with(context)
            .load(uri)
            .into(imageView)

    }

    fun pickFromGallery(): Intent {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            val photoPickerIntent = Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = "image/*"
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            return photoPickerIntent
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            return intent
        }
    }

    interface OnClickListener {
        fun onItemClick(item: String)
    }

    fun launchImageCrop(
        imageUri: Uri,
        cropImage: ActivityResultLauncher<CropImageContractOptions>
    ) {
        cropImage.launch(
            options(uri = imageUri) {
                setFixAspectRatio(true)
                setGuidelines(CropImageView.Guidelines.ON)
                setAspectRatio(1, 1)
                setCropShape(CropImageView.CropShape.RECTANGLE)
                setCropMenuCropButtonTitle("Done")
                setRequestedSize(640, 640, CropImageView.RequestSizeOptions.RESIZE_INSIDE)

            }
        )
    }

    fun uploadFile(uri: Uri, name: String): MultipartBody.Part {

        val originalFile = File(uri.path!!)
        val filePart = originalFile.asRequestBody("image/*".toMediaTypeOrNull())
        //val filePart: RequestBody = RequestBody.create("image/*".toMediaTypeOrNull(), originalFile)
        val file: MultipartBody.Part = MultipartBody.Part.createFormData(
            name,
            URLEncoder.encode(originalFile.name, "utf-8"),
            filePart
        )
        return file
    }
}