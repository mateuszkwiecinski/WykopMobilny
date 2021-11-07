package io.github.wykopmobilny.domain.strings

object Strings {

    const val APP_NAME = "Wykop"
    const val SHARE_TITLE = "Udostępnij"

    object Notifications {

        const val TITLE = APP_NAME
        fun notificationContentUnbounded(count: Int) = "Posiadasz $count+ nowych powiadomień."
        fun notificationContent(count: Int) =
            when (count) {
                1 -> "Posiadasz $count nowe powiadominie."
                in 2..4 -> "Posiadasz $count nowe powiadomienia."
                else -> "Posiadasz $count nowych powiadomień."
            }
    }

    object Link {

        const val BURY_REASON_TITLE = "Zakop:"
        const val BURY_REASON_DUPLICATE = "Duplikat"
        const val BURY_REASON_SPAM = "Spam"
        const val BURY_REASON_FAKE_INFO = "Informacja nieprawdziwa"
        const val BURY_REASON_WRONG_CONTENT = "Treśc nieodpowiednia"
        const val BURY_REASON_UNSUITABLE_CONTENT = "Nie nadaje się"

        const val MORE_TITLE = "Opcje znaleziska:"
        const val MORE_OPTION_SHARE = "Udostępnij"
        const val MORE_OPTION_OPEN_IN_BROWSER = "Otwórz w przeglądarce"

        const val COMMENTS_SORT_BEST = "Najlepsze"
        const val COMMENTS_SORT_NEW = "Najnowsze"
        const val COMMENTS_SORT_OLD = "Najstarsze"
        const val COMMENTS_SORT_TITLE = "Sortuj komentarze"

        fun upvotesPercentage(value: Int) = "$value% wykopało"
        fun moreOptionUpvotersList(count: Int) = "Lista wykopujących ($count)"
        fun moreOptioDownvotersList(count: Int) = "Lista zakopujących ($count)"
        fun commentsSortOption(key: String) = "$key komentarze"
    }
}
