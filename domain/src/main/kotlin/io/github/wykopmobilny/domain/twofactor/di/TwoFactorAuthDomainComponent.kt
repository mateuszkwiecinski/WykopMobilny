package io.github.wykopmobilny.domain.twofactor.di

import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import io.github.wykopmobilny.domain.twofactor.GetTwoFactorAuthDetailsQuery
import io.github.wykopmobilny.ui.twofactor.GetTwoFactorAuthDetails
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDependencies

@TwoFactorAuthScope
@Subcomponent(modules = [TwoFactorAuthModule::class])
interface TwoFactorAuthDomainComponent : TwoFactorAuthDependencies

@Module
internal abstract class TwoFactorAuthModule {

    @Binds
    abstract fun GetTwoFactorAuthDetailsQuery.bind(): GetTwoFactorAuthDetails
}
