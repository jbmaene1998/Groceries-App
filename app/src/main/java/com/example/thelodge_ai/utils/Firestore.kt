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
}
