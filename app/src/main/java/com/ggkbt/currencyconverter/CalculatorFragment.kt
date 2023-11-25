package com.ggkbt.currencyconverter

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ggkbt.currencyconverter.databinding.FragmentCalculatorBinding
import com.ggkbt.currencyconverter.enums.Currency
import com.ggkbt.currencyconverter.enums.ListSelector
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat


class CalculatorFragment : Fragment() {
    companion object {
        private const val SELECTED_CURRENCY = "selected_currency"
        private const val LIST_SELECTOR = "list_selector"
        private const val DIVISION_SCALE = 8
        private const val DECIMAL_PATTERN = "#0.####"
    }

    private lateinit var listSelector: ListSelector
    private var selectedCurrency: Currency? = null

    private val binding: FragmentCalculatorBinding by viewBinding(CreateMethod.INFLATE)
    private val viewModel: CurrencyViewModel by viewModels()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        listSelector = arguments?.getParcelable(LIST_SELECTOR) ?: ListSelector.UNDEFINED
        selectedCurrency = arguments?.getParcelable(SELECTED_CURRENCY)
        Log.d("TAG_123", "listSelector: $listSelector, selectedCurrency: $selectedCurrency")
        if (listSelector != ListSelector.UNDEFINED && selectedCurrency != null) {
            if (listSelector == ListSelector.SOURCE) {
                viewModel.baseCurrency = selectedCurrency!!
            } else if (listSelector == ListSelector.TARGET) {
                viewModel.targetCurrency = selectedCurrency!!
            }
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("TAG_123", "listSelector: $listSelector, selectedCurrency: $selectedCurrency")

        binding.iwSettings.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_calculatorFragment_to_settingsFragment)
        }

        if (viewModel.prefs.isCbr) setCbrCase() else setXrCase()
        binding.baseCurrencySelectorContainer.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("list_selector", ListSelector.SOURCE)
            it.findNavController()
                .navigate(R.id.action_calculatorFragment_to_currencyListFragment, bundle)
        }


        viewModel.getError().observe(viewLifecycleOwner) { error ->
            binding.calculatorLayout.visibility = View.VISIBLE
            if (error != null) {
                error.second?.let { noDbData ->
                    if (noDbData) {
                        binding.calculatorLayout.visibility = View.GONE
                        binding.errorTextView.text =
                            context?.let {
                                error.first?.let { it1 ->
                                    ErrorHelper(it).getFetchingError(
                                        it1
                                    )
                                }
                            }
                        binding.errorLayout.visibility = View.VISIBLE
                    }
                }
                binding.apply {
                    progressBar.visibility = View.GONE
                    if (swipeContainer.isRefreshing) {
                        Toast.makeText(
                            context,
                            context?.let {
                                error.first?.let { it1 ->
                                    ErrorHelper(it).getFetchingError(
                                        it1
                                    )
                                }
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    swipeContainer.isRefreshing = false
                }
            }
        }
        binding.swipeContainer.setOnRefreshListener {
            viewModel.refreshData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCbrCase() {
        viewModel.targetCurrency = Currency.RUB
        binding.targetCurrencyArrowDown.visibility = View.GONE
        viewModel.getCbrData().observe(viewLifecycleOwner) { currenciesList ->
            if (!currenciesList.isNullOrEmpty()) {
                var cbrBase = currenciesList.find { currency ->
                    currency.charCode == viewModel.baseCurrency.name
                }
                if (cbrBase == null) {
                    viewModel.baseCurrency = Currency.EUR
                    cbrBase = currenciesList.find { currency ->
                        currency.charCode == viewModel.baseCurrency.name
                    } ?: return@observe
                }
                onSuccess(nominal = cbrBase.nominal.toBigDecimal(), value = cbrBase.value)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setXrCase() {
        binding.targetCurrencyArrowDown.visibility = View.VISIBLE
        binding.targetCurrencySelectorContainer.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("list_selector", ListSelector.TARGET)
            it.findNavController()
                .navigate(R.id.action_calculatorFragment_to_currencyListFragment, bundle)
        }
        with(viewModel) {
            getXrData().observe(viewLifecycleOwner) { rates ->
                if (!rates.isNullOrEmpty()) {
                    Log.d("TAG123", "rates: $rates")
                    Log.d("TAG123", "rates[baseCurrency.name]: ${rates[baseCurrency.name]}")
                    Log.d("TAG123", "rates[targetCurrency.name]: ${rates[targetCurrency.name]}")
                    onSuccess(
                        nominal = rates[baseCurrency.name] ?: BigDecimal(-1),
                        value = rates[targetCurrency.name] ?: BigDecimal(-1)
                    )
                }
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onSuccess(value: BigDecimal, nominal: BigDecimal) {
        binding.apply {
            swipeContainer.isRefreshing = false
            calculatorLayout.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            viewModel.baseCurrency.let {
                valueInBaseCurrencyEditText.hint = it.name
                baseCurrencyTextView.text = it.name
                baseCurrencyFlag.setImageResource(it.flagResId)
            }
            viewModel.targetCurrency.let {
                valueInTargetCurrencyEditText.hint = it.name
                targetCurrencyTextView.text = it.name
                targetCurrencyFlag.setImageResource(it.flagResId)
            }
            valueInBaseCurrencyEditText.setText(
                formatBigDecimal(nominal)
            )
            valueInTargetCurrencyEditText.setText(
                formatBigDecimal(value)
            )
        }
        Log.d("TAG_VALUES", "value: $value, nominal: $nominal")
        with(viewModel) {
            oneTargetVal = calculateOneTargetVal(value, nominal)
            valueInTarget = calculateValueInTarget(value, nominal)
        }
        setTextChangedListeners()
        Log.d(
            "TAG_VALUES",
            "oneTargetVal: ${viewModel.oneTargetVal}, valueInTarget: ${viewModel.valueInTarget}"
        )
    }

    private fun formatBigDecimal(value: BigDecimal): String =
        DecimalFormat(DECIMAL_PATTERN).format(value)

    private fun setTextChangedListeners() {
        lateinit var valueInSourceTextWatcher: TextWatcher
        lateinit var valueInTargetTextWatcher: TextWatcher
        valueInSourceTextWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable) = Unit

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) = Unit

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                val valueToPrint = try {
                    DecimalFormat(DECIMAL_PATTERN).format(
                        viewModel.valueInTarget?.times(s.toString().toBigDecimal())
                    )
                } catch (e: NumberFormatException) {
                    ""
                }
                Log.d("LOG_WATCHER", "valueInSourceTextWatcher valueToPrint: $valueToPrint")
                binding.valueInTargetCurrencyEditText.apply {
                    removeTextChangedListener(valueInTargetTextWatcher)
                    setText(valueToPrint)
                    addTextChangedListener(valueInTargetTextWatcher)
                }

            }
        }
        valueInTargetTextWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable) = Unit

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) = Unit

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                val valueToPrint =
                    try {
                        DecimalFormat(DECIMAL_PATTERN).format(
                            viewModel.oneTargetVal?.times(
                                s.toString().replace(",", ".").toBigDecimal()
                            )
                        )
                    } catch (e: NumberFormatException) {
                        ""
                    }
                Log.d("LOG_WATCHER", "valueInTargetTextWatcher valueToPrint: $valueToPrint")
                binding.valueInBaseCurrencyEditText.apply {
                    removeTextChangedListener(valueInSourceTextWatcher)
                    setText(valueToPrint)
                    addTextChangedListener(valueInSourceTextWatcher)
                }

            }
        }
        binding.valueInBaseCurrencyEditText.addTextChangedListener(valueInSourceTextWatcher)
        binding.valueInTargetCurrencyEditText.addTextChangedListener(valueInTargetTextWatcher)
    }


    private fun calculateValueInTarget(value: BigDecimal, nominal: BigDecimal) =
        value.divide(nominal, DIVISION_SCALE, RoundingMode.HALF_UP)

    private fun calculateOneTargetVal(value: BigDecimal, nominal: BigDecimal) =
        nominal.divide(
            value, DIVISION_SCALE, RoundingMode.HALF_UP
        )
}
