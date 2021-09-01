package io.github.wykopmobilny.work

import io.github.wykopmobilny.ui.base.Query

interface GetBlacklistRefreshWorkDetails : Query<WorkData>

data class WorkData(
    val onWorkRequested: suspend () -> Result<Unit>,
)
