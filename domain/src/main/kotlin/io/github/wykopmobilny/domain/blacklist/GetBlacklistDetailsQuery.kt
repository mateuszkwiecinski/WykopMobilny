package io.github.wykopmobilny.domain.blacklist

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.fresh
import io.github.wykopmobilny.domain.blacklist.di.BlacklistScope
import io.github.wykopmobilny.domain.repositories.ProfilesRepository
import io.github.wykopmobilny.domain.repositories.TagsRepository
import io.github.wykopmobilny.domain.utils.safe
import io.github.wykopmobilny.domain.utils.withResource
import io.github.wykopmobilny.storage.api.Blacklist
import io.github.wykopmobilny.kotlin.AppScopes
import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.ItemState
import io.github.wykopmobilny.ui.base.Resource
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.blacklist.BlacklistedDetailsUi
import io.github.wykopmobilny.ui.blacklist.BlacklistedElementUi
import io.github.wykopmobilny.ui.blacklist.GetBlacklistDetails
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal class GetBlacklistDetailsQuery @Inject constructor(
    private val store: Store<Unit, Blacklist>,
    private val viewState: BlacklistViewStateStorage,
    private val tagsRepository: TagsRepository,
    private val profilesRepository: ProfilesRepository,
    private val appScopes: AppScopes,
) : GetBlacklistDetails {

    override fun invoke() = combine(
        viewState.state,
        store.stream(StoreRequest.cached(Unit, refresh = false)),
    ) { viewStateUi, response ->
        val blacklist = response.dataOrNull()
        val content = if (blacklist == null || blacklist.users.isEmpty() && blacklist.tags.isEmpty()) {
            BlacklistedDetailsUi.Content.Empty(
                isLoading = viewStateUi.generalResource.isLoading,
                loadAction = ::refresh,
            )
        } else {
            BlacklistedDetailsUi.Content.WithData(
                users = BlacklistedDetailsUi.Content.WithData.ElementPage(
                    isRefreshing = viewStateUi.generalResource.isLoading,
                    refreshAction = ::refresh,
                    elements = blacklist.users.sorted()
                        .map { user ->
                            BlacklistedElementUi(
                                name = user,
                                state = when (val state = viewStateUi.usersState[user]) {
                                    is ItemState.Error -> BlacklistedElementUi.StateUi.Error(
                                        showError = {
                                            viewState.update {
                                                it.copy(
                                                    generalResource = Resource.error(
                                                        failedAction = FailedAction(
                                                            cause = state.error,
                                                            retryAction = { unblockUser(user) },
                                                        ),
                                                    ),
                                                )
                                            }
                                        },
                                    )
                                    ItemState.InProgress -> BlacklistedElementUi.StateUi.InProgress
                                    null -> BlacklistedElementUi.StateUi.Default(
                                        unblock = { unblockUser(user) },
                                    )
                                },
                            )
                        },
                ),
                tags = BlacklistedDetailsUi.Content.WithData.ElementPage(
                    isRefreshing = viewStateUi.generalResource.isLoading,
                    refreshAction = ::refresh,
                    elements = blacklist.tags.sorted()
                        .map { tag ->
                            BlacklistedElementUi(
                                name = tag,
                                state = when (val state = viewStateUi.tagsState[tag]) {
                                    is ItemState.Error -> BlacklistedElementUi.StateUi.Error(
                                        showError = {
                                            viewState.update {
                                                it.copy(
                                                    generalResource = Resource.error(
                                                        failedAction = FailedAction(
                                                            cause = state.error,
                                                            retryAction = { unblockTag(tag) },
                                                        ),
                                                    ),
                                                )
                                            }
                                        },
                                    )
                                    ItemState.InProgress -> BlacklistedElementUi.StateUi.InProgress
                                    null -> BlacklistedElementUi.StateUi.Default(
                                        unblock = { unblockTag(tag) },
                                    )
                                },
                            )
                        },
                ),
            )
        }

        BlacklistedDetailsUi(
            errorDialog = viewStateUi.generalResource.failedAction?.let { error ->
                ErrorDialogUi(
                    error = error.cause,
                    retryAction = error.retryAction,
                    dismissAction = {
                        appScopes.safe<BlacklistScope> { viewState.update { it.copy(generalResource = Resource.idle()) } }
                    },
                )
            },
            content = content,
        )
    }

    private fun refresh(): Unit = appScopes.safe<BlacklistScope> {
        withResource(
            refresh = { store.fresh(Unit) },
            update = { resource -> viewState.update { it.copy(generalResource = resource) } },
            launch = { callback -> appScopes.safe<BlacklistScope>(block = callback) },
        )
    }

    private fun unblockTag(tag: String) = appScopes.safe<BlacklistScope> {
        viewState.update { it.copy(tagsState = it.tagsState + (tag to ItemState.InProgress)) }
        runCatching { tagsRepository.unblockTag(tag) }
            .onFailure { error -> viewState.update { it.copy(tagsState = it.tagsState + (tag to ItemState.Error(error))) } }
            .onSuccess { viewState.update { it.copy(tagsState = it.tagsState - tag) } }
    }

    private fun unblockUser(user: String) = appScopes.safe<BlacklistScope> {
        viewState.update { it.copy(usersState = it.usersState + (user to ItemState.InProgress)) }
        runCatching { runCatching { profilesRepository.unblockUser(user) } }
            .onFailure { error -> viewState.update { it.copy(usersState = it.usersState + (user to ItemState.Error(error))) } }
            .onSuccess { viewState.update { it.copy(usersState = it.usersState - user) } }
    }
}
