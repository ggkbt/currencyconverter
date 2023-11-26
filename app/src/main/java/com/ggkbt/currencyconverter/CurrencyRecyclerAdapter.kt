package com.ggkbt.currencyconverter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ggkbt.currencyconverter.databinding.CurrencyItemBinding
import com.ggkbt.currencyconverter.enums.Currency

class CurrencyRecyclerAdapter(private val listener: (currency: Currency) -> Unit) :
    ListAdapter<Currency, CurrencyRecyclerAdapter.CurrencyItemViewHolder>(
        CurrencyItemDiffCallback()
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val currencyItemBinding = CurrencyItemBinding.inflate(layoutInflater, parent, false)
        return CurrencyItemViewHolder(currencyItemBinding)
    }

    override fun onBindViewHolder(holder: CurrencyItemViewHolder, position: Int) {
        val currencyItem = currentList[position]
        holder.bind(currencyItem, listener)
    }

    class CurrencyItemViewHolder(private val currencyItemBinding: CurrencyItemBinding) :
        RecyclerView.ViewHolder(currencyItemBinding.root) {
        fun bind(
            currency: Currency,
            listener: (currency: Currency) -> Unit
        ) {
            val context = itemView.context
            currencyItemBinding.apply {
                codeTextView.text = currency.name
                nameTextView.text = context.resources.getString(currency.currencyNameResId)
                flagImageView.setImageResource(currency.flagResId)
            }
            itemView.setOnClickListener {
                listener(currency)
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