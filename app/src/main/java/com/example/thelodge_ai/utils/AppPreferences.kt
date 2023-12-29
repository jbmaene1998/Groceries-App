import android.content.Context
import android.content.SharedPreferences

class AppPreferences private constructor(context: Context) {

    private val PREFS_NAME = "MyPrefsFile"
    private val IS_FAHRENHEIT_KEY = "isFahrenheit"

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isFahrenheit: Boolean
        get() = prefs.getBoolean(IS_FAHRENHEIT_KEY, true)
        set(value) = prefs.edit().putBoolean(IS_FAHRENHEIT_KEY, value).apply()

    companion object {
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            if (instance == null) {
                instance = AppPreferences(context)
            }
            return instance!!
        }
    }
}
