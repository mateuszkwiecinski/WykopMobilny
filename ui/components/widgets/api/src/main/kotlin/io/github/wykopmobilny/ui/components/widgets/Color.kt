package io.github.wykopmobilny.ui.components.widgets

sealed interface Color

enum class ColorConst(val hexValue: String) : Color {

    CommentCurrentUser("#3498db"),
    CommentOriginalPoster("#F75616"),
    CommentLinked("#27AE60"),
    Male("#46ABF2"),
    Female("#f246d0"),
    Transparent("#00000000"),
    UserGreen("#339933"),
    UserOrange("#ff5917"),
    UserClaret("#BB0000"),
    UserBanned("#999999"),
    UserDeleted("#999999"),
    UserClient("#3F6FA0"),
    UserUnknown("#0000FF"),
    LinkBadgeHot("#f71e16"),
    CounterDownvoted("#c0392b"),
    CounterUpvoted("#27ae60"),
}

enum class ColorReference : Color {
    Admin,
}
