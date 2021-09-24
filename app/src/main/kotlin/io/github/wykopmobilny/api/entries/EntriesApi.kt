package io.github.wykopmobilny.api.entries

import io.github.wykopmobilny.api.WykopImageFile
import io.github.wykopmobilny.api.responses.EntryCommentResponse
import io.github.wykopmobilny.api.responses.EntryResponse
import io.github.wykopmobilny.api.responses.FavoriteResponse
import io.github.wykopmobilny.api.responses.VoteResponse
import io.github.wykopmobilny.models.dataclass.Entry
import io.github.wykopmobilny.models.dataclass.EntryVotePublishModel
import io.github.wykopmobilny.models.dataclass.Survey
import io.github.wykopmobilny.models.dataclass.Voter
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface EntriesApi {

    val entryVoteSubject: PublishSubject<EntryVotePublishModel>
    val entryUnVoteSubject: PublishSubject<EntryVotePublishModel>

    fun voteEntry(entryId: Long): Single<VoteResponse>
    fun unvoteEntry(entryId: Long): Single<VoteResponse>
    fun voteComment(commentId: Long): Single<VoteResponse>
    fun unvoteComment(commentId: Long): Single<VoteResponse>
    fun addEntry(body: String, wykopImageFile: WykopImageFile, plus18: Boolean): Single<EntryResponse>
    fun addEntry(body: String, embed: String?, plus18: Boolean): Single<EntryResponse>
    fun addEntryComment(body: String, entryId: Long, embed: String?, plus18: Boolean): Single<EntryCommentResponse>
    fun addEntryComment(body: String, entryId: Long, wykopImageFile: WykopImageFile, plus18: Boolean): Single<EntryCommentResponse>
    fun markFavorite(entryId: Long): Single<FavoriteResponse>
    fun deleteEntry(entryId: Long): Single<EntryResponse>
    fun editEntry(body: String, entryId: Long): Single<EntryResponse>
    fun editEntryComment(body: String, commentId: Long): Single<EntryCommentResponse>
    fun deleteEntryComment(commentId: Long): Single<EntryCommentResponse>
    fun voteSurvey(entryId: Long, answerId: Int): Single<Survey>

    fun getHot(page: Int, period: String): Single<List<Entry>>
    fun getStream(page: Int): Single<List<Entry>>
    fun getActive(page: Int): Single<List<Entry>>
    fun getObserved(page: Int): Single<List<Entry>>
    fun getEntry(id: Long): Single<Entry>
    fun getEntryVoters(id: Long): Single<List<Voter>>
    fun getEntryCommentVoters(id: Long): Single<List<Voter>>
}
