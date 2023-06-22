package combignerdranch.android.photogallery

import android.content.Context
import androidx.preference.PreferenceManager


private const val PREF_SEARCH_QUERY = "searchQuery"

object QueryPreferences {

    fun getStoredQuery(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    fun setStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PREF_SEARCH_QUERY, query)
            .apply()
    }
}

/*добавьте две зависимости в свой gradle
implementation 'androidx.preference:preference-ktx:1.2.1'
implementation 'androidx.legacy:legacy-preference-v14:1.0.0'
Удалить
import android.preference.PreferenceManager
Добавить
import androidx.preference.PreferenceManager
Если вы столкнулись с ошибкой “дублирующийся класс” во время выполнения, вам также может потребоваться добавить эти зависимости:

implementation "androidx.lifecycle:lifecycle-viewmodel:2.5.1"
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1"*/