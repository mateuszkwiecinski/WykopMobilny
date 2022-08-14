package io.github.wykopmobilny.models.dataclass

import io.github.wykopmobilny.api.responses.VoteResponse

data class EntryVotePublishModel(
    val entryId: Long,
    val voteResponse: VoteResponse,
)
