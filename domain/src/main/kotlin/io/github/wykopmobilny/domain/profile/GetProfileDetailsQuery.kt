package io.github.wykopmobilny.domain.profile

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import io.github.wykopmobilny.data.cache.api.ProfileDetailsView
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.InteropRequestsProvider
import io.github.wykopmobilny.domain.profile.di.ProfileScope
import io.github.wykopmobilny.domain.repositories.ProfilesRepository
import io.github.wykopmobilny.domain.strings.Strings
import io.github.wykopmobilny.domain.utils.safeKeyed
import io.github.wykopmobilny.storage.api.UserInfoStorage
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.FailedAction
import io.github.wykopmobilny.ui.base.SimpleViewStateStorage
import io.github.wykopmobilny.ui.base.components.ContextMenuOptionUi
import io.github.wykopmobilny.ui.base.components.Drawable
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.profile.BanReasonUi
import io.github.wykopmobilny.ui.profile.GetProfileDetails
import io.github.wykopmobilny.ui.profile.ProfileDetailsUi
import io.github.wykopmobilny.ui.profile.ProfileHeaderUi
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
                userInfo = profile?.userInfo?.toUi(onClicked = null),
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
                        dismissAction = safeCallback { viewStateStorage.update { it.copy(failedAction = null) } },
                    )
                },
                header = header,
                onAddEntryClicked = safeCallback { interopRequests.request(InteropRequest.NewEntry(profileId)) },
                contextMenuOptions = contextMenuOptions,
            )
        }
            .flowOn(AppDispatchers.Default)

    private fun badgesOption() = ContextMenuOptionUi(
        label = Strings.Profile.BADGES,
        onClick = safeCallback { TODO() },
    )

    private fun privateMessageOption() = ContextMenuOptionUi(
        label = Strings.Profile.PRIVATE_MESSAGE,
        icon = Drawable.PrivateMessage,
        onClick = safeCallback { interopRequests.request(InteropRequest.PrivateMessage(profileId)) },
    )

    private fun blockOption() = ContextMenuOptionUi(
        label = Strings.Profile.BLOCK_USER,
        onClick = safeCallback { profilesRepository.blockUser(profileId) },
    )

    private fun unblockOption() = ContextMenuOptionUi(
        label = Strings.Profile.UNBLOCK_USER,
        onClick = safeCallback { profilesRepository.unblockUser(profileId) },
    )

    private fun observeOption() = ContextMenuOptionUi(
        label = Strings.Profile.OBSERVE_USER,
        onClick = safeCallback { profilesRepository.observeUser(profileId) },
    )

    private fun unobserveOption() = ContextMenuOptionUi(
        label = Strings.Profile.UNOBSERVE_USER,
        onClick = safeCallback { profilesRepository.unobserveUser(profileId) },
    )

    private fun reportOption() = ContextMenuOptionUi(
        label = Strings.Profile.REPORT,
        onClick = safeCallback { TODO() },
    )

    private fun safeCallback(function: suspend CoroutineScope.() -> Unit): () -> Unit = {
        appScopes.safeKeyed<ProfileScope>(profileId) {
            runCatching { function() }
                .onFailure { failure -> viewStateStorage.update { it.copy(failedAction = FailedAction(failure)) } }
        }
    }

    private fun Instant.toJoinedAgo() =
        periodUntil(clock.now(), TimeZone.currentSystemDefault()).toPrettyString()
}
