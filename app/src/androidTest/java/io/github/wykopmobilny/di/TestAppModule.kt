package io.github.wykopmobilny.di

import android.content.Context
import dagger.Binds
import dagger.Module
import io.github.wykopmobilny.TestApp
import io.github.wykopmobilny.ui.base.AppScopes

@Module
internal abstract class TestAppModule {

    @Binds
    abstract fun TestApp.provideContext(): Context

    @Binds
    abstract fun TestApp.appScopes(): AppScopes
}
