package com.dodonehir.findshelter.ui.settings

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dodonehir.findshelter.R
import com.dodonehir.findshelter.databinding.FragmentSettingsBinding
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
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textSettings
//        settingsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        // set radio button selected
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "selected button: ${requireContext().dataStore.data.first()[EQUPTYPE]}")

                // radio button index이기 때문에 dataStore의 값에서 1을 뺀다.
                var index = requireContext().dataStore.data.first()[EQUPTYPE]?.toInt()?.minus(1) ?: 0
                binding.radioGroup.check(binding.radioGroup.getChildAt(index).id)
            } catch (e: IOException) {
                Log.e(TAG, "IOException occurred: ${e.message}")
            }
        }

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.button1 -> settingsViewModel.updateEquptype(requireContext(), "001")
                R.id.button2 -> settingsViewModel.updateEquptype(requireContext(), "002")
                R.id.button3 -> settingsViewModel.updateEquptype(requireContext(), "003")
                R.id.button4 -> settingsViewModel.updateEquptype(requireContext(), "004")
                R.id.button5 -> settingsViewModel.updateEquptype(requireContext(), "005")
                R.id.button6 -> settingsViewModel.updateEquptype(requireContext(), "006")
                R.id.button7 -> settingsViewModel.updateEquptype(requireContext(), "007")
                R.id.button8 -> settingsViewModel.updateEquptype(requireContext(), "008")
                R.id.button9 -> settingsViewModel.updateEquptype(requireContext(), "009")
                R.id.button10 -> settingsViewModel.updateEquptype(requireContext(), "010")
                R.id.button11 -> settingsViewModel.updateEquptype(requireContext(), "011")
                R.id.button12 -> settingsViewModel.updateEquptype(requireContext(), "012")
                R.id.button13 -> settingsViewModel.updateEquptype(requireContext(), "013")
                R.id.button14 -> settingsViewModel.updateEquptype(requireContext(), "014")
                R.id.button15 -> settingsViewModel.updateEquptype(requireContext(), "015")
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}