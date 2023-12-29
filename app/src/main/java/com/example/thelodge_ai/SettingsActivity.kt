package com.example.thelodge_ai

import AppPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SettingsActivity : AppCompatActivity() {

    private val url = "http://192.168.137.229:8080/toggle_led"
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        appPreferences = AppPreferences.getInstance(applicationContext)

        val toggleButton: ToggleButton = findViewById(R.id.toggleButton)
        toggleButton.isChecked = appPreferences.isFahrenheit

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            appPreferences.isFahrenheit = isChecked
        }

        val postButton: Button = findViewById(R.id.postButton)
        postButton.setOnClickListener {
            PostRequestTask().execute()
        }
    }

    private inner class PostRequestTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            makePostRequest()
            return null
        }
    }

    private fun makePostRequest() {
        try {
            val url = URL(url)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"

            // If you have parameters to send in the request, setDoOutput to true
            connection.doOutput = false

            // Get the response
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
}
