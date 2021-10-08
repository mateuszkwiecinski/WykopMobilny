package io.github.wykopmobilny.work

interface WorkRequestDetails : () -> WorkData

interface GetNotificationsRefreshWorkDetails : WorkRequestDetails

interface GetBlacklistRefreshWorkDetails : WorkRequestDetails

data class WorkData(
    val onWorkRequested: suspend () -> Result<Unit>,
)
