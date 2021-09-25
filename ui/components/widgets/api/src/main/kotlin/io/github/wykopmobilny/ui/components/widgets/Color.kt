package io.github.wykopmobilny.ui.components.widgets

sealed interface Color

@JvmInline
value class ColorHex(val hexValue: String) : Color

enum class ColorReference : Color {
    Admin,
    CounterDefault,
    CounterUpvoted,
    CounterDownvoted,
}
