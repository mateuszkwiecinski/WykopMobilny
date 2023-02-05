package io.github.wykopmobilny.domain.di

import dagger.BindsInstance
import dagger.Component
import io.github.wykopmobilny.api.WykopApi
import io.github.wykopmobilny.blacklist.api.Scraper
import io.github.wykopmobilny.data.cache.api.ApplicationCache
import io.github.wykopmobilny.domain.blacklist.di.BlacklistDomainComponent
import io.github.wykopmobilny.domain.linkdetails.di.LinkDetailsComponent
import io.github.wykopmobilny.domain.login.ConnectConfig
import io.github.wykopmobilny.domain.login.di.LoginDomainComponent
import io.github.wykopmobilny.domain.navigation.Framework
import io.github.wykopmobilny.domain.navigation.InteropModule
import io.github.wykopmobilny.domain.navigation.InteropRequestService
import io.github.wykopmobilny.domain.notifications.di.NotificationsDomainComponent
import io.github.wykopmobilny.domain.notifications.di.NotificationsGlobalModule
import io.github.wykopmobilny.domain.profile.di.ProfileDomainComponent
import io.github.wykopmobilny.domain.promoted.PromotedModule
import io.github.wykopmobilny.domain.search.di.SearchDomainComponent
import io.github.wykopmobilny.domain.settings.di.SettingsDomainComponent
import io.github.wykopmobilny.domain.startup.AppConfig
import io.github.wykopmobilny.domain.startup.InitializeApp
import io.github.wykopmobilny.domain.styles.di.StylesDomainComponent
import io.github.wykopmobilny.domain.twofactor.di.TwoFactorAuthDomainComponent
import io.github.wykopmobilny.domain.work.di.WorkDomainComponent
import io.github.wykopmobilny.notification.NotificationsApi
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.storage.api.Storages
import io.github.wykopmobilny.kotlin.AppScopes
import io.github.wykopmobilny.work.WorkApi
import kotlinx.datetime.Clock
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        InteropModule::class,
        StoresModule::class,
        PromotedModule::class,
        NotificationsGlobalModule::class,
    ],
    dependencies = [
        Storages::class,
        Scraper::class,
        WykopApi::class,
        Framework::class,
        ApplicationCache::class,
        WorkApi::class,
        NotificationsApi::class,
    ],
)
interface DomainComponent {

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance appScopes: AppScopes,
            @BindsInstance connectConfig: () -> ConnectConfig,
            @BindsInstance clock: Clock,
            @BindsInstance appConfig: AppConfig,
            storages: Storages,
            scraper: Scraper,
            wykop: WykopApi,
            framework: Framework,
            applicationCache: ApplicationCache,
            work: WorkApi,
            notifications: NotificationsApi,
        ): DomainComponent
    }

    fun login(): LoginDomainComponent

    fun styles(): StylesDomainComponent

    fun settings(): SettingsDomainComponent

    fun blacklist(): BlacklistDomainComponent

    fun linkDetails(): LinkDetailsComponent.Factory

    fun profile(): ProfileDomainComponent.Factory

    fun search(): SearchDomainComponent

    fun navigation(): InteropRequestService

    fun settingsApiInterop(): SettingsPreferencesApi

    fun work(): WorkDomainComponent

    fun notifications(): NotificationsDomainComponent

    fun twoFactor(): TwoFactorAuthDomainComponent

    fun initializeApp(): InitializeApp
}
