package io.github.wykopmobilny

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.github.wykopmobilny.di.DaggerTestAppComponent
import io.github.wykopmobilny.domain.login.ConnectConfig
import io.github.wykopmobilny.fakes.FakeCookieProvider
import io.github.wykopmobilny.storage.android.DaggerStoragesComponent
import io.github.wykopmobilny.storage.android.StoragesComponent
import io.github.wykopmobilny.kotlin.AppDispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.datetime.Clock

internal class TestApp : WykopApp() {

    val cookieProvider = FakeCookieProvider()

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = DaggerTestAppComponent.factory()
        .create(
            instance = this,
            okHttpClient = okHttpClient,
            wykop = wykopApi,
            patrons = patrons,
            scraper = scraper,
            storages = storages,
            settingsInterop = domainComponent.settingsApiInterop(),
        )

    override val wykopApi by lazy {
        daggerWykop().create(
            okHttpClient = okHttpClient,
            baseUrl = "http://localhost:8000",
            appKey = { "fixture-app-key" },
            signingInterceptor = { it.proceed(it.request()) },
            cacheDir = cacheDir.resolve("tests/okhttp-cache"),
        )
    }
    override val patrons by lazy {
        daggerPatrons().create(
            okHttpClient = okHttpClient,
            baseUrl = "http://localhost:8000",
        )
    }

    override val scraper by lazy {
        daggerScraper().create(
            okHttpClient = okHttpClient,
            baseUrl = "http://localhost:8000",
            cookieProvider = cookieProvider::cookieForSite,
        )
    }

    public override val storages: StoragesComponent by lazy {
        DaggerStoragesComponent.factory().create(
            context = this,
            dbName = null,
            executor = AppDispatchers.IO.asExecutor(),
        )
    }

    override val domainComponent by lazy {
        daggerDomain().create(
            appScopes = this,
            connectConfig = { ConnectConfig("http://localhost:8000/Login/Connect") },
            clock = Clock.System,
            storages = storages,
            scraper = scraper,
            wykop = wykopApi,
            framework = framework,
            applicationCache = applicationCache,
            appConfig = appConfig,
            work = work,
            notifications = notifications,
        )
    }

    companion object {
        lateinit var instance: TestApp
    }
}
