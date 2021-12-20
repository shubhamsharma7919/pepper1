package com.example.projemanag.activities.Common

import com.example.projemanag.model.Results
import com.example.projemanag.remote.IGoogleAPIService
import com.example.projemanag.remote.RetrofitClient
import com.example.projemanag.remote.RetrofitScalarsClient

object Common {

        private val GOOGLE_API_URL="https://maps.googleapis.com/"
        var currentResult:Results?=null

        val googleApiService:IGoogleAPIService
        get()=RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)

        val googleApiServiceScalars:IGoogleAPIService
                get()= RetrofitScalarsClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)


}