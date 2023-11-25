package com.ggkbt.currencyconverter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ggkbt.currencyconverter.databinding.CurrencyItemBinding
import com.ggkbt.currencyconverter.enums.Currency
import com.ggkbt.currencyconverter.enums.ListSelector

class CurrencyRecyclerAdapter :
    ListAdapter<Currency, CurrencyRecyclerAdapter.CurrencyItemViewHolder>(
        CurrencyItemDiffCallback()
    ) {

    private lateinit var listSelector: ListSelector

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val currencyItemBinding = CurrencyItemBinding.inflate(layoutInflater, parent, false)
        return CurrencyItemViewHolder(currencyItemBinding)
    }

    override fun onBindViewHolder(holder: CurrencyItemViewHolder, position: Int) {
        val currencyItem = currentList[position]
        holder.bind(currencyItem, listSelector)
    }

    fun submitListWithSelector(list: List<Currency>?, listSelector: ListSelector) {
        super.submitList(list)
        this.listSelector = listSelector
    }

    class CurrencyItemViewHolder(private val currencyItemBinding: CurrencyItemBinding) :
        RecyclerView.ViewHolder(currencyItemBinding.root) {
        fun bind(currency: Currency, listSelector: ListSelector) {
            val context = itemView.context
            currencyItemBinding.apply {
                codeTextView.text = currency.name
                nameTextView.text = context.resources.getString(currency.currencyNameResId)
                flagImageView.setImageResource(currency.flagResId)
            }
            itemView.setOnClickListener { view: View ->
                val bundle = Bundle()
                bundle.putParcelable("selected_currency", currency)
                bundle.putParcelable("list_selector", listSelector)
                view.findNavController()
                    .navigate(R.id.action_currencyListFragment_to_calculatorFragment, bundle)
            }
        }
    }

    class CurrencyItemDiffCallback : DiffUtil.ItemCallback<Currency>() {

        override fun areItemsTheSame(oldItem: Currency, newItem: Currency) =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Currency, newItem: Currency) =
            oldItem == newItem
    }

}