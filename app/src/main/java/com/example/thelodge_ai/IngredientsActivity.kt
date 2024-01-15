package com.example.thelodge_ai

import FirestoreHelper
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class IngredientsActivity : AppCompatActivity() {

    data class Ingredient(
        val id: String,
        val name: String,
    )
    private val firestoreHelper = FirestoreHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)

        // Retrieve ingredients from Firestore using FirestoreHelper
        val ingredientsLayout: LinearLayout = findViewById(R.id.ingredientsLayout)

        firestoreHelper.getAllIngredients(
            onSuccess = { documents ->
                ingredientsLayout.removeAllViews()

                for (document in documents) {
                    val ingredient = Ingredient(
                        id = document["id"].toString(),
                        name = document["name"].toString() // Update field name if needed
                    )
                    displayIngredient(ingredient)
                }
            },
            onFailure = { exception ->
                Toast.makeText(this, "Error loading ingredients: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun displayIngredient(ingredient: Ingredient) {
        val ingredientsLayout: LinearLayout = findViewById(R.id.ingredientsLayout)

        val ingredientTextView = TextView(this)
        ingredientTextView.text = ingredient.name

        val deleteButton = Button(this)
        deleteButton.text = "Delete"
        deleteButton.setOnClickListener {
            // Delete ingredient from Firestore using FirestoreHelper
            firestoreHelper.deleteDocument(
                documentId = ingredient.id,
                onSuccess = {
                    // Remove the views from the layout
                    ingredientsLayout.removeView(ingredientTextView)
                    ingredientsLayout.removeView(deleteButton)

                    Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show()
                },
                onFailure = {
                    Toast.makeText(this, "Error deleting ingredient: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        ingredientsLayout.addView(ingredientTextView)
        ingredientsLayout.addView(deleteButton)
    }
}
