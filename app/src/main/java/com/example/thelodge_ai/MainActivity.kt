package com.example.thelodge_ai

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize firebase
        FirebaseApp.initializeApp(this)

        // Check Fridge Button
        val checkFridgeButton: Button = findViewById(R.id.btnCheckFridge)
        checkFridgeButton.setOnClickListener {
            // Handle Check Fridge button click
            startActivity(Intent(this, FridgeActivity::class.java))
        }

        // Add Product Button
        val addProductButton: Button = findViewById(R.id.btnAddProduct)
        addProductButton.setOnClickListener {
            // Handle Add Product button click
            startActivity(Intent(this, ScanActivity::class.java))
        }

        // Recipes Button
        val recipesButton: Button = findViewById(R.id.btnRecipes)
        recipesButton.setOnClickListener {
            // Handle Settings button click
            startActivity(Intent(this, RecipesActivity::class.java))
        }

        // Settings Button
        val settingsButton: Button = findViewById(R.id.btnSettings)
        settingsButton.setOnClickListener {
            // Handle Settings button click
            startActivity(Intent(this, SettingsActivity::class.java))
        }


        // Ingredients Button
        val ingredientsButton: Button = findViewById(R.id.btnIngredients)
        ingredientsButton.setOnClickListener {
            // Handle Settings button click
            startActivity(Intent(this, IngredientsActivity::class.java))
        }
    }
}
