// FirestoreHelper.kt
import com.google.firebase.firestore.FirebaseFirestore


class FirestoreHelper {

    private val firestore = FirebaseFirestore.getInstance()

    fun getLastDocument(
        onSuccess: (fahrenheit: Double, temperature: Double, humidity: Double, dewPoint: Double) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val collectionReference = firestore.collection("sensors-data")

        // Query the collection to get documents ordered by timestamp in descending order
        collectionReference
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Get the first (and only) document in the result set
                    val documentSnapshot = querySnapshot.documents[0]

                    val fahrenheit = documentSnapshot.getDouble("fahrenheit") ?: 0.0
                    val temperature = documentSnapshot.getDouble("temperature") ?: 0.0
                    val humidity = documentSnapshot.getDouble("humidity") ?: 0.0
                    val dewPoint = documentSnapshot.getDouble("dewPoint") ?: 0.0

                    onSuccess.invoke(fahrenheit, temperature, humidity, dewPoint)
                } else {
                    // Handle the case where the collection is empty
                    onFailure.invoke(Exception("No documents found"))
                }
            }
            .addOnFailureListener { exception ->
                // Handle failures in retrieving data from Firestore
                onFailure.invoke(exception)
            }
    }

    fun getAllIngredients(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val collectionReference = firestore.collection("ingredients")

        collectionReference
            .get()
            .addOnSuccessListener { querySnapshot ->
                val documents = mutableListOf<Any>()

                for (documentSnapshot in querySnapshot) {
                    val data = documentSnapshot.data
                    val document = mapOf(
                        "id" to documentSnapshot.id,
                        "name" to data["ingredient"] // Add other fields as needed
                    )
                    documents.add(document)
                }

                onSuccess.invoke(documents)
            }
            .addOnFailureListener { exception ->
                onFailure.invoke(exception)
            }
    }

    fun deleteDocument(
        documentId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val collectionReference = firestore.collection("ingredients")

        collectionReference.document(documentId)
            .delete()
            .addOnSuccessListener {
                onSuccess.invoke()
            }
            .addOnFailureListener { exception ->
                onFailure.invoke(exception)
            }
    }

    fun addIngredient(
        ingredient: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val collectionReference = firestore.collection("ingredients")

        // Use the provided map to get the ID for the ingredient
        val optionalResults = mapOf(
            "Banana" to 0,
            "Cabbage" to 1,
            "Carrots" to 2,
            "Milk" to 3,
            "Onion" to 4,
            "Potato" to 5,
            "Tomato" to 6
        )

        val ingredientId = optionalResults[ingredient] ?: -1

        if (ingredientId != -1) {
            // Create a new document with the specified ingredient and ID
            val newIngredient = hashMapOf(
                "ingredient" to ingredient,
                "id" to ingredientId
                // Add other properties as needed
            )

            // Add the new document to the "ingredients" collection
            collectionReference
                .add(newIngredient)
                .addOnSuccessListener { documentReference ->
                    // Retrieve the auto-generated ID of the newly added document
                    val newDocumentId = documentReference.id
                    onSuccess.invoke(newDocumentId)
                }
                .addOnFailureListener { exception ->
                    onFailure.invoke(exception)
                }
        } else {
            // Handle the case where the ingredient is not found in the map
            onFailure.invoke(Exception("Invalid ingredient: $ingredient"))
        }
    }
}
