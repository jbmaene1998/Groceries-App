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

    fun listenForIngredientsChanges(
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val collectionReference = firestore.collection("ingredients")

        // Listen for changes in the "ingredients" collection
        collectionReference.addSnapshotListener { querySnapshot, exception ->
            if (exception != null) {
                // Handle failures in listening for changes
                onFailure.invoke(exception)
                return@addSnapshotListener
            }

            val ingredientsList = mutableListOf<String>()

            // Iterate through documents and get the ingredients
            querySnapshot?.documents?.forEach { documentSnapshot ->
                val ingredient = documentSnapshot.getString("ingredient")
                ingredient?.let {
                    ingredientsList.add(it)
                }
            }

            // Invoke the success callback with the list of ingredients
            onSuccess.invoke(ingredientsList)
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
                val documents = mutableListOf<Map<String, Any>>()

                for (documentSnapshot in querySnapshot) {
                    val data = documentSnapshot.data
                    val document = mutableMapOf<String, Any>()

                    document["id"] = documentSnapshot.id
                    document["name"] = data["ingredient"] as Any // Update field name if needed

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
}
