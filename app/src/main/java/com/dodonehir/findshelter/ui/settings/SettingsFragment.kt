package com.dodonehir.findshelter.ui.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.dodonehir.findshelter.R
import com.dodonehir.findshelter.databinding.FragmentSettingsBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsFragment : Fragment() {

    val TAG = javaClass.name

    // Preferences Datastore key
    val EQUPTYPE = stringPreferencesKey("equptype")

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        // set radio button selected
        val job = lifecycleScope.launch {
            try {
                Log.d(TAG, "selected button: ${requireContext().dataStore.data.first()[EQUPTYPE]}")

                // radio button index이기 때문에 dataStore의 값에서 1을 뺀다.
                var index =
                    requireContext().dataStore.data.first()[EQUPTYPE]?.toInt()?.minus(1) ?: 0
                binding.radioGroup.check(binding.radioGroup.getChildAt(index).id)
            } catch (e: Exception) {
                Log.e(TAG, "Exception occurred: ${e.message}", e)
            }
        }
        if (job.isCompleted)
            job.cancel()

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.button1 -> updateEquptype(requireContext(), "001")
                R.id.button2 -> updateEquptype(requireContext(), "002")
                R.id.button3 -> updateEquptype(requireContext(), "003")
                R.id.button4 -> updateEquptype(requireContext(), "004")
                R.id.button5 -> updateEquptype(requireContext(), "005")
                R.id.button6 -> updateEquptype(requireContext(), "006")
                R.id.button7 -> updateEquptype(requireContext(), "007")
                R.id.button8 -> updateEquptype(requireContext(), "008")
                R.id.button9 -> updateEquptype(requireContext(), "009")
                R.id.button10 -> updateEquptype(requireContext(), "010")
                R.id.button11 -> updateEquptype(requireContext(), "011")
                R.id.button12 -> updateEquptype(requireContext(), "012")
                R.id.button13 -> updateEquptype(requireContext(), "013")
                R.id.button14 -> updateEquptype(requireContext(), "014")
                R.id.button15 -> updateEquptype(requireContext(), "015")
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateEquptype(context: Context, selectedEquptype: String) {
        // Preferences DataStore 에 쓰기
        val job = lifecycleScope.launch {
            context.dataStore.edit { settings ->
                settings[EQUPTYPE] = selectedEquptype
            }
        }
        // 한 번만 실행되도록
        if (job.isCompleted)
            job.cancel()
    }

}