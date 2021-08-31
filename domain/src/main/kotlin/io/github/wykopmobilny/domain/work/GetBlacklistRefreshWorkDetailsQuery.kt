package io.github.wykopmobilny.domain.work

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.fresh
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.storage.api.Blacklist
import io.github.wykopmobilny.storage.api.SessionStorage
import io.github.wykopmobilny.work.GetBlacklistRefreshWorkDetails
import io.github.wykopmobilny.work.WorkData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

internal class GetBlacklistRefreshWorkDetailsQuery @Inject constructor(
    private val sessionStorage: SessionStorage,
    private val store: Store<Unit, Blacklist>,
) : GetBlacklistRefreshWorkDetails {

    override fun invoke(): Flow<WorkData> =
        flowOf(
            WorkData(
                onWorkRequested = {
                    if (sessionStorage.session.first() != null) {
                        runCatching { store.fresh(Unit); Unit }
                    } else {
                        Napier.i("User not logged in, skipping blacklist refresh")
                        Result.success(Unit)
                    }
                },
            ),
        )
}
