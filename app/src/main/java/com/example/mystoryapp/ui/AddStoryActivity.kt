package com.example.mystoryapp.ui

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import com.example.mystoryapp.AppPreferences
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityAddStoryBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var getFile: File? = null
    private lateinit var currentPhotoPath: String
    private val addStoryViewModel: AddStoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.add_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        addStoryViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        addStoryViewModel.message.observe(this) {
            showToast(it)
        }

        binding.buttonCamera.setOnClickListener {
            startTakePhoto()
        }

        binding.buttonGallery.setOnClickListener {
            startGallery()
        }

        binding.buttonAdd.setOnClickListener {
            uploadImage()
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            myFile.let { file ->
                getFile = file
                binding.imagePhoto.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                getString(R.string.package_name),
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri

            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@AddStoryActivity)
                getFile = myFile
                binding.imagePhoto.setImageURI(uri)
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private fun uploadImage() {
        val desc = binding.edAddDescription.text.toString()
        when {
            getFile == null -> {
                Toast.makeText(
                    this@AddStoryActivity,
                    getString(R.string.input_picture),
                    Toast.LENGTH_SHORT
                ).show()
            }
            desc.trim().isEmpty() -> {
                Toast.makeText(
                    this@AddStoryActivity,
                    getString(R.string.input_des),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val file = getFile as File
                val description = desc.toRequestBody("text/plain".toMediaType())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                val appPreferences = AppPreferences(this)
                val token = appPreferences.authToken
                if (token != null) {
                    addStoryViewModel.addStoryUser(imageMultipart, description, token)
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            this@AddStoryActivity,
            StringBuilder(getString(R.string.message)).append(message),
            Toast.LENGTH_SHORT
        ).show()

        if (message == "Story created successfully") {
            startActivity(Intent(this@AddStoryActivity, StoryActivity::class.java))
            finish()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val FILENAME_FORMAT = "dd-MMM-yyyy"
    }
}