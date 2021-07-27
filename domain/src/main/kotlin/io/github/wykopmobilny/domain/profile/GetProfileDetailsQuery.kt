package io.github.wykopmobilny.domain.profile

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import dagger.Lazy
import io.github.wykopmobilny.api.responses.ProfileResponse
import io.github.wykopmobilny.domain.blacklist.actions.UsersRepository
import io.github.wykopmobilny.domain.profile.di.ProfileScope
import io.github.wykopmobilny.domain.styles.GetAppTheme
import io.github.wykopmobilny.storage.api.UserInfoStorage
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.base.launchInKeyed
import io.github.wykopmobilny.ui.profile.AvatarUi
import io.github.wykopmobilny.ui.profile.BanReasonUi
import io.github.wykopmobilny.ui.profile.ContextMenuOptionUi
import io.github.wykopmobilny.ui.profile.GetProfileDetails
import io.github.wykopmobilny.ui.profile.NickUi
import io.github.wykopmobilny.ui.profile.ProfileDetailsUi
import io.github.wykopmobilny.ui.profile.ProfileHeaderUi
import io.github.wykopmobilny.ui.profile.ProfileMenuOption
import io.github.wykopmobilny.ui.profile.RankUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

internal class GetProfileDetailsQuery @Inject constructor(
    @ProfileId private val profileId: String,
    private val profileStore: Store<String, ProfileResponse>,
    private val getAppTheme: GetAppTheme,
    private val userInfoStorage: UserInfoStorage,
    private val usersRepository: Lazy<UsersRepository>,
    private val appScopes: AppScopes,
) : GetProfileDetails {

    override fun invoke(): Flow<ProfileDetailsUi> =
        combine(
            profileStore.stream(StoreRequest.cached(profileId, refresh = true)),
            userInfoStorage.loggedUser,
        ) { profileResponse, loggedUser ->
            val profile = profileResponse.dataOrNull()
            val header = if (profile != null) {
                ProfileHeaderUi.WithData(
                    description = profile.description,
                    avatarUi = AvatarUi(
                        avatarUrl = profile.avatar,
                        rank = profile.rank?.takeIf { it > 0 }?.let { rank ->
                            RankUi(
                                number = rank,
                                color = profile.color.getNickColor(getAppTheme),
                            )
                        },
                        genderStrip = profile.sex?.let(::getGender),
                    ),
                    backgroundUrl = profile.background ?: DEFAULT_PROFILE_BACKGROUND,
                    banReason = profile.ban?.let { ban ->
                        BanReasonUi(
                            reason = ban.reason,
                            endDate = ban.date,
                        )
                    },
                    nick = NickUi(
                        name = profile.login,
                        color = profile.color.getNickColor(getAppTheme),
                    ),
                    followersCount = profile.followers ?: 0,
                    joinedAgo = profile.signupAt,
                )
            } else {
                ProfileHeaderUi.Loading
            }

            @OptIn(ExperimentalStdlibApi::class)
            val contextMenuOptions = if (loggedUser == null || loggedUser.userName == profileId) {
                listOf(badgesOption())
            } else {
                buildList {
                    add(privateMessageOption())
                    if (profile != null) {
                        if (profile.isBlocked == true) {
                            add(unblockOption())
                        } else {
                            add(blockOption())
                        }
                        if (profile.isObserved == true) {
                            add(unobserveOption())
                        } else {
                            add(observeOption())
                        }
                        add(reportOption())
                    }
                }
            }

            ProfileDetailsUi(
                errorDialog = null,
                header = header,
                onAddEntryClicked = {},
                contextMenuOptions = contextMenuOptions,
            )
        }

    private fun badgesOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Badges,
        onClick = {},
    )

    private fun privateMessageOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.PrivateMessage,
        onClick = {},
    )

    private fun blockOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Block,
        onClick = { appScopes.launchInKeyed<ProfileScope>(profileId) { usersRepository.get().blockUser(profileId) } },
    )

    private fun unblockOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Unblock,
        onClick = { appScopes.launchInKeyed<ProfileScope>(profileId) { usersRepository.get().unblockUser(profileId) } },
    )

    private fun observeOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.ObserveProfile,
        onClick = { appScopes.launchInKeyed<ProfileScope>(profileId) { usersRepository.get().observeUser(profileId) } },
    )

    private fun unobserveOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.UnobserveProfile,
        onClick = { appScopes.launchInKeyed<ProfileScope>(profileId) { usersRepository.get().unobserveUser(profileId) } },
    )

    private fun reportOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Report,
        onClick = { },
    )
}
