package com.ggkbt.currencyconverter

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ggkbt.currencyconverter.databinding.FragmentSettingsBinding
import com.ggkbt.currencyconverter.preferences.LocaleManager

class SettingsFragment : Fragment() {

    private val binding: FragmentSettingsBinding by viewBinding(CreateMethod.INFLATE)
    private val currencyViewModel: CurrencyViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentLanguage = context?.let { LocaleManager.getCurrentLanguage(it) } ?: "en"
        binding.apply {
            settingsToolbar.setNavigationOnClickListener {
                it.findNavController().navigateUp();
            }
            switchTheme.isEnabled = currencyViewModel.prefs.systemTheme
            radioCbrfApi.isChecked = currencyViewModel.prefs.isCbr
            radioExchangeRatesApi.isChecked = !currencyViewModel.prefs.isCbr
            radioEnglish.isChecked = currentLanguage == "en"
            radioRussian.isChecked = currentLanguage == "ru"
            switchTheme.setOnCheckedChangeListener { buttonView, isChecked ->
                currencyViewModel.prefs.systemTheme = isChecked
            }
            radioRussian.setOnClickListener {
                radioEnglish.isChecked = false
                activity?.let { LocaleManager.setLocale(it, "ru") }
            }
            radioEnglish.setOnClickListener {
                radioRussian.isChecked = false
                activity?.let { LocaleManager.setLocale(it, "en") }
            }
            radioCbrfApi.setOnClickListener {
                radioExchangeRatesApi.isChecked = false
                currencyViewModel.prefs.isCbr = true
            }
            radioExchangeRatesApi.setOnClickListener {
                radioCbrfApi.isChecked = false
                currencyViewModel.prefs.isCbr = false
            }
        }
    }
}