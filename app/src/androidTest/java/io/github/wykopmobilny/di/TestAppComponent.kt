package io.github.wykopmobilny.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import io.github.wykopmobilny.TestApp
import io.github.wykopmobilny.api.WykopApi
import io.github.wykopmobilny.blacklist.api.Scraper
import io.github.wykopmobilny.di.modules.NetworkModule
import io.github.wykopmobilny.di.modules.RepositoryModule
import io.github.wykopmobilny.patrons.remote.PatronsComponent
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.storage.api.Storages
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        TestAppModule::class,
        ActivityBuilder::class,
        NetworkModule::class,
        RepositoryModule::class,
    ],
    dependencies = [
        WykopApi::class,
        PatronsComponent::class,
        Scraper::class,
        Storages::class,
    ],
)
internal interface TestAppComponent : AppComponent {

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance instance: TestApp,
            @BindsInstance okHttpClient: OkHttpClient,
            wykop: WykopApi,
            patrons: PatronsComponent,
            scraper: Scraper,
            storages: Storages,
            @BindsInstance settingsInterop: SettingsPreferencesApi,
        ): TestAppComponent
    }
}
