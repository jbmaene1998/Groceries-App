package com.example.thelodge_ai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import khttp.post
import kotlinx.android.synthetic.main.activity_settings.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    // Use the API key defined in build.gradle
    private val apiKey: String by lazy {
        BuildConfig.OPENAI_API_KEY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recipes)

        val inputText = "You are a cook! You help college students with eating more healthier. Give 5 recipes: Recipe name, ingredients and guide to make the recipe."
        val generatedText = makeApiRequest(inputText)
        resultTextView.text = generatedText
    }

    private fun makeApiRequest(prompt: String): String {
        val endpoint = "https://api.openai.com/v1/engines/davinci-codex/completions"

        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer $apiKey"
        )

        val data = mapOf(
            "prompt" to prompt,
            "max_tokens" to 150
            // Add other parameters as needed
        )

        val response = post(endpoint, headers = headers, json = data)

        return parseApiResponse(response.text)
    }

    private fun parseApiResponse(response: String): String {
        val jsonResponse = JSONObject(response)
        val generatedText = jsonResponse.optString("choices", "")
        return generatedText
    }
}
