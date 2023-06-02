package com.example.mystoryapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mystoryapp.AppPreferences
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityAddStoryBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var getFile: File? = null
    private lateinit var currentPhotoPath: String
    private var isLocationChecked = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
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

        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            isLocationChecked = isChecked
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
        val locationSwitch = binding.switchLocation.isChecked
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
                val file = reduceFileImage(getFile as File)
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
                    if (locationSwitch) {
                        getMyLocation()
                    } else {
                        addStoryViewModel.addStoryUser(
                            imageMultipart,
                            description,
                            token,
                            null,
                            null
                        )
                    }
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

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLocation()
                }
                else -> {
                    // No location access granted.
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            val desc = binding.edAddDescription.text.toString()
            val file = reduceFileImage(getFile as File)
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
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val lat = location.latitude
                        val lon = location.longitude
                        addStoryViewModel.addStoryUser(
                            imageMultipart,
                            description,
                            token,
                            lat,
                            lon
                        )
                    } else {
                        Toast.makeText(
                            this@AddStoryActivity,
                            "Location is not found. Try Again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this@AddStoryActivity,
                        "Failed to get location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > MAXIMAL_SIZE)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
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
        const val MAXIMAL_SIZE = 1000000
    }
}