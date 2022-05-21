package io.github.wykopmobilny.api.endpoints

import io.github.wykopmobilny.APP_KEY
import io.github.wykopmobilny.api.responses.EntryCommentResponse
import io.github.wykopmobilny.api.responses.EntryResponse
import io.github.wykopmobilny.api.responses.FavoriteResponse
import io.github.wykopmobilny.api.responses.SurveyResponse
import io.github.wykopmobilny.api.responses.VoteResponse
import io.github.wykopmobilny.api.responses.VoterResponse
import io.github.wykopmobilny.api.responses.WykopApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface EntriesRetrofitApi {

    @GET("/entries/hot/page/{page}/period/{period}/appkey/$APP_KEY")
    suspend fun getHot(@Path("page") page: Int, @Path("period") period: String): WykopApiResponse<List<EntryResponse>>

    @GET("/entries/stream/page/{page}/appkey/$APP_KEY")
    suspend fun getStream(@Path("page") page: Int): WykopApiResponse<List<EntryResponse>>

    @GET("/entries/active/page/{page}/appkey/$APP_KEY")
    suspend fun getActive(@Path("page") page: Int): WykopApiResponse<List<EntryResponse>>

    @GET("/entries/observed/page/{page}/appkey/$APP_KEY")
    suspend fun getObserved(@Path("page") page: Int): WykopApiResponse<List<EntryResponse>>

    @GET("/entries/entry/{id}/appkey/$APP_KEY")
    suspend fun getEntry(@Path("id") id: Long): WykopApiResponse<EntryResponse>

    @GET("/entries/voteup/{entryId}/appkey/$APP_KEY")
    suspend fun voteEntry(@Path("entryId") entryId: Long): WykopApiResponse<VoteResponse>

    @GET("/entries/voteremove/{entryId}/appkey/$APP_KEY")
    suspend fun unvoteEntry(@Path("entryId") entryId: Long): WykopApiResponse<VoteResponse>

    @GET("/entries/commentvoteup/{commentId}/appkey/$APP_KEY")
    suspend fun voteComment(@Path("commentId") commentId: Long): WykopApiResponse<VoteResponse>

    @GET("/entries/commentvoteremove/{commentId}/appkey/$APP_KEY")
    suspend fun unvoteComment(@Path("commentId") commentId: Long): WykopApiResponse<VoteResponse>

    @GET("/entries/favorite/{entryId}/appkey/$APP_KEY")
    suspend fun markFavorite(@Path("entryId") entryId: Long): WykopApiResponse<FavoriteResponse>

    @GET("/entries/surveyvote/{entryId}/{answerId}/appkey/$APP_KEY")
    suspend fun voteSurvey(@Path("entryId") entryId: Long, @Path("answerId") answerId: Int): WykopApiResponse<SurveyResponse>

    @Multipart
    @POST("/entries/add/appkey/$APP_KEY")
    suspend fun addEntry(
        @Part("body") body: RequestBody,
        @Part("adultmedia") plus18: RequestBody,
        @Part file: MultipartBody.Part,
    ): WykopApiResponse<EntryResponse>

    @GET("/entries/upvoters/{entryId}/appkey/$APP_KEY")
    suspend fun getEntryVoters(@Path("entryId") entryId: Long): WykopApiResponse<List<VoterResponse>>

    @GET("/entries/commentupvoters/{commentId}/appkey/$APP_KEY")
    suspend fun getCommentUpvoters(@Path("commentId") commentId: Long): WykopApiResponse<List<VoterResponse>>

    @FormUrlEncoded
    @POST("/entries/add/appkey/$APP_KEY")
    suspend fun addEntry(
        @Field("body") body: String,
        @Field("embed") embed: String?,
        @Field("adultmedia") plus18: Boolean,
    ): WykopApiResponse<EntryResponse>

    @Multipart
    @POST("/entries/commentadd/{entryId}/appkey/$APP_KEY")
    suspend fun addEntryComment(
        @Part("body") body: RequestBody,
        @Part("adultmedia") plus18: RequestBody,
        @Path("entryId") entryId: Long,
        @Part file: MultipartBody.Part,
    ): WykopApiResponse<EntryCommentResponse>

    @FormUrlEncoded
    @POST("/entries/commentadd/{entryId}/appkey/$APP_KEY")
    suspend fun addEntryComment(
        @Field("body") body: String,
        @Field("embed") embed: String?,
        @Field("adultmedia") plus18: Boolean,
        @Path("entryId") entryId: Long,
    ): WykopApiResponse<EntryCommentResponse>

    @Multipart
    @POST("/entries/edit/{entryId}/appkey/$APP_KEY")
    suspend fun editEntry(
        @Part("body") body: RequestBody,
        @Part("adultmedia") plus18: RequestBody,
        @Path("entryId") entryId: Long,
        @Part file: MultipartBody.Part,
    ): WykopApiResponse<EntryCommentResponse>

    @FormUrlEncoded
    @POST("/entries/edit/{entryId}/appkey/$APP_KEY")
    suspend fun editEntry(
        @Field("body") body: String,
        @Field("embed") embed: String?,
        @Field("adultmedia") plus18: Boolean,
        @Path("entryId") entryId: Long,
    ): WykopApiResponse<EntryCommentResponse>

    @GET("/entries/delete/{entryId}/appkey/$APP_KEY")
    suspend fun deleteEntry(@Path("entryId") entryId: Long): WykopApiResponse<EntryResponse>

    @Multipart
    @POST("/entries/commentedit/{commentId}/appkey/$APP_KEY")
    suspend fun editEntryComment(
        @Part("body") body: RequestBody,
        @Part("adultmedia") plus18: RequestBody,
        @Path("commentId") commentId: Long,
        @Part file: MultipartBody.Part,
    ): WykopApiResponse<EntryCommentResponse>

    @FormUrlEncoded
    @POST("/entries/commentedit/{commentId}/appkey/$APP_KEY")
    suspend fun editEntryComment(
        @Field("body") body: String,
        @Field("embed") embed: String?,
        @Field("adultmedia") plus18: Boolean,
        @Path("commentId") commentId: Long,
    ): WykopApiResponse<EntryCommentResponse>

    @GET("/entries/commentdelete/{commentId}/appkey/$APP_KEY")
    suspend fun deleteEntryComment(@Path("commentId") commentId: Long): WykopApiResponse<EntryCommentResponse>
}
