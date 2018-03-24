package com.anshdeep.worldpopulation.data

import com.anshdeep.worldpopulation.api.ApiService
import com.anshdeep.worldpopulation.api.model.CountryResult
import io.reactivex.Observable

/**
 * Created by ansh on 23/03/18.
 */
class CountryRemoteDataSource {
    private val apiService: ApiService = ApiService.getApiService()

    fun getCountries(): Observable<CountryResult> {
        return apiService.getCountries()
    }
}