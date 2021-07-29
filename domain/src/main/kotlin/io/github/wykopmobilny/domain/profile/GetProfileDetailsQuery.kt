package io.github.wykopmobilny.domain.profile

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import io.github.wykopmobilny.data.cache.api.ProfileEntity
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
    private val profileStore: Store<String, ProfileEntity>,
    private val getAppTheme: GetAppTheme,
    private val userInfoStorage: UserInfoStorage,
    private val usersRepository: UsersRepository,
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
                    banReason = if (profile.banReason != null || profile.banDate != null) {
                        BanReasonUi(
                            reason = profile.banReason,
                            endDate = profile.banDate,
                        )
                    } else {
                        null
                    },
                    nick = NickUi(
                        name = profile.id,
                        color = profile.color.getNickColor(getAppTheme),
                    ),
                    followersCount = profile.followers?.toInt() ?: 0,
                    joinedAgo = profile.signupAt,
                )
            } else {
                ProfileHeaderUi.Loading
            }

            @OptIn(ExperimentalStdlibApi::class)
            val contextMenuOptions = if (loggedUser == null || loggedUser.id == profileId) {
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
        onClick = { appScopes.launchInKeyed<ProfileScope>(profileId) { usersRepository.blockUser(profileId) } },
    )

    private fun unblockOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Unblock,
        onClick = { appScopes.launchInKeyed<ProfileScope>(profileId) { usersRepository.unblockUser(profileId) } },
    )

    private fun observeOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.ObserveProfile,
        onClick = { appScopes.launchInKeyed<ProfileScope>(profileId) { usersRepository.observeUser(profileId) } },
    )

    private fun unobserveOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.UnobserveProfile,
        onClick = { appScopes.launchInKeyed<ProfileScope>(profileId) { usersRepository.unobserveUser(profileId) } },
    )

    private fun reportOption() = ContextMenuOptionUi(
        option = ProfileMenuOption.Report,
        onClick = { },
    )
}
