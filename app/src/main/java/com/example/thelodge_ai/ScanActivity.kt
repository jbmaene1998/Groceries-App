package com.example.thelodge_ai

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    private val url = "http://localhost:5000/upload"
    private lateinit var cameraPreview: SurfaceView
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        cameraPreview = findViewById(R.id.cameraPreview)
        val scanButton: Button = findViewById(R.id.scanButton)

        // Request camera permissions and initialize cameraExecutor
        cameraExecutor = Executors.newSingleThreadExecutor()

        scanButton.setOnClickListener {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // CameraProvider is now guaranteed to be available
            val cameraProvider = cameraProviderFuture.get()

            // Set up Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraPreview.holder.surfaceProvider)
            }

            // Set up ImageCapture
            val imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind any existing use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // Set up image capture listener
                imageCapture.takePicture(
                    ContextCompat.getMainExecutor(this),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            // Handle the captured image here
                            val imageFile = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

                            // Save the image to the file
                            val savedUri = imageFile.toUri()
                            val msg = "Photo capture succeeded: $savedUri"
                            Log.d("CameraX", msg)

                            // Upload the image using AsyncTask
                            PostImageRequestTask().execute(imageFile)

                            super.onCaptureSuccess(image)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            // Handle capture error here
                            Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                            super.onError(exception)
                        }
                    }
                )

            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class PostImageRequestTask : AsyncTask<File, Void, Void>() {
        override fun doInBackground(vararg params: File): Void? {
            // Modify this function to handle image upload
            makePostRequest(params[0])
            return null
        }
    }

    private fun makePostRequest(imageFile: File) {
        try {
            val url = URL(url)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true

            val outputStream: OutputStream = connection.outputStream
            val imageBytes = imageFile.readBytes()
            outputStream.write(imageBytes)
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            Log.d("PostRequest", "Response Code: $responseCode")

            val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
            val response = inputStream.use(BufferedReader::readText)

            Log.d("PostRequest", "Response: $response")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Successfully connected
                Log.d("PostRequest", "POST request successful")
            } else {
                // Handle the error
                Log.e("PostRequest", "POST request failed")
            }

            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PostRequest", "Exception: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
