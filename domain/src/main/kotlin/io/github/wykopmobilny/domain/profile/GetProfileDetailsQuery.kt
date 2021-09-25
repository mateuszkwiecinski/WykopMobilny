package io.github.wykopmobilny.domain.profile

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import io.github.wykopmobilny.data.cache.api.ProfileDetailsView
import io.github.wykopmobilny.domain.blacklist.actions.ProfilesRepository
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.InteropRequestsProvider
import io.github.wykopmobilny.domain.profile.di.ProfileScope
import io.github.wykopmobilny.storage.api.UserInfoStorage
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.SimpleViewStateStorage
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.base.launchInKeyed
import io.github.wykopmobilny.ui.profile.BanReasonUi
import io.github.wykopmobilny.ui.profile.ContextMenuOptionUi
import io.github.wykopmobilny.ui.profile.GetProfileDetails
import io.github.wykopmobilny.ui.profile.ProfileDetailsUi
import io.github.wykopmobilny.ui.profile.ProfileHeaderUi
import io.github.wykopmobilny.ui.profile.ProfileMenuOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import javax.inject.Inject

private val ProfileDetailsView.userInfo
    get() = UserInfo(
        profileId = id,
        avatarUrl = avatar,
        rank = rank?.takeIf { it > 0 },
        gender = gender?.toGenderDomain(),
        color = color.toColorDomain(),
    )

internal class GetProfileDetailsQuery @Inject constructor(
    @ProfileId private val profileId: String,
    private val profileStore: Store<Unit, ProfileDetailsView>,
    private val userInfoStorage: UserInfoStorage,
    private val profilesRepository: ProfilesRepository,
    private val appScopes: AppScopes,
    private val viewStateStorage: SimpleViewStateStorage,
    private val clock: Clock,
    private val interopRequests: InteropRequestsProvider,
) : GetProfileDetails {

    override fun invoke() =
        combine(
            profileStore.stream(StoreRequest.cached(Unit, refresh = false)),
            userInfoStorage.loggedUser,
            viewStateStorage.state,
        ) { storeResponse, loggedUser, viewState ->
            val profile = storeResponse.dataOrNull()
            val header = ProfileHeaderUi(
                isLoading = viewState.isLoading || storeResponse is StoreResponse.Loading,
                description = profile?.description,
                userInfo = profile?.userInfo?.toUi(),
                backgroundUrl = profile?.let { it.background ?: DEFAULT_PROFILE_BACKGROUND },
                banReason = if (profile?.banReason != null || profile?.banDate != null) {
                    BanReasonUi(
                        reason = profile.banReason,
                        endDate = profile.banDate,
                    )
                } else {
                    null
                },
                followersCount = profile?.let { it.followers ?: 0 },
                joinedAgo = profile?.signupAt?.toJoinedAgo(),
            )

            @OptIn(ExperimentalStdlibApi::class)
            val contextMenuOptions = if (loggedUser == null || loggedUser.id == profileId) {
                listOf(badgesOption())
            } else {
                buildList {
                    add(privateMessageOption())
                    if (profile != null) {
                        if (profile.isBlocked > 0) {
                            add(unblockOption())
                        } else {
                            add(blockOption())
                        }
                        if (profile.isObserved > 0) {
                            add(unobserveOption())
                        } else {
                            add(observeOption())
                        }
                        add(reportOption())
                    }
                }
            }

            ProfileDetailsUi(
                errorDialog = viewState.failedAction?.let { failure ->
                    ErrorDialogUi(
                        error = failure.cause,
                        retryAction = failure.retryAction,
                        dismissAction = { launchWithErrorHandling { viewStateStorage.update { it.copy(failedAction = null) } } },
                    )
                },
                header = header,
                onAddEntryClicked = { launchWithErrorHandling { interopRequests.request(InteropRequest.NewEntry(profileId)) } },
                contextMenuOptions = contextMenuOptions,
            )
        }
            .flowOn(AppDispatchers.Default)

    private fun badgesOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Badges,
        onClick = { launchWithErrorHandling { TODO() } },
    )

    private fun privateMessageOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.PrivateMessage,
        onClick = { launchWithErrorHandling { interopRequests.request(InteropRequest.PrivateMessage(profileId)) } },
    )

    private fun blockOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Block,
        onClick = { launchWithErrorHandling { profilesRepository.blockUser(profileId) } },
    )

    private fun launchWithErrorHandling(function: suspend CoroutineScope.() -> Unit) {
        appScopes.launchInKeyed<ProfileScope>(profileId) {
            runCatching { function() }
                .onFailure { failure -> viewStateStorage.update { it.copy(failedAction = FailedAction(failure)) } }
        }
    }

    private fun unblockOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Unblock,
        onClick = { launchWithErrorHandling { profilesRepository.unblockUser(profileId) } },
    )

    private fun observeOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.ObserveProfile,
        onClick = { launchWithErrorHandling { profilesRepository.observeUser(profileId) } },
    )

    private fun unobserveOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.UnobserveProfile,
        onClick = { launchWithErrorHandling { profilesRepository.unobserveUser(profileId) } },
    )

    private fun reportOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Report,
        onClick = { launchWithErrorHandling { TODO() } },
    )

    private fun Instant.toJoinedAgo() =
        periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString()
}
