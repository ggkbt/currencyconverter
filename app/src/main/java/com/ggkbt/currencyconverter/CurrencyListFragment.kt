package com.ggkbt.currencyconverter

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.CreateMethod
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ggkbt.currencyconverter.databinding.FragmentCurrencyListBinding
import com.ggkbt.currencyconverter.enums.Currency
import com.ggkbt.currencyconverter.enums.ListSelector


class CurrencyListFragment : Fragment() {
    private val binding: FragmentCurrencyListBinding by viewBinding(CreateMethod.INFLATE)
    private val viewModel: CurrencyViewModel by viewModels()
    private val adapter = CurrencyRecyclerAdapter()

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
        Log.d("TAG_LIST", "viewModel.prefs.isCbr: ${viewModel.prefs.isCbr}")
        Log.d("TAG_LIST", "listSelector: $listSelector")
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
                adapter.submitListWithSelector(list, ListSelector.SOURCE)
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
                    adapter.submitListWithSelector(currenciesList, listSelector)
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