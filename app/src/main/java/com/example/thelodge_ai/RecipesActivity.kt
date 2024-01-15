package com.example.thelodge_ai

import android.os.AsyncTask
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class RecipesActivity() : AppCompatActivity() {

    private val apiKey: String = "https://chat.openai.com/share/60b4f38c-0985-4659-836f-eb4feeab5f62"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        val inputText = "Give a healty recipe for a student: Recipe name, ingredients and guide to make the recipe. Based on following ingredients: apple, sausage, potatoes"
        val generatedText = makeApiRequest(inputText)

        // Find the TextView and set the generated text
        val generatedTextView = findViewById<TextView>(R.id.generatedTextView)
        generatedTextView.text = "Loading recipe..."
    }

    private inner class ApiRequestTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String {
            try {
                val endpoint = "https://api.openai.com/v1/engines/text-davinci-003/completions"
                val url = URL(endpoint)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.doOutput = true

                val jsonInputString = "{\"prompt\":\"${params[0]}\",\"max_tokens\":150}"
                val outputStream: OutputStream = connection.outputStream
                outputStream.write(jsonInputString.toByteArray(charset("UTF-8")))
                outputStream.flush()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()

                    var line: String? = null
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    reader.close()
                    connection.disconnect()

                    return parseApiResponse(response.toString())
                } else {
                    connection.disconnect()
                    return "Error: $responseCode"
                }
            } catch (e: Exception) {
                return "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String) {
            // Assuming you have a TextView with the id generatedTextView in your layout
            val generatedTextView = findViewById<TextView>(R.id.generatedTextView)

            // Set the generated text to the TextView
            generatedTextView.text = result
        }
    }

    // Your existing function with changes to use AsyncTask
    private fun makeApiRequest(prompt: String) {
        val apiRequestTask = ApiRequestTask()
        apiRequestTask.execute(prompt)
    }


    private fun parseApiResponse(response: String): String {
        val jsonResponse = JSONObject(response)
        val generatedChoices = jsonResponse.optJSONArray("choices")

        // Check if the array is not null and has elements
        if (generatedChoices != null && generatedChoices.length() > 0) {
            val firstChoice = generatedChoices.getJSONObject(0)
            return firstChoice.optString("text")
        } else {
            // Handle the case where the array is empty or null
            return "No choices found"
        }
    }
}
