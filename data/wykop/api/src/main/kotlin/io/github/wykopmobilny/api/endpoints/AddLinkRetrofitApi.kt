package io.github.wykopmobilny.api.endpoints

import io.github.wykopmobilny.APP_KEY
import io.github.wykopmobilny.api.responses.WykopApiResponse
import io.github.wykopmobilny.api.responses.AddLinkPreviewImage
import io.github.wykopmobilny.api.responses.LinkResponse
import io.github.wykopmobilny.api.responses.NewLinkResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AddLinkRetrofitApi {
    @FormUrlEncoded
    @POST("addlink/draft/appkey/$APP_KEY")
    suspend fun getDraft(@Field("url") url: String): NewLinkResponse

    @GET("/addlink/images/key/{key}/appkey/$APP_KEY")
    suspend fun getImages(@Path("key") key: String): WykopApiResponse<List<AddLinkPreviewImage>>

    @FormUrlEncoded
    @POST("/addlink/add/key/{key}/appkey/$APP_KEY")
    suspend fun publishLink(
        @Path("key") key: String,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("tags") tags: String,
        @Field("photo") photo: String? = null,
        @Field("url") url: String,
        @Field("plus18") plus18: Int,
    ): WykopApiResponse<LinkResponse>
}
