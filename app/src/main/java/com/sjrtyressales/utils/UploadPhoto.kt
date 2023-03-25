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
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
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
}