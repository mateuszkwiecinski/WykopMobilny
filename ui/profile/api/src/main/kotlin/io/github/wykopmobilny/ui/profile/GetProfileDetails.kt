package io.github.wykopmobilny.ui.profile

import io.github.wykopmobilny.ui.base.Query
import io.github.wykopmobilny.ui.base.components.ContextMenuOptionUi
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi
import io.github.wykopmobilny.ui.components.widgets.UserInfoUi

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
