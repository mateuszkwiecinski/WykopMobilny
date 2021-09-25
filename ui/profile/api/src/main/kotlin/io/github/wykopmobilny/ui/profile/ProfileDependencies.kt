package io.github.wykopmobilny.ui.profile

interface ProfileDependencies {

    fun profileDetails(): GetProfileDetails

    fun profileLinks(): GetProfileActions
}
