package io.github.wykopmobilny.ui.components.users

data class UserInfoUi(
    val avatar: AvatarUi,
    val name: String,
    val color: Color?,
)

data class AvatarUi(
    val avatarUrl: String?,
    val rank: Int?,
    val genderStrip: Color?,
)

sealed interface Color

@JvmInline
value class ColorHex(val hexValue: String) : Color

enum class ColorReference : Color {
    Admin,
    CounterDefault,
    CounterUpvoted,
    CounterDownvoted,
}
