package io.github.wykopmobilny.api.endpoints

import io.github.wykopmobilny.APP_KEY
import io.github.wykopmobilny.api.responses.DigResponse
import io.github.wykopmobilny.api.responses.DownvoterResponse
import io.github.wykopmobilny.api.responses.LinkCommentResponse
import io.github.wykopmobilny.api.responses.LinkResponse
import io.github.wykopmobilny.api.responses.LinkVoteResponse
import io.github.wykopmobilny.api.responses.RelatedResponse
import io.github.wykopmobilny.api.responses.UpvoterResponse
import io.github.wykopmobilny.api.responses.VoteResponse
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

interface LinksRetrofitApi {

    @GET("/links/upcoming/page/{page}/sort/{sortBy}/appkey/$APP_KEY")
    suspend fun getUpcoming(
        @Path("page") page: Int,
        @Path("sortBy") sortBy: String,
    ): WykopApiResponse<List<LinkResponse>>

    @GET("/links/promoted/page/{page}/appkey/$APP_KEY")
    suspend fun getPromoted(@Path("page") page: Int): WykopApiResponse<List<LinkResponse>>

    @GET("/links/observed/page/{page}/appkey/$APP_KEY")
    suspend fun getObserved(@Path("page") page: Int): WykopApiResponse<List<LinkResponse>>

    @GET("/links/comments/{linkId}/sort/{sortBy}/appkey/$APP_KEY")
    suspend fun getLinkComments(@Path("linkId") linkId: Long, @Path("sortBy") sortBy: String): WykopApiResponse<List<LinkCommentResponse>>

    @GET("/links/link/{linkId}/appkey/$APP_KEY")
    suspend fun getLink(@Path("linkId") linkId: Long): WykopApiResponse<LinkResponse>

    @GET("/links/commentVoteUp/{linkId}/{commentId}/appkey/$APP_KEY")
    suspend fun commentVoteUp(@Path("linkId") linkId: Long, @Path("commentId") commentId: Long): WykopApiResponse<LinkVoteResponse>

    @GET("/links/commentVoteDown/{linkId}/{commentId}/appkey/$APP_KEY")
    suspend fun commentVoteDown(@Path("linkId") linkId: Long, @Path("commentId") commentId: Long): WykopApiResponse<LinkVoteResponse>

    @GET("/links/commentVoteCancel/{linkId}/{commentId}/appkey/$APP_KEY")
    suspend fun commentVoteCancel(@Path("linkId") linkId: Long, @Path("commentId") commentId: Long): WykopApiResponse<LinkVoteResponse>

    @GET("/links/upvoters/{linkId}/appkey/$APP_KEY")
    suspend fun getUpvoters(@Path("linkId") linkId: Long): WykopApiResponse<List<UpvoterResponse>>

    @GET("/links/downvoters/{linkId}/appkey/$APP_KEY")
    suspend fun getDownvoters(@Path("linkId") linkId: Long): WykopApiResponse<List<DownvoterResponse>>

    @GET("/links/related/{linkId}/appkey/$APP_KEY")
    suspend fun getRelated(@Path("linkId") linkId: Long): WykopApiResponse<List<RelatedResponse>>

    @GET("/links/voteup/{linkId}/appkey/$APP_KEY")
    suspend fun voteUp(@Path("linkId") linkId: Long): WykopApiResponse<DigResponse>

    @GET("/links/votedown/{linkId}/{voteType}/appkey/$APP_KEY")
    suspend fun voteDown(
        @Path("linkId") linkId: Long,
        @Path("voteType") reason: Int,
    ): WykopApiResponse<DigResponse>

    @GET("/links/relatedvoteup/{linkId}/{relatedId}/appkey/$APP_KEY")
    suspend fun relatedVoteUp(@Path("linkId") linkId: Long, @Path("relatedId") relatedId: Long): WykopApiResponse<VoteResponse>

    @GET("/links/relatedvotedown/{linkId}/{relatedId}/appkey/$APP_KEY")
    suspend fun relatedVoteDown(@Path("linkId") linkId: Long, @Path("relatedId") relatedId: Long): WykopApiResponse<VoteResponse>

    @GET("/links/voteremove/{linkId}/appkey/$APP_KEY")
    suspend fun voteRemove(@Path("linkId") linkId: Long): WykopApiResponse<DigResponse>

    @Multipart
    @POST("/links/commentadd/{linkId}/{commentId}/appkey/$APP_KEY")
    suspend fun addComment(
        @Part("body") body: RequestBody,
        @Part("adultmedia") plus18: RequestBody,
        @Path("linkId") linkId: Long,
        @Path("commentId") commentId: Long,
        @Part file: MultipartBody.Part,
    ): WykopApiResponse<LinkCommentResponse>

    @FormUrlEncoded
    @POST("/links/commentadd/{linkId}/{commentId}/appkey/$APP_KEY")
    suspend fun addComment(
        @Field("body") body: String,
        @Path("linkId") linkId: Long,
        @Path("commentId") commentId: Long,
        @Field("embed") embed: String?,
        @Field("adultmedia") plus18: Boolean,
    ): WykopApiResponse<LinkCommentResponse>

    @POST("/links/commentdelete/{linkCommentId}/appkey/$APP_KEY")
    suspend fun deleteComment(@Path("linkCommentId") linkId: Long): WykopApiResponse<LinkCommentResponse>

    @Multipart
    @POST("/links/commentadd/{linkId}/appkey/$APP_KEY")
    suspend fun addComment(
        @Part("body") body: RequestBody,
        @Part("adultmedia") plus18: RequestBody,
        @Path("linkId") linkId: Long,
        @Part file: MultipartBody.Part,
    ): WykopApiResponse<LinkCommentResponse>

    @FormUrlEncoded
    @POST("/links/commentadd/{linkId}/appkey/$APP_KEY")
    suspend fun addComment(
        @Field("body") body: String,
        @Path("linkId") linkId: Long,
        @Field("embed") embed: String?,
        @Field("adultmedia") plus18: Boolean,
    ): WykopApiResponse<LinkCommentResponse>

    @FormUrlEncoded
    @POST("/links/relatedadd/{linkId}/appkey/$APP_KEY")
    suspend fun addRelated(
        @Field("title") body: String,
        @Path("linkId") linkId: Long,
        @Field("url") url: String,
        @Field("plus18") plus18: Boolean,
    ): WykopApiResponse<RelatedResponse>

    @FormUrlEncoded
    @POST("/links/commentedit/{linkId}/appkey/$APP_KEY")
    suspend fun editComment(
        @Field("body") body: String,
        @Path("linkId") linkId: Long,
    ): WykopApiResponse<LinkCommentResponse>

    @GET("/links/favorite/{linkId}/appkey/$APP_KEY")
    suspend fun toggleFavorite(@Path("linkId") linkId: Long): WykopApiResponse<List<Boolean>>
}
