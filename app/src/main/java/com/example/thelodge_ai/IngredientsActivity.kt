package com.example.thelodge_ai

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class IngredientsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)

        // Retrieve ingredients from Firestore
        val firestore = FirebaseFirestore.getInstance()
        val ingredientsLayout: LinearLayout = findViewById(R.id.ingredientsLayout)

        firestore.collection("ingredients")
            .get()
            .addOnSuccessListener { result ->
                ingredientsLayout.removeAllViews()

                for (document in result) {
                    val ingredient = document.toObject(Ingredient::class.java)
                    displayIngredient(ingredient)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading ingredients", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayIngredient(ingredient: Ingredient) {
        val ingredientsLayout: LinearLayout = findViewById(R.id.ingredientsLayout)

        val ingredientTextView = TextView(this)
        ingredientTextView.text = ingredient.name

        val deleteButton = Button(this)
        deleteButton.text = "Delete"
        deleteButton.setOnClickListener {
            // Delete ingredient from Firestore
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("ingredients").document(ingredient.id)
                .delete()
                .addOnSuccessListener {
                    // Remove the views from the layout
                    ingredientsLayout.removeView(ingredientTextView)
                    ingredientsLayout.removeView(deleteButton)

                    Toast.makeText(this, "Ingredient deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error deleting ingredient", Toast.LENGTH_SHORT).show()
                }
        }

        ingredientsLayout.addView(ingredientTextView)
        ingredientsLayout.addView(deleteButton)
    }
}
