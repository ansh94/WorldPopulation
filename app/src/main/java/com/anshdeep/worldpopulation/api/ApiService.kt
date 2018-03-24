package com.anshdeep.worldpopulation.api

import com.anshdeep.worldpopulation.api.model.CountryResult
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Created by ansh on 23/03/18.
 */
interface ApiService {


    @GET("jsonparsetutorial.txt")
    fun getCountries(): Observable<CountryResult>

    companion object Factory {
        private const val BASE_URL = "http://www.androidbegin.com/tutorial/"

        fun getApiService(): ApiService {
            return Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
                    .create(ApiService::class.java)
        }
    }
}