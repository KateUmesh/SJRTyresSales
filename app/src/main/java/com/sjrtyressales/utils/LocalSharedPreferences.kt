package com.sjrtyressales.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalSharedPreferences @Inject constructor(@ApplicationContext context: Context) {
    private var mSharedPreferences: SharedPreferences = context.getSharedPreferences(Constant.appName, Context.MODE_PRIVATE)
    private var sharedPreferencesEdit: SharedPreferences.Editor

    init {
        sharedPreferencesEdit = mSharedPreferences.edit()
    }

    /**
     * @return value
     */
    fun getStringValue(key: String): String? {
        if (mSharedPreferences != null)
            return mSharedPreferences.getString(key, "")

        return ""
    }

    /**
     * This method for storing value
     */
    fun putStringValue(key: String, value: String?) {
        sharedPreferencesEdit.putString(key, value)
        commitValue()
    }

    fun commitValue() {
        sharedPreferencesEdit.apply()
        sharedPreferencesEdit.commit()
    }

    /**
     * This method for storing Country name
     */
    fun deleteValue(key: String) {
        sharedPreferencesEdit.remove(key)
        commitValue()
    }

    fun putLongValue(key: String, value: Long) {
        sharedPreferencesEdit.putLong(key, value)
        commitValue()
    }

    fun getLongValue(key: String): Long {
        if (mSharedPreferences != null)
            return mSharedPreferences.getLong(key, 0)
        return 0
    }

    fun putIntValue(key: String, value: Int) {
        sharedPreferencesEdit.putInt(key, value)
        commitValue()
    }

    fun getIntValue(key: String): Int {
        if (mSharedPreferences != null)
            return mSharedPreferences.getInt(key, 0)
        return 0
    }

    fun clear(){
        sharedPreferencesEdit.clear()
        commitValue()
    }

}