package com.ggkbt.currencyconverter.view

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ggkbt.currencyconverter.ErrorHelper
import com.ggkbt.currencyconverter.PairsRecyclerAdapter
import com.ggkbt.currencyconverter.R
import com.ggkbt.currencyconverter.databinding.FragmentCalculatorBinding
import com.ggkbt.currencyconverter.enums.Currency
import com.ggkbt.currencyconverter.enums.ListSelector
import com.ggkbt.currencyconverter.model.FavoritePair
import com.ggkbt.currencyconverter.viewmodel.CurrencyViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat


class CalculatorFragment : Fragment() {
    companion object {
        private const val DIVISION_SCALE = 8
        private const val DECIMAL_PATTERN = "#0.####"
    }


    private var isValueInSourceTextWatcherActive = true
    private var isValueInTargetTextWatcherActive = true
    private lateinit var valueInSourceTextWatcher: TextWatcher
    private lateinit var valueInTargetTextWatcher: TextWatcher

    private val binding: FragmentCalculatorBinding by viewBinding(CreateMethod.INFLATE)
    private val viewModel: CurrencyViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private val adapter = PairsRecyclerAdapter(
        listener = { pair ->
            onSetFromFavorites(pair)
        },
        onDeletePair = { pair ->
            viewModel.deletePairFromDb(pair)
        }
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("TAG123", "CalculatorFragment viewModel: ${viewModel.hashCode()}")

        viewModel.nullOldValues()
        Log.d("TAG123", "onViewCreated oldSourceValue: ${viewModel.oldSourceValue}")
        valueInSourceTextWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable) = Unit

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) = Unit

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                Log.d("TAG", "isValueInSourceTextWatcherActive: $isValueInSourceTextWatcherActive, s: $s, oldSourceValue: ${viewModel.oldSourceValue}")
                if (!isValueInSourceTextWatcherActive || s.toString() == viewModel.oldSourceValue) return
                viewModel.oldSourceValue = s.toString()
                isValueInTargetTextWatcherActive = false
                binding.valueInTargetCurrencyEditText.removeTextChangedListener(valueInTargetTextWatcher)
                val valueToPrint = try {
                    DecimalFormat(DECIMAL_PATTERN).format(
                        viewModel.valueInTarget?.times(s.toString().toBigDecimal())
                    )
                } catch (e: NumberFormatException) {
                    ""
                }
                Log.d("TAG", "valueInSourceTextWatcher valueToPrint: $valueToPrint, current line: $s")
                binding.valueInTargetCurrencyEditText.setText(valueToPrint)
                isValueInTargetTextWatcherActive = true
                binding.valueInTargetCurrencyEditText.addTextChangedListener(valueInTargetTextWatcher)
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
                Log.d("TAG", "isValueInTargetTextWatcherActive: $isValueInTargetTextWatcherActive, s: $s, oldTargetValue: ${viewModel.oldTargetValue}")
                if (!isValueInTargetTextWatcherActive || s.toString() == viewModel.oldTargetValue) return
                viewModel.oldTargetValue = s.toString()
                isValueInSourceTextWatcherActive = false
                binding.valueInBaseCurrencyEditText.removeTextChangedListener(valueInSourceTextWatcher)
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
                Log.d("TAG", "valueInTargetTextWatcher valueToPrint: $valueToPrint, current line: $s")
                binding.valueInBaseCurrencyEditText.setText(valueToPrint)
                isValueInSourceTextWatcherActive = true
                binding.valueInBaseCurrencyEditText.addTextChangedListener(valueInSourceTextWatcher)

            }
        }

        with(binding) {
            iwSettings.setOnClickListener {
                view.findNavController()
                    .navigate(R.id.action_calculatorFragment_to_settingsFragment)
            }

            currencyPairs.adapter = adapter
            binding.currencyPairs.apply {
                layoutManager = LinearLayoutManager(context)
            }
            baseCurrencySelectorContainer.setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelable("list_selector", ListSelector.SOURCE)
                it.findNavController()
                    .navigate(R.id.action_calculatorFragment_to_currencyListFragment, bundle)
            }
        }
        viewModel.getFilteredFavoritePairs().observe(viewLifecycleOwner) { pairs ->
            Log.d("TAG123", "fragment pairs: $pairs")
            setContainsInFavs(pairs)
            adapter.submitList(pairs)
        }
        viewModel.isCbrLiveData.observe(viewLifecycleOwner) { isCbr ->
            if (isCbr) {
                setCbrCase()
            } else {
                setXrCase()
            }
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
            Log.d("TAG_SWIPE", "swipe")
            removeTextChangeListeners() // <- удаляю TextWatchers
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
                    Log.d("TAG123", "baseCurrency: $baseCurrency, targetCurrency: $targetCurrency")
                    onSuccess(
                        nominal = rates[baseCurrency.name] ?: BigDecimal(-1),
                        value = rates[targetCurrency.name] ?: BigDecimal(-1)
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setContainsInFavs(pairs: List<FavoritePair>) {
        if (pairs.any { pair ->
                pair.from == viewModel.baseCurrency.name && pair.to == viewModel.targetCurrency.name
            }) {
            binding.ivAddToFavsStar.setImageResource(R.drawable.star_filled)
            binding.tvAddToFavs.text = getString(R.string.remove_from_favs)
            binding.llAddToFavs.setOnClickListener {
                viewModel.deletePairFromDb(
                    FavoritePair(
                        viewModel.baseCurrency.name,
                        viewModel.targetCurrency.name
                    )
                )
            }
        } else {
            binding.ivAddToFavsStar.setImageResource(R.drawable.star_empty)
            binding.tvAddToFavs.text = getString(R.string.add_to_favs)
            binding.llAddToFavs.setOnClickListener {
                viewModel.writePairToDb(
                    FavoritePair(
                        viewModel.baseCurrency.name,
                        viewModel.targetCurrency.name
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onSetFromFavorites(pair: FavoritePair) {
        viewModel.nullOldValues()
        viewModel.updatePair(Currency.fromCharCode(pair.from), Currency.fromCharCode(pair.to))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onSuccess(value: BigDecimal, nominal: BigDecimal) {
        Log.d("TAG123", "onSuccess")
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
        }
        Log.d("TAG_VALUES", "value: $value, nominal: $nominal")
        with(viewModel) {
            oneTargetVal = calculateOneTargetVal(value, nominal)
            valueInTarget = calculateValueInTarget(value, nominal)
        }
        setTextChangedListeners()
        binding.valueInBaseCurrencyEditText.setText(
            formatBigDecimal(nominal)
        )
        Log.d(
            "TAG_VALUES",
            "oneTargetVal: ${viewModel.oneTargetVal}, valueInTarget: ${viewModel.valueInTarget}"
        )
    }

    private fun formatBigDecimal(value: BigDecimal): String =
        DecimalFormat(DECIMAL_PATTERN).format(value)

    private fun setTextChangedListeners() {
        isValueInSourceTextWatcherActive = true
        isValueInTargetTextWatcherActive = true
        binding.valueInBaseCurrencyEditText.addTextChangedListener(valueInSourceTextWatcher)
        binding.valueInTargetCurrencyEditText.addTextChangedListener(valueInTargetTextWatcher)
    }

    private fun removeTextChangeListeners() {
        isValueInSourceTextWatcherActive = false
        isValueInTargetTextWatcherActive = false
        binding.valueInTargetCurrencyEditText.removeTextChangedListener(valueInTargetTextWatcher)
        binding.valueInBaseCurrencyEditText.removeTextChangedListener(valueInSourceTextWatcher)
    }


    private fun calculateValueInTarget(value: BigDecimal, nominal: BigDecimal) =
        value.divide(nominal, DIVISION_SCALE, RoundingMode.HALF_UP)

    private fun calculateOneTargetVal(value: BigDecimal, nominal: BigDecimal) =
        nominal.divide(
            value, DIVISION_SCALE, RoundingMode.HALF_UP
        )
}
