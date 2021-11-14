package io.github.wykopmobilny.patrons.api

import retrofit2.http.GET

interface PatronsRetrofitApi {

    @GET("otwarty-wykop-mobilny/owm-patrons/master/patrons.json")
    suspend fun getPatrons(): PatronsResponse
}
