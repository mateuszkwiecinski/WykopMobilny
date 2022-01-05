package io.github.wykopmobilny.domain.errorhandling

sealed class KnownError : Throwable() {

    data class TwoFactorAuthorizationRequired(override val message: String) : KnownError()
}
