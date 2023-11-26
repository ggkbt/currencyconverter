package com.ggkbt.currencyconverter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ggkbt.currencyconverter.databinding.CurrencyPairItemBinding
import com.ggkbt.currencyconverter.enums.Currency
import com.ggkbt.currencyconverter.model.FavoritePair

class PairsRecyclerAdapter(
    private val listener: (pair: FavoritePair) -> Unit,
    private val onDeletePair: (pair: FavoritePair) -> Unit
) :
    ListAdapter<FavoritePair, PairsRecyclerAdapter.PairViewHolder>(
        CurrencyItemDiffCallback()
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PairViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val currencyPairItemBinding = CurrencyPairItemBinding.inflate(layoutInflater, parent, false)
        return PairViewHolder(currencyPairItemBinding, listener, onDeletePair)
    }

    override fun onBindViewHolder(holder: PairViewHolder, position: Int) {
        val pair = currentList[position]
        holder.bind(pair)
    }

    class PairViewHolder(
        private val pairItemBinding: CurrencyPairItemBinding,
        private val listener: (pair: FavoritePair) -> Unit,
        private val onDeletePair: (pair: FavoritePair) -> Unit
    ) :
        RecyclerView.ViewHolder(pairItemBinding.root) {
        fun bind(pair: FavoritePair) {
            val context = itemView.context
            itemView.setOnClickListener {
                listener(pair)
            }
            pairItemBinding.apply {
                pairTextView.text =
                    context.resources.getString(R.string.from_to, pair.from, pair.to)
                flagFrom.setImageResource(Currency.fromCharCode(pair.from).flagResId)
                flagTo.setImageResource(Currency.fromCharCode(pair.to).flagResId)
                ivStarFav.setImageResource(R.drawable.star_filled)
                ivStarFav.setOnClickListener {
                    onDeletePair(pair)
                }
            }
        }
    }

    class CurrencyItemDiffCallback : DiffUtil.ItemCallback<FavoritePair>() {

        override fun areContentsTheSame(oldItem: FavoritePair, newItem: FavoritePair) =
            (oldItem.from == newItem.from) && (oldItem.to == newItem.to)

        override fun areItemsTheSame(oldItem: FavoritePair, newItem: FavoritePair): Boolean =
            oldItem == newItem
    }

}