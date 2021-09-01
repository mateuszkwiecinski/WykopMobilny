package io.github.wykopmobilny.di

import android.content.Context
import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.WykopApp
import io.github.wykopmobilny.ui.base.AppScopes

@Module
internal abstract class AppModule {

    @Binds
    abstract fun WykopApp.provideContext(): Context

    @Binds
    abstract fun WykopApp.appScopes(): AppScopes
}
