package com.anshdeep.worldpopulation.api.model

import com.google.gson.annotations.SerializedName

/**
 * Created by ansh on 23/03/18.
 */
data class CountryResult(
        @SerializedName("worldpopulation") val worldpopulation : List<WorldPopulation>
)