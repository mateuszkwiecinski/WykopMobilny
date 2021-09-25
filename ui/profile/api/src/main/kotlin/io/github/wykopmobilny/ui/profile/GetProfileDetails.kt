package io.github.wykopmobilny.ui.profile

import io.github.wykopmobilny.ui.base.Query
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.components.users.UserInfoUi

interface GetProfileDetails : Query<ProfileDetailsUi>

class ProfileDetailsUi(
    val header: ProfileHeaderUi,
    val onAddEntryClicked: () -> Unit,
    val contextMenuOptions: List<ContextMenuOptionUi<ProfileMenuOption>>,
    val errorDialog: ErrorDialogUi?,
)

data class ProfileHeaderUi(
    val isLoading: Boolean,
    val description: String?,
    val userInfo: UserInfoUi?,
    val backgroundUrl: String?,
    val banReason: BanReasonUi?,
    val followersCount: Int?,
    val joinedAgo: String?,
)

data class ContextMenuOptionUi<T : Enum<T>>(
    val option: T,
    val onClick: () -> Unit,
)

enum class ProfileMenuOption {
    PrivateMessage,
    Unblock,
    Block,
    ObserveProfile,
    UnobserveProfile,
    Badges,
    Report,
}

data class BanReasonUi(
    val reason: String?,
    val endDate: String?,
)
