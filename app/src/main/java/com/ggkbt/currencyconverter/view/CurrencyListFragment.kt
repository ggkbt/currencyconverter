package com.ggkbt.currencyconverter.view

import android.os.Build
import android.os.Bundle
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
import com.ggkbt.currencyconverter.CurrencyRecyclerAdapter
import com.ggkbt.currencyconverter.ErrorHelper
import com.ggkbt.currencyconverter.R
import com.ggkbt.currencyconverter.databinding.FragmentCurrencyListBinding
import com.ggkbt.currencyconverter.enums.Currency
import com.ggkbt.currencyconverter.enums.ListSelector
import com.ggkbt.currencyconverter.viewmodel.CurrencyViewModel


class CurrencyListFragment : Fragment() {
    private val binding: FragmentCurrencyListBinding by viewBinding(CreateMethod.INFLATE)
    private val viewModel: CurrencyViewModel by activityViewModels()
    @RequiresApi(Build.VERSION_CODES.O)
    private val adapter = CurrencyRecyclerAdapter { currency ->
        if (listSelector == ListSelector.SOURCE) {
            viewModel.updatePair(newBase = currency)
        }
        if (listSelector == ListSelector.TARGET) {
            viewModel.updatePair(newTarget = currency)
        }
        view?.findNavController()
            ?.navigate(R.id.action_currencyListFragment_to_calculatorFragment)
    }

    companion object {
        const val LIST_SELECTOR = "list_selector"
    }

    private lateinit var listSelector: ListSelector
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listSelector = arguments?.getParcelable(LIST_SELECTOR) ?: ListSelector.UNDEFINED
        if (viewModel.prefs.isCbr) setCbrCase() else setXrCase()
        viewModel.getError().observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.apply {
                    progressBar.visibility = View.GONE
                    if (currencyList.visibility == View.VISIBLE && swipeContainer.isRefreshing) {
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
                    if (currencyList.visibility == View.GONE) {
                        errorTextView.text =
                            context?.let {
                                error.first?.let { it1 ->
                                    ErrorHelper(it).getFetchingError(
                                        it1
                                    )
                                }
                            }
                        errorLayout.visibility = View.VISIBLE
                    }
                    swipeContainer.isRefreshing = false
                }
            }

        }

        binding.swipeContainer.setOnRefreshListener {
            viewModel.refreshData()
        }
        binding.retryButton.setOnClickListener {
            binding.errorLayout.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            viewModel.refreshData()
        }
        binding.currencyList.apply {
            layoutManager = LinearLayoutManager(context)
        }
        binding.currencyList.adapter = adapter

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCbrCase() {
        viewModel.getCbrData().observe(viewLifecycleOwner) { currenciesList ->
            Log.d(
                "TAG_LIST",
                "viewModel.getCbrData().observe(viewLifecycleOwner) currenciesList: $currenciesList"
            )
            val list = currenciesList.map {
                Currency.fromCharCode(it.charCode)
            }
            if (currenciesList != null) {
                adapter.submitList(list)
                binding.errorLayout.visibility = View.GONE
                binding.currencyList.visibility = View.VISIBLE
            }
            binding.apply {
                swipeContainer.isRefreshing = false
                progressBar.visibility = View.GONE
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setXrCase() {
        viewModel.getXrData().observe(viewLifecycleOwner) { rates ->
            if (rates != null) {
                val currenciesList = rates.map { entry ->
                    Currency.fromCharCode(entry.key)
                }
                if (listSelector != ListSelector.UNDEFINED) {
                    adapter.submitList(currenciesList)
                }
                binding.errorLayout.visibility = View.GONE
                binding.currencyList.visibility = View.VISIBLE
            }
            binding.apply {
                swipeContainer.isRefreshing = false
                progressBar.visibility = View.GONE
            }

        }
    }

}