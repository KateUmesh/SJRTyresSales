package com.sjrtyressales.utils

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class UploadPhoto @Inject constructor() {

    private var fileUri: File? = null
    fun checkPermissionsToOpenCamera(
        context: Context,
        requestMultiplePermissions: ActivityResultLauncher<Array<String>>,
        cameraResultLauncher: ActivityResultLauncher<Intent>
    ):File? {

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
                //openCamera(cameraResultLauncher)
                openCamera(context,cameraResultLauncher)
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
               //openCamera(cameraResultLauncher)
                openCamera(context,cameraResultLauncher)
            }
        }
        return fileUri
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

    @Throws(IOException::class)
    fun saveImage1(bitmap: Bitmap, name: String, resolver: ContentResolver): Uri {
        val fos: OutputStream
        val imageUri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //val resolver: ContentResolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
            fos = resolver.openOutputStream(Objects.requireNonNull(imageUri))!!
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val image = File(imagesDir, "$name.jpg")
            imageUri = Uri.fromFile(image)
            fos = FileOutputStream(image)
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        Objects.requireNonNull(fos).close()
        return imageUri
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

    fun compressImage(file: File):Bitmap{
        val bitmap = BitmapFactory.decodeFile(file.path)
        try{
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
            Objects.requireNonNull(outputStream).close()

        }catch(e:Exception){
            e.printStackTrace()
        }

        return bitmap
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

    fun openCamera(context: Context,cameraResultLauncher: ActivityResultLauncher<Intent>):File? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var photoURI: Uri? = null
        try {
            fileUri = createImageFile(context)
            photoURI = FileProvider.getUriForFile(context,
                "com.sjrtyressales.fileProvider",
                fileUri!!
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        cameraResultLauncher.launch(intent)
        //startActivityForResult(intent, REQUEST_CODE_CAMERA_PICTURE)
        return fileUri
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File? {
        val timeStamp: String = SimpleDateFormat(
            "dd-MMM-yyyy hh:mm a",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "PROFILE_PICTURE_$timeStamp"
        val image: File
        val storageDir: File?
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storageDir = File(
                Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "Sjrtyressales"
            )
            if (!storageDir!!.exists()) {
                storageDir.mkdirs()
            }
            image = File(storageDir, "$imageFileName.jpg")
            image.createNewFile()
        } else {
            storageDir =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES + File.separator + "Sjrtyressales")
            image = File.createTempFile(imageFileName, ".jpg", storageDir)
            val currentPhotoPath = image.absolutePath
        }
        return image
    }
}