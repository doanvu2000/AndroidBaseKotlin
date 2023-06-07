package com.example.baseproject.base.pref

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

class SpUtils internal constructor(private val SP_FILE_KEY: String) {

    /**
     * Method to get hold of subject 'SharedPreferences' instance
     *
     * @param context Android Context
     * @return returns instance of subject 'SharedPreferences'
     * */
    private fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(SP_FILE_KEY, Context.MODE_PRIVATE)

    /**
     * Method to get hold of subject 'SharedPreferences.Editor' instance
     *
     * @param context Android Context
     * @return returns instance of subject 'SharedPreferences.Editor'
     * */
    private fun getSpEditor(context: Context): SharedPreferences.Editor =
        getSharedPreferences(context).edit()

    /**
     * Method to save(async) object(Primitive,Serializable) on Shared Preference
     *
     * @param context Android Context
     * @param data object for saving of Serializable data types
     * @param key unique key to the object to be saved
     * */
    fun <T : Serializable> saveData(context: Context, data: T, key: String) {
        GlobalScope.launch { saveDataSync(context, data, key) }
    }

    /**
     * Method to save(suspend) object(Primitive,Serializable) on Shared Preference
     *
     * @param context Android Context
     * @param data object for saving of Serializable data types
     * @param key unique key to the object to be saved
     * */
    suspend fun <T : Serializable> saveDataSuspended(context: Context, data: T, key: String) =
        runSuspended { saveDataSync(context, data, key) }!!

    /**
     * Method to save(blocking) object(Primitive,Serializable) on Shared Preference
     *
     * @param context Android Context
     * @param data object for saving of Serializable data types
     * @param key unique key to the object to be saved
     * */
    fun <T : Serializable> saveDataSync(context: Context, data: T, key: String) {
        return saveSerializableData(getSpEditor(context), data, key)
    }

    private fun <T : Serializable> saveSerializableData(editor: SharedPreferences.Editor, data: T, key: String) {
        when (data) {
            is Long -> editor.putLong(key, data)
            is Int -> editor.putInt(key, data)
            is Float -> editor.putFloat(key, data)
            is Boolean -> editor.putBoolean(key, data)
            is String -> editor.putString(key, data.toString())
            else -> editor.putString(key, data.toSerializedString())
        }
        editor.apply()
    }

    /**
     * Method to read(suspend) Serializable objects
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param type subject class type
     * @return 'instance' of subject type if found else null
     * */
    suspend fun <T : Serializable> getDataSuspended(context: Context, key: String, type: Class<T>): T? =
        runSuspended { getData(context, key, type) }

    /**
     * Method to read(blocking) Serializable objects
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param type subject class type
     * @return 'instance' of subject type if found else null
     * */
    @Suppress("UNCHECKED_CAST")
    fun <T : Serializable> getData(context: Context, key: String, type: Class<T>): T? {
        var retVal: T? = null
        getSharedPreferences(context).let {
            if (it.contains(key)) {
                try {
                    retVal = when {
                        type.isAssignableFrom(Long::class.java) -> it.getLong(key, Long.MIN_VALUE)
                        type.isAssignableFrom(Int::class.java) -> it.getInt(key, Int.MIN_VALUE)
                        type.isAssignableFrom(Float::class.java) -> it.getFloat(key, Float.MIN_VALUE)
                        type.isAssignableFrom(false.javaClass) -> it.getBoolean(key, false)
                        type.isAssignableFrom(String::class.java) -> it.getString(key, "")
                        else -> it.getString(key, "")!!.toSerializable(type)
                    } as T?
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            } else {
                retVal = null
            }
        }
        return retVal
    }

    /**
     * Method to save(Async) Collection of objects that implements Serializable on Shared Preference
     *
     * @param context Android Context
     * @param data Collection of object for saving
     * @param key unique key to the object to be saved
     * @param saveSynced Whether should be saved synced
     * */
    fun <T : Serializable> saveSerializableCollection(context: Context, data: Collection<T>, key: String) =
        GlobalScope.launch { saveSerializableCollectionSync(context, data, key) }

    /**
     * Method(suspend) to save Collection of objects that implements Serializable on Shared Preference
     *
     * @param context Android Context
     * @param data Collection of object for saving
     * @param key unique key to the object to be saved
     * */
    suspend fun <T : Serializable> saveSerializableCollectionSuspended(context: Context, data: Collection<T>, key: String) =
        runSuspended { saveSerializableCollectionSync(context, data, key) }

    /**
     * Method(blocking) to save Collection of objects that implements Serializable on Shared Preference
     *
     * @param context Android Context
     * @param data Collection of object for saving
     * @param key unique key to the object to be saved
     * */
    fun <T : Serializable> saveSerializableCollectionSync(context: Context, data: Collection<T>, key: String) =
        saveSerializableCollection(getSpEditor(context), data, key)

    private fun <T : Serializable> saveSerializableCollection(editor: SharedPreferences.Editor, data: Collection<T>, key: String) {
        data.map { it.toSerializedString().addTag() }.toMutableSet().let {
            editor.putStringSet(key, it)
        }
        editor.apply()
    }

    /**
     * Method(async) to save Map<K : Serializable,V : Serializable> on Shared Preference
     *
     * @param context Android Context
     * @param data Map<K : Serializable,V : Serializable>
     * @param key unique key to the object to be saved
     * */
    fun <K : Serializable, V : Serializable> saveSerializableMap(context: Context, data: Map<K, V>, key: String) =
        GlobalScope.launch { saveSerializableMapSync(context, data, key) }

    /**
     * Method(suspend) to save Map<K : Serializable,V : Serializable> on Shared Preference
     *
     * @param context Android Context
     * @param data Map<K : Serializable,V : Serializable>
     * @param key unique key to the object to be saved
     * */
    suspend fun <K : Serializable, V : Serializable> saveSerializableMapSuspended(context: Context, data: Map<K, V>, key: String) =
        runSuspended { saveSerializableMapSync(context, data, key) }

    /**
     * Method(blocking) to save Map<K : Serializable,V : Serializable> on Shared Preference
     *
     * @param context Android Context
     * @param data Map<K : Serializable,V : Serializable>
     * @param key unique key to the object to be saved
     * */
    fun <K : Serializable, V : Serializable> saveSerializableMapSync(context: Context, data: Map<K, V>, key: String) =
        saveSerializableMap(getSpEditor(context), data, key)

    private fun <K : Serializable, V : Serializable> saveSerializableMap(
        editor: SharedPreferences.Editor,
        data: Map<K, V>,
        key: String
    ) {
        val dataSet = mutableSetOf<String>()
        data.keys.asSequence().forEach {
            dataSet.add(data.get(it)!!.toSerializedString().addTag(it.toSerializedString()))
        }
        editor.putStringSet(key, dataSet)
        editor.apply()
    }

    /**
     * Method(async) to save Map<K : Serializable,V : Parcelable> on Shared Preference
     *
     * @param context Android Context
     * @param data Map<K : Serializable,V : Parcelable>
     * @param key unique key to the object to be saved
     * */
    fun <K : Serializable, V : Parcelable> saveParcelableMap(
        context: Context,
        data: Map<K, V>, key: String
    ) = GlobalScope.launch { saveParcelableMapSync(context, data, key) }

    /**
     * Method(suspend) to save Map<K : Serializable,V : Parcelable> on Shared Preference
     *
     * @param context Android Context
     * @param data Map<K : Serializable,V : Parcelable>
     * @param key unique key to the object to be saved
     * */
    suspend fun <K : Serializable, V : Parcelable> saveParcelableMapSuspended(
        context: Context,
        data: Map<K, V>, key: String
    ) = runSuspended { saveParcelableMapSync(context, data, key) }

    /**
     * Method(blocking) to save Map<K : Serializable,V : Parcelable> on Shared Preference
     *
     * @param context Android Context
     * @param data Map<K : Serializable,V : Parcelable>
     * @param key unique key to the object to be saved
     * */
    fun <K : Serializable, V : Parcelable> saveParcelableMapSync(
        context: Context,
        data: Map<K, V>, key: String
    ) = saveParcelableMap(getSpEditor(context), data, key)

    private fun <K : Serializable, V : Parcelable> saveParcelableMap(
        editor: SharedPreferences.Editor,
        data: Map<K, V>, key: String
    ) {
        val dataSet = mutableSetOf<String>()
        data.keys.asSequence().forEach {
            dataSet.add(data.get(it)!!.toSerializedString().addTag(it.toSerializedString()))
        }
        editor.putStringSet(key, dataSet)
        editor.apply()
    }

    /**
     * Method(suspend) to read Map<K : Serializable,V : Serializable> from Shared Preference
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param keyType class type of Map key
     * @param valueType class type of Map value
     * @return Map<K : Serializable,V : Serializable> if found else null
     * */

    suspend fun <K : Serializable, V : Serializable> getSerializableMapSuspended(
        context: Context, keyType: Class<K>, valueType: Class<V>, key: String
    )
            : Map<K, V>? =
        runSuspended { getSerializableMap(context, keyType, valueType, key) }

    /**
     * Method(blocking) to read Map<K : Serializable,V : Serializable> from Shared Preference
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param keyType class type of Map key
     * @param valueType class type of Map value
     * @return Map<K : Serializable,V : Serializable> if found else null
     * */
    fun <K : Serializable, V : Serializable> getSerializableMap(
        context: Context, keyType: Class<K>, valueType: Class<V>, key: String
    )
            : Map<K, V>? {
        val dataSet = mutableMapOf<K, V>()
        try {
            getSharedPreferences(context)
                .getStringSet(key, mutableSetOf<String>())?.let {
                    if (it.isNotEmpty()) {
                        it.asSequence()
                            .map { it.removeTag() }
                            .filter { it != null }
                            .map { it!! }.forEach {
                                dataSet.put(
                                    it.first.toSerializable(keyType)!!,
                                    it.second.toSerializable(valueType)!!
                                )
                            }
                        return dataSet.toMap()
                    }
                }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return null
    }

    /**
     * Method(suspend) to read Map<K : Serializable,V : Parcelable> from Shared Preference
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param keyType class type of Map key
     * @param creator Parcelable.Creator of subject value type
     * @return Map<K : Serializable,V : Serializable> if found else null
     * */
    suspend fun <K : Serializable, V : Parcelable> getParcelableMapSuspended(
        context: Context, keyType: Class<K>,
        creator: Parcelable.Creator<V>, key: String
    )
            : Map<K, V>? = runSuspended { getParcelableMap(context, keyType, creator, key) }

    /**
     * Method(blocking) to read Map<K : Serializable,V : Parcelable> from Shared Preference
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param keyType class type of Map key
     * @param creator Parcelable.Creator of subject value type
     * @return Map<K : Serializable,V : Serializable> if found else null
     * */
    fun <K : Serializable, V : Parcelable> getParcelableMap(
        context: Context, keyType: Class<K>,
        creator: Parcelable.Creator<V>, key: String
    )
            : Map<K, V>? {
        val dataSet = mutableMapOf<K, V>()
        try {
            getSharedPreferences(context)
                .getStringSet(key, mutableSetOf<String>())?.let {
                    if (it.isNotEmpty()) {
                        it.asSequence()
                            .map { it.removeTag() }
                            .filter { it != null }
                            .map { it!! }.forEach {
                                dataSet.put(
                                    it.first.toSerializable(keyType)!!,
                                    it.second.toParcelable(creator)
                                )
                            }
                        return dataSet.toMap()
                    }
                }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return null
    }

    /**
     * Method(blocking) to read object Collection of Class, that implements Serializable from Shared Preference
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param type subject class type
     * @return Collection of subject type if found else null
     * */
    fun <T : Serializable> getSerializableCollection(context: Context, type: Class<T>, key: String): Collection<T>? {
        if (checkIfExists(context, key)) {
            try {
                getSharedPreferences(context)
                    .getStringSet(key, mutableSetOf())
                    ?.let {
                        if (it.isNotEmpty()) {
                            return it.map { it.removeTag()?.second?.toSerializable(type) }.filter { it != null }.map { it!! }
                        }
                    }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
        return null
    }

    /**
     * Method to read object Collection of Class, that implements Serializable, with suspension
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param type subject class type
     * @return Collection of subject type if found else null
     * */
    suspend fun <T : Serializable> getSerializableCollectionSuspended(context: Context, type: Class<T>, key: String)
            : Collection<T>? = runSuspended { getSerializableCollection(context, type, key) }

    /**
     * Method to save(Async) Collection of objects that implements Parcelable on Shared Preference
     *
     * @param context Android Context
     * @param data Collection of object for saving
     * @param key unique key to the object to be saved
     * */
    fun <T : Parcelable> saveParcelableCollection(context: Context, data: Collection<T>, key: String) =
        GlobalScope.launch { saveParcelableCollectionSync(context, data, key) }

    private fun <T : Parcelable> saveParcelableCollection(editor: SharedPreferences.Editor, data: Collection<T>, key: String) {
        data.map { it.toSerializedString().addTag() }.toMutableSet().let {
            editor.putStringSet(key, it)
        }
        editor.apply()
    }

    /**
     * Method(blocking) to save Collection of objects that implements Parcelable on Shared Preference
     *
     * @param context Android Context
     * @param data Collection of object for saving
     * @param key unique key to the object to be saved
     * */
    fun <T : Parcelable> saveParcelableCollectionSync(context: Context, data: Collection<T>, key: String) =
        saveParcelableCollection(getSpEditor(context), data, key)

    /**
     * Method(suspend) to save Collection of objects that implements Parcelable on Shared Preference
     *
     * @param context Android Context
     * @param data Collection of object for saving
     * @param key unique key to the object to be saved
     * */
    suspend fun <T : Parcelable> saveParcelableCollectionSuspended(context: Context, data: Collection<T>, key: String) =
        runSuspended { saveParcelableCollectionSync(context, data, key) }

    /**
     * Method(blocking) to read object Collection of Class, that implements Parcelable
     *
     * @param context Android Context
     * @param creator Parcelable.Creator of subject type
     * @param key unique key to the object to be saved
     * @return Collection of subject type if found else null
     * */
    fun <T : Parcelable> getParcelableCollection(context: Context, creator: Parcelable.Creator<T>, key: String): Collection<T>? {
        if (checkIfExists(context, key)) {
            try {
                getSharedPreferences(context)
                    .getStringSet(key, mutableSetOf())
                    ?.let {
                        if (it.isNotEmpty()) {
                            return it.map { it.removeTag()?.second?.toParcelable(creator) }.filter { it != null }.map { it!! }
                        }
                    }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
        return null
    }

    /**
     * Method to read object Collection of Class, that implements Parcelable, with suspension
     *
     * @param context Android Context
     * @param creator Parcelable.Creator of subject type
     * @param key unique key to the object to be saved
     * @return Collection of subject type if found else null
     * */
    suspend fun <T : Parcelable> getParcelableCollectionSuspended(context: Context, creator: Parcelable.Creator<T>, key: String)
            : Collection<T>? = runSuspended { getParcelableCollection(context, creator, key) }

    /**
     * Method(async) to save Parcelable data on Shared Preference
     *
     * @param context Android Context
     * @param data Parcelable
     * @param key unique key to the object to be saved
     * */
    fun <T : Parcelable> saveParcelable(context: Context, data: T, key: String) =
        GlobalScope.launch { saveParcelableSync(context, data, key) }

    /**
     * Method(suspend) to save Parcelable data on Shared Preference
     *
     * @param context Android Context
     * @param data Parcelable
     * @param key unique key to the object to be saved
     * */
    suspend fun <T : Parcelable> saveParcelableSuspended(context: Context, data: T, key: String) =
        runSuspended { saveParcelableSync(context, data, key) }

    /**
     * Method(blocking) to save Parcelable data on Shared Preference
     *
     * @param context Android Context
     * @param data Parcelable
     * @param key unique key to the object to be saved
     * */
    fun <T : Parcelable> saveParcelableSync(context: Context, data: T, key: String) = saveParcelable(getSpEditor(context), data, key)

    private fun <T : Parcelable> saveParcelable(editor: SharedPreferences.Editor, data: T, key: String) {
        editor.putString(key, data.toSerializedString())
        editor.apply()
    }

    /**
     * Method(Blocking) to read object of Class that implements Parcelable from Shared Preference
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param creator Parcelable.Creator of subject type
     * @return 'instance' of subject type if found else null
     * */
    fun <T : Parcelable> getParcelableData(
        context: Context, key: String,
        creator: Parcelable.Creator<T>
    ): T? {
        var retVal: T? = null
        getSharedPreferences(context).let {
            if (it.contains(key)) {
                try {
                    it.getString(key, "")?.let {
                        if (it.isNotBlank()) {
                            retVal = it.toParcelable(creator)
                        }
                    }
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            } else {
                retVal = null
            }
        }
        return retVal
    }

    /**
     * Method to read object of Class that implements Parcelable with suspension
     *
     * @param context Android Context
     * @param key unique key to the object to be saved
     * @param creator Parcelable.Creator of subject type
     * @return 'instance' of subject type if found else null
     * */
    suspend fun <T : Parcelable> getParcelableDataSuspended(
        context: Context, key: String,
        creator: Parcelable.Creator<T>
    ): T? =
        runSuspended { getParcelableData(context, key, creator) }

    /**
     * Removes object with given key from Shared Preferences
     *
     * @param context Android Context
     * @param key unique key to the saved object
     * */
    fun removeKey(context: Context, key: String) = getSpEditor(context).remove(key).apply()

    /**
     * Checks whwather object with given key exists on Shared Preferences
     *
     * @param context Android Context
     * @param key unique key to the saved object
     * @return true if found else false
     * */
    fun checkIfExists(context: Context, key: String): Boolean = getSharedPreferences(context).contains(key)

    /**
     * Clears all saved data from subject Shared Preferences
     *
     * @param context Android Context
     * */
    fun clearAll(context: Context): Boolean = getSpEditor(context).clear().commit()

    /**
     * Registers Shared Preference Change Listener     *
     *
     * */
    fun registerOnChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) = getSharedPreferences(context).registerOnSharedPreferenceChangeListener(listener)

    /**
     * Un-registers Shared Preference Change Listener     *
     *
     * */
    fun unRegisterOnChangeListener(
        context: Context,
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) = getSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(listener)

    companion object {

        private val DEFAULT_SP_FILE_NAME: String =
            "com.dasbikash.android_shared_preference_utils.SharedPreferenceUtils.DEFAULT_SP_FILE_NAME"

        /**
         * Returns class instance for given Shared Preferences storage file
         *
         * @param spFileName Shared Preferences storage file name
         * @return instance of SharedPreferenceUtils
         * */
        @JvmStatic
        fun getInstance(spFileName: String) = SpUtils(spFileName)


        /**
         * Returns class instance for default Shared Preferences storage file
         *
         * @return instance of SharedPreferenceUtils that points to default file.
         * */
        @JvmStatic
        fun getDefaultInstance() = SpUtils(DEFAULT_SP_FILE_NAME)

        @Keep
        private class SpEntry(
            var data: String,
            var type: Class<*>,
            var keyType: Class<*>? = null,
            var valueType: Class<*>? = null
        ) : Serializable
    }
}