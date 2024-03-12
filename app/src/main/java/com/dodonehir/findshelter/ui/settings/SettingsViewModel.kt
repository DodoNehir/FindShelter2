package com.dodonehir.findshelter.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    val EQUPTYPE = stringPreferencesKey("equptype")

    fun updateEquptype(context: Context, selectedEquptype: String) {
        // Preferences DataStore 에 쓰기
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[EQUPTYPE] = selectedEquptype
            }
        }
    }
}