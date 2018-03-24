package com.anshdeep.worldpopulation.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anshdeep.worldpopulation.R
import com.anshdeep.worldpopulation.api.model.WorldPopulation
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.rv_item_countries.view.*
import java.util.*

/**
 * Created by ansh on 23/03/18.
 */
class CountriesAdapter(
        private val listener: (WorldPopulation) -> Unit
) : RecyclerView.Adapter<CountriesAdapter.CountryViewHolder>() {

    private var data: List<WorldPopulation> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        return CountryViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.rv_item_countries, parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) = holder.bind(data[position],listener)

    fun swapData(data: List<WorldPopulation>) {
        this.data = data
        notifyDataSetChanged()
    }

    class CountryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: WorldPopulation, listener: (WorldPopulation) -> Unit) = with(itemView) {

            Glide.with(this)
                    .load(item.flag)
                    .into(country_flag)

            country_name.text = item.country
            country_population.text = item.population

            setOnClickListener {
                listener(item)
            }
        }
    }
}