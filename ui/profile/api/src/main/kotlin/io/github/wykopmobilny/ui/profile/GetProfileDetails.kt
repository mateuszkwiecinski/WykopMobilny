package io.github.wykopmobilny.ui.profile

import io.github.wykopmobilny.ui.base.Query
import io.github.wykopmobilny.ui.base.components.ErrorDialogUi

interface GetProfileDetails : Query<ProfileDetailsUi>

class ProfileDetailsUi(
    val header: ProfileHeaderUi,
    val onAddEntryClicked: () -> Unit,
    val contextMenuOptions: List<ContextMenuOptionUi<ProfileMenuOption>>,
    val errorDialog: ErrorDialogUi?,
)

sealed class ProfileHeaderUi {

    object Loading : ProfileHeaderUi()

    class WithData(
        val description: String?,
        val avatarUi: AvatarUi,
        val backgroundUrl: String,
        val banReason: BanReasonUi?,
        val nick: NickUi,
        val followersCount: Int,
        val joinedAgo: String,
    ) : ProfileHeaderUi()
}

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

data class AvatarUi(
    val avatarUrl: String,
    val rank: RankUi?,
    val genderStrip: ColorHex?,
)

data class NickUi(
    val name: String,
    val color: ColorHex,
)

data class RankUi(
    val number: Int,
    val color: ColorHex,
)

data class BanReasonUi(
    val reason: String?,
    val endDate: String?,
)

@JvmInline
value class ColorHex(val hexValue: String)
