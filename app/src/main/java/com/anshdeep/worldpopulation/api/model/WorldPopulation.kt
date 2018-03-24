package com.anshdeep.worldpopulation.api.model

import com.google.gson.annotations.SerializedName


/**
 * Created by ansh on 23/03/18.
 */
data class WorldPopulation(
        @SerializedName("rank") val rank: Int,
        @SerializedName("country") val country: String,
        @SerializedName("population") val population: String,
        @SerializedName("flag") val flag: String
)