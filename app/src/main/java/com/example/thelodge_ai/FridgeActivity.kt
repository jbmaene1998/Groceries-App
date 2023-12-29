// FridgeActivity.kt
import AppPreferences
import FirestoreHelper
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FridgeActivity : AppCompatActivity() {

    private lateinit var temperatureTitle: TextView
    private lateinit var temperatureValue: TextView
    private lateinit var dewPointTitle: TextView
    private lateinit var dewPointValue: TextView
    private lateinit var humidityTitle: TextView
    private lateinit var humidityValue: TextView
    private val firestoreHelper = FirestoreHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge)

        // Initialize TextViews
        temperatureTitle = findViewById(R.id.temperatureTitle)
        temperatureValue = findViewById(R.id.temperatureValue)
        dewPointTitle = findViewById(R.id.dewPointTitle)
        dewPointValue = findViewById(R.id.dewPointValue)
        humidityTitle = findViewById(R.id.humidityTitle)
        humidityValue = findViewById(R.id.humidityValue)

        // Set text based on preferences
        val isFahrenheit = AppPreferences.getInstance(this).isFahrenheit
        temperatureTitle.text = if (isFahrenheit) "Fahrenheit" else "Celsius"

        // Retrieve values from the last document in Firestore using FirestoreHelper
        firestoreHelper.getLastDocument(
            onSuccess = { fahrenheit, temperature, humidity, dewPoint ->
                // Set the TextViews with the retrieved values
                if (isFahrenheit) {
                    temperatureValue.text = String.format("%.2f °F", fahrenheit)
                } else {
                    temperatureValue.text = String.format("%.2f °C", temperature)
                }
                dewPointValue.text = String.format("%.2f", dewPoint)
                humidityValue.text = String.format("%.2f", humidity)
            },
            onFailure = { exception ->
                // Handle failures
                // For example, show an error message
                // Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
