package io.github.wykopmobilny.domain.strings

import io.github.wykopmobilny.domain.navigation.AuthenticatorApp

object Strings {

    const val APP_NAME = "Wykop"
    const val SHARE_TITLE = "Udostępnij"
    const val COPIED_TO_CLIPBOARD = "Skopiowano do schowka"

    object Notifications {

        const val TITLE = APP_NAME
        fun notificationContentUnbounded(count: Int) = when {
            count == 1 -> "Posiadasz nowe powiadomienia."
            count in 2..4 -> "Posiadasz $count+ nowe powiadomienia."
            count in 12..14 -> "Posiadasz $count+ nowych powiadomień."
            count % 10 in 2..4 -> "Posiadasz $count+ nowe powiadomienia."
            else -> "Posiadasz $count+ nowych powiadomień."
        }

        fun notificationContent(count: Int) = when {
            count == 1 -> "Posiadasz 1 nowe powiadomienia."
            count in 2..4 -> "Posiadasz $count nowe powiadomienia."
            count in 12..14 -> "Posiadasz $count nowych powiadomień."
            count % 10 in 2..4 -> "Posiadasz $count nowe powiadomienia."
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

        const val MORE_TITLE_LINK = "Opcje znaleziska:"
        const val MORE_TITLE_COMMENT = "Opcje komentarza:"
        const val MORE_OPTION_SHARE = "Udostępnij"
        const val MORE_OPTION_COPY = "Skopiuj treść"
        const val MORE_OPTION_REPORT = "Zgłoś naruszenie"
        const val MORE_OPTION_OPEN_IN_BROWSER = "Otwórz w przeglądarce"

        const val COMMENTS_SORT_BEST = "Najlepsze"
        const val COMMENTS_SORT_NEW = "Najnowsze"
        const val COMMENTS_SORT_OLD = "Najstarsze"
        const val COMMENTS_SORT_TITLE = "Sortuj komentarze"

        fun upvotesPercentage(value: Int) = "$value% wykopało"
        fun moreOptionUpvotersList(count: Int) = "Lista wykopujących ($count)"
        fun moreOptionDownvotersList(count: Int) = "Lista zakopujących ($count)"
        fun commentsSortOption(key: String) = "$key komentarze"
    }

    object Comment {
        const val SPOILER_DIALOG_TITLE = "Spoiler"
    }

    object Profile {

        const val PRIVATE_MESSAGE = "Wiadomość prywatna"
        const val OBSERVE_USER = "Obserwuj"
        const val UNOBSERVE_USER = "Nie obserwuj"
        const val BLOCK_USER = "Zablokuj"
        const val UNBLOCK_USER = "Odblokuj"
        const val BADGES = "Osiągnięcia"
        const val REPORT = "Zgłoś"
    }

    object TwoFactorAuth {

        const val Cta = "Weryfikuj"

        fun openAuthenticator(externalApp: AuthenticatorApp) = when (externalApp) {
            AuthenticatorApp.Google -> "Google Authenticator"
            AuthenticatorApp.Microsoft -> "Microsoft Authenticator"
            AuthenticatorApp.Authy -> "Authy"
            AuthenticatorApp.AuthenticatorPro -> "Authenticator Pro"
            AuthenticatorApp.Bitwarden -> "Bitwarden"
            AuthenticatorApp.Pixplicity -> "Pixplicity Authenticator"
            AuthenticatorApp.Salesforce -> "Salesforce Authenticator"
            AuthenticatorApp.Lastpass -> "Lastpass Authenticator"
        }
    }
}
