package com.mariachizhova.testtask

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.mariachizhova.testtask.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    private val FILE_NAME = "photo.jpg"
    val APP_TAG = "Testtask"
    private lateinit var photoFile: File
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var totalNumber: Int = 0
    private var templateName: String = "myPicture_$totalNumber.jpg"
    private var inputTemplate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.numberView.text = totalNumber.toString()
        val input = getIntent().getSerializableExtra("inputTemplate")
        if (input != null) {
            inputTemplate = input.toString()
            templateName = inputTemplate + '_' + totalNumber + ".jpg"
        }
        mBinding.templateName.text = templateName
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleCameraImage(result.data)
                } else {
                    Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
                }
            }

        mBinding.buttonTakePicture.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)
            val fileProvider =
                FileProvider.getUriForFile(this, "com.mariachizhova.testtask.provider", photoFile)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
            try {
                resultLauncher.launch(cameraIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
            }
        }
        mBinding.buttonEditTemplate.setOnClickListener {
            val intent = Intent(this, EditTemplateActivity::class.java)
            startActivity(intent)
        }
    }

    fun getPhotoFile(fileName: String): File {
        val mediaStorageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG)
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory")
        }
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    private fun handleCameraImage(intent: Intent?) {
        val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)
        mBinding.imageView.setImageBitmap(takenImage)
        saveMediaToStorage(takenImage)
        totalNumber += 1
        mBinding.numberView.text = totalNumber.toString()
        if (inputTemplate == "") {
            templateName = "myPicture_$totalNumber.jpg"
        } else {
            templateName = inputTemplate + '_' + totalNumber + ".jpg"
        }
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        val filename = templateName
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                Log.d(APP_TAG, imageUri.toString())
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Saved to photos with name $templateName", Toast.LENGTH_SHORT).show()
        }
    }
}