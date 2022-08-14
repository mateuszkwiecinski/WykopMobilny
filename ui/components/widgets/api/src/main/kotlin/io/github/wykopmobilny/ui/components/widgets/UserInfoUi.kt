package io.github.wykopmobilny.ui.components.widgets

data class UserInfoUi(
    val avatar: AvatarUi,
    val name: String,
    val color: Color?,
)

data class AvatarUi(
    val avatarUrl: String,
    val rank: Int?,
    val genderStrip: Color?,
    val onClicked: (() -> Unit)?,
)
