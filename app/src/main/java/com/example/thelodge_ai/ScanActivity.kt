package com.example.thelodge_ai

import FirestoreHelper
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private val firestoreHelper = FirestoreHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan) // Replace with your layout resource ID

        outputDirectory = outputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, Const.REQUIRED_PERMISSIONS, Const.REQUEST_CODE_PERMISSIONS
            )
        }

        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
            takePhoto()
        }
    }

    private fun outputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let { mFile ->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    detectProduct(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(Const.TAG, "onError: ${exception.message}", exception)
                }
            }
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Const.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                makeText(this, "Permissions not granted by user", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also { mPreview ->
                    mPreview.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(Const.TAG, "startCamera Failed:", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun allPermissionsGranted() =
        Const.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    fun uploadImageAsync(photoFile: File) {
        try {
            val client = OkHttpClient()

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", photoFile.name, RequestBody.create("image/jpeg".toMediaTypeOrNull(), photoFile))
                .build()

            val request = Request.Builder()
                .url("http://192.168.137.229:5000/upload")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    TODO("Not yet implemented")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        // Parse the JSON response to get the result
                        val result = parseResultFromJson(responseBody)

                        // Show a toast with the result
                        showToast(result)

                        firestoreHelper.addIngredient(
                            ingredient = result,
                            onSuccess = { newDocumentId ->
                                println("Successfully added $result with ID: $newDocumentId")
                            },
                            onFailure = { exception ->
                                println("Error adding $result: ${exception.message}")
                            }
                        )
                    } else {
                        // Handle the error case
                        showToast("Error: ${response.code}")
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle other exceptions specific to your use case
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun parseResultFromJson(json: String?): String {
        // Implement your JSON parsing logic here
        // For simplicity, assuming a straightforward JSON structure
        return try {
            val jsonObject = JSONObject(json)
            jsonObject.getString("result")
        } catch (e: JSONException) {
            // Handle JSON parsing exception
            ""
        }
    }


    private fun detectProduct(photoFile: File) {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                uploadImageAsync(photoFile)
            }
        }
    }


}
