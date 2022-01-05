package io.github.wykopmobilny

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Toast
import androidx.core.app.ShareCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.github.aakira.napier.Napier
import io.github.wykopmobilny.api.ApiSignInterceptor
import io.github.wykopmobilny.data.cache.sqldelight.DaggerApplicationCacheComponent
import io.github.wykopmobilny.di.DaggerAppComponent
import io.github.wykopmobilny.domain.blacklist.di.BlacklistScope
import io.github.wykopmobilny.domain.di.DomainComponent
import io.github.wykopmobilny.domain.di.HasScopeInitializer
import io.github.wykopmobilny.domain.linkdetails.di.LinkDetailsScope
import io.github.wykopmobilny.domain.login.ConnectConfig
import io.github.wykopmobilny.domain.login.di.LoginScope
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.android.DaggerFrameworkComponent
import io.github.wykopmobilny.domain.notifications.di.NotificationsScope
import io.github.wykopmobilny.domain.profile.di.ProfileScope
import io.github.wykopmobilny.domain.search.di.SearchScope
import io.github.wykopmobilny.domain.settings.di.SettingsScope
import io.github.wykopmobilny.domain.startup.AppConfig
import io.github.wykopmobilny.domain.styles.di.StylesScope
import io.github.wykopmobilny.domain.twofactor.di.TwoFactorAuthScope
import io.github.wykopmobilny.domain.work.di.WorkScope
import io.github.wykopmobilny.initializers.RemoteConfigKeys
import io.github.wykopmobilny.links.details.LinkDetailsDependencies
import io.github.wykopmobilny.links.details.LinkDetailsKey
import io.github.wykopmobilny.notification.AppNotification.Type.Notifications
import io.github.wykopmobilny.notification.NotificationDependencies
import io.github.wykopmobilny.notification.di.DaggerNotificationsComponent
import io.github.wykopmobilny.storage.android.DaggerStoragesComponent
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.styles.StylesDependencies
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.blacklist.BlacklistDependencies
import io.github.wykopmobilny.ui.login.LoginDependencies
import io.github.wykopmobilny.ui.modules.NewNavigator
import io.github.wykopmobilny.ui.modules.blacklist.BlacklistActivity
import io.github.wykopmobilny.ui.modules.embedview.EmbedViewActivity
import io.github.wykopmobilny.ui.modules.embedview.YoutubeActivity
import io.github.wykopmobilny.ui.modules.input.entry.add.AddEntryActivity
import io.github.wykopmobilny.ui.modules.links.downvoters.DownvotersActivity
import io.github.wykopmobilny.ui.modules.links.upvoters.UpvotersActivity
import io.github.wykopmobilny.ui.modules.mainnavigation.MainNavigationActivity
import io.github.wykopmobilny.ui.modules.notificationslist.NotificationsListActivity
import io.github.wykopmobilny.ui.modules.photoview.PhotoViewActivity
import io.github.wykopmobilny.ui.modules.pm.conversation.ConversationActivity
import io.github.wykopmobilny.ui.modules.profile.ProfileActivity
import io.github.wykopmobilny.ui.modules.tag.TagActivity
import io.github.wykopmobilny.ui.profile.ProfileDependencies
import io.github.wykopmobilny.ui.search.SearchDependencies
import io.github.wykopmobilny.ui.settings.SettingsDependencies
import io.github.wykopmobilny.ui.twofactor.TwoFactorAuthDependencies
import io.github.wykopmobilny.utils.ApplicationInjector
import io.github.wykopmobilny.utils.linkhandler.WykopLinkHandler
import io.github.wykopmobilny.utils.requireDependency
import io.github.wykopmobilny.utils.usermanager.SimpleUserManagerApi
import io.github.wykopmobilny.utils.usermanager.UserCredentials
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import io.github.wykopmobilny.work.WorkDependencies
import io.github.wykopmobilny.work.di.DaggerWorkManagerComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

open class WykopApp : DaggerApplication(), ApplicationInjector, AppScopes {

    companion object {

        const val WYKOP_API_URL = "https://a2.wykop.pl"
    }

    @Inject
    lateinit var userManagerApi: Lazy<UserManagerApi>

    @Inject
    lateinit var settingsPreferencesApi: Lazy<SettingsPreferencesApi>

    override val applicationScope = CoroutineScope(Job() + Dispatchers.Default)

    private val okHttpClient = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        // https://github.com/square/okhttp/issues/3146
        .connectionPool(
            ConnectionPool(
                maxIdleConnections = 0,
                keepAliveDuration = 1,
                timeUnit = TimeUnit.NANOSECONDS,
            ),
        )
        .build()

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        doInterop()

        applicationScope.launch { domainComponent.initializeApp().invoke() }
        applicationScope.launch {
            storages.userInfoStorage().loggedUser
                .mapNotNull { it?.id }
                .collect { FirebaseCrashlytics.getInstance().setUserId(it) }
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
        DaggerAppComponent.factory().create(
            instance = this,
            okHttpClient = okHttpClient,
            wykop = wykopApi,
            patrons = patrons,
            scraper = scraper,
            storages = storages,
            settingsInterop = domainComponent.settingsApiInterop(),
        )

    protected open val domainComponent: DomainComponent by lazy {
        daggerDomain().create(
            appScopes = this,
            connectConfig = {
                val appKey = FirebaseRemoteConfig.getInstance().getString(RemoteConfigKeys.API_APP_KEY)
                ConnectConfig(connectUrl = "https://a2.wykop.pl/login/connect/appkey/$appKey")
            },
            clock = Clock.System,
            storages = storages,
            scraper = scraper,
            wykop = wykopApi,
            framework = framework,
            applicationCache = applicationCache,
            work = work,
            appConfig = appConfig,
            notifications = notifications,
        )
    }

    protected val appConfig = object : AppConfig {

        private val firebase
            get() = FirebaseRemoteConfig.getInstance()
        override val blacklistRefreshInterval: Duration
            get() = firebase.getLong(RemoteConfigKeys.BLACKLIST_REFRESH_INTERVAL).milliseconds
        override val blacklistFlexInterval: Duration
            get() = firebase.getLong(RemoteConfigKeys.BLACKLIST_FLEX_INTERVAL).milliseconds
        override val notificationsEnabled: Boolean
            get() = firebase.getBoolean(RemoteConfigKeys.NOTIFICATIONS_ENABLED)
    }

    protected open val notifications by lazy {
        DaggerNotificationsComponent.factory().create(
            context = this,
            interopIntentHandler = { type ->
                when (type) {
                    is Notifications.SingleMessage -> WykopLinkHandler.getLinkIntent(type.interopUrl, this)
                        ?: MainNavigationActivity.getIntent(this)
                            .also { Napier.e("Invalid deeplink for url=${type.interopUrl}") }
                    Notifications.MultipleNotifications -> Intent(applicationContext, NotificationsListActivity::class.java)
                }
            },
            dependencies = object : NotificationDependencies {
                val lazyDependencies by lazy { requireDependency<NotificationDependencies>() }

                override fun handleNotificationDismissed() =
                    lazyDependencies.handleNotificationDismissed()
            },
        )
    }

    protected open val work by lazy {
        DaggerWorkManagerComponent.factory().create(
            context = this,
        )
    }

    protected open val storages by lazy {
        DaggerStoragesComponent.factory().create(
            context = this,
            dbName = "wykop_storage.sqlite",
            executor = AppDispatchers.IO.asExecutor(),
        )
    }

    protected open val scraper by lazy {
        daggerScraper().create(
            okHttpClient = okHttpClient,
            baseUrl = "https://wykop.pl",
            cookieProvider = { webPage -> CookieManager.getInstance().getCookie(webPage) },
        )
    }

    protected open val patrons by lazy {
        daggerPatrons().create(
            okHttpClient = okHttpClient.newBuilder()
                .cache(Cache(cacheDir.resolve("okhttp/patrons"), maxSize = 5 * 1024 * 1024L))
                .build(),
            baseUrl = "https://raw.githubusercontent.com/",
        )
    }

    protected open val wykopApi by lazy {
        daggerWykop().create(
            okHttpClient = okHttpClient,
            baseUrl = WYKOP_API_URL,
            appKey = { FirebaseRemoteConfig.getInstance().getString(RemoteConfigKeys.API_APP_KEY) },
            cacheDir = cacheDir.resolve("okhttp/wykop"),
            signingInterceptor = ApiSignInterceptor(
                object : SimpleUserManagerApi {

                    override fun getUserCredentials(): UserCredentials? =
                        userManagerApi.get().getUserCredentials()
                },
            ),
        )
    }

    protected open val framework by lazy {
        DaggerFrameworkComponent.factory().create(
            application = this,
            scope = applicationScope,
        )
    }

    protected open val applicationCache by lazy {
        DaggerApplicationCacheComponent.factory().create(
            context = this,
        )
    }

    private val scopes = mutableMapOf<String, SubScope<Any>>()

    data class SubScope<T>(
        val dependencyContainer: T,
        val coroutineScope: CoroutineScope,
    )

    // TODO @mk : 25/07/2021 I don't know where I'm going here yet. Will figure something out 👀 
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getDependency(clazz: KClass<T>, scopeId: Any?): T =
        when (clazz) {
            LoginDependencies::class -> getOrPutScope<LoginScope>(scopeId) { domainComponent.login() }
            StylesDependencies::class -> getOrPutScope<StylesScope>(scopeId) { domainComponent.styles() }
            SettingsDependencies::class -> getOrPutScope<SettingsScope>(scopeId) { domainComponent.settings() }
            BlacklistDependencies::class -> getOrPutScope<BlacklistScope>(scopeId) { domainComponent.blacklist() }
            WorkDependencies::class -> getOrPutScope<WorkScope>(scopeId) { domainComponent.work() }
            SearchDependencies::class -> getOrPutScope<SearchScope>(scopeId) { domainComponent.search() }
            NotificationDependencies::class -> getOrPutScope<NotificationsScope>(scopeId) { domainComponent.notifications() }
            TwoFactorAuthDependencies::class -> getOrPutScope<TwoFactorAuthScope>(scopeId) { domainComponent.twoFactor() }
            LinkDetailsDependencies::class -> {
                scopeId as LinkDetailsKey
                getOrPutScope<LinkDetailsScope>(scopeId) { domainComponent.linkDetails().create(key = scopeId) }
            }
            ProfileDependencies::class -> {
                scopeId as String
                getOrPutScope<ProfileScope>(scopeId) { domainComponent.profile().create(profileId = scopeId) }
            }
            else -> error("Unknown dependency type $clazz")
        }.dependencyContainer as T

    private inline fun <reified T : Any> scopeKey(id: Any?) = scopeKey(T::class, id)

    private fun <T : Any> scopeKey(clazz: KClass<T>, id: Any?) = "${clazz.qualifiedName}=$id"

    private inline fun <reified T : Any> getOrPutScope(id: Any?, container: () -> Any) =
        scopes.getOrPut(scopeKey<T>(id)) { initScope(container()) }

    private fun <T> initScope(container: T) =
        SubScope(
            dependencyContainer = container,
            coroutineScope = CoroutineScope(Job(applicationScope.coroutineContext[Job]) + Dispatchers.Default),
        ).apply {
            if (container is HasScopeInitializer) {
                coroutineScope.launch { container.initializer().initialize() }
            }
        }

    override fun <T : Any> destroyDependency(clazz: KClass<T>, scopeId: Any?) {
        Napier.i("Destroying dependency name=${clazz.java.name}")
        when (clazz) {
            LoginDependencies::class -> scopes.remove(scopeKey<LoginScope>(scopeId))
            StylesDependencies::class -> scopes.remove(scopeKey<StylesScope>(scopeId))
            SettingsDependencies::class -> scopes.remove(scopeKey<SettingsScope>(scopeId))
            BlacklistDependencies::class -> scopes.remove(scopeKey<BlacklistScope>(scopeId))
            WorkDependencies::class -> scopes.remove(scopeKey<WorkScope>(scopeId))
            SearchDependencies::class -> scopes.remove(scopeKey<SearchScope>(scopeId))
            LinkDetailsDependencies::class -> scopes.remove(scopeKey<LinkDetailsScope>(scopeId))
            ProfileDependencies::class -> scopes.remove(scopeKey<ProfileScope>(scopeId))
            NotificationDependencies::class -> scopes.remove(scopeKey<NotificationsScope>(scopeId))
            TwoFactorAuthDependencies::class -> scopes.remove(scopeKey<TwoFactorAuthScope>(scopeId))
            else -> error("Unknown dependency type $clazz")
        }?.coroutineScope?.cancel()
    }

    override fun <T : Any> launchScoped(
        clazz: KClass<T>,
        id: Any?,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        val key = scopeKey(clazz, id)
        scopes[key]?.coroutineScope?.launch(block = block) ?: return Napier.w("launchScoped didn't find scope for key=$key")
    }

    private fun doInterop() {
        applicationScope.launch {
            var currentActivity: Activity? = null
            registerActivityLifecycleCallbacks(
                object : ActivityLifecycleCallbacks {
                    override fun onActivityCreated(
                        activity: Activity,
                        savedInstanceState: Bundle?,
                    ) = Unit

                    override fun onActivityStarted(activity: Activity) = Unit

                    override fun onActivityResumed(activity: Activity) {
                        currentActivity = activity
                    }

                    override fun onActivityPaused(activity: Activity) {
                        currentActivity = null
                    }

                    override fun onActivityStopped(activity: Activity) = Unit

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) =
                        Unit

                    override fun onActivityDestroyed(activity: Activity) = Unit
                },
            )
            domainComponent.navigation().request.collect {
                val context = currentActivity ?: return@collect
                when (it) {
                    InteropRequest.BlackListScreen -> context.startActivity(
                        BlacklistActivity.createIntent(
                            context,
                        ),
                    )
                    is InteropRequest.ShowToast -> {
                        withContext(AppDispatchers.Main) {
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }
                    }
                    is InteropRequest.PrivateMessage ->
                        context.startActivity(
                            ConversationActivity.createIntent(
                                context,
                                it.profileId,
                            ),
                        )
                    is InteropRequest.NewEntry ->
                        context.startActivity(
                            AddEntryActivity.createIntent(
                                context,
                                null,
                                "@${it.profileId}: ",
                            ),
                        )
                    is InteropRequest.Profile ->
                        context.startActivity(ProfileActivity.createIntent(context, it.profileId))
                    is InteropRequest.Tag ->
                        context.startActivity(TagActivity.createIntent(context, it.tagId))
                    is InteropRequest.WebBrowser -> {
                        val navigator = NewNavigator(context, settingsPreferencesApi.get())
                        if (it.force) {
                            navigator.openBrowser(it.url)
                        } else {
                            WykopLinkHandler(context, navigator).handleUrl(it.url)
                        }
                    }
                    is InteropRequest.Share -> {
                        val type = when (it.type) {
                            InteropRequest.Share.Type.TextPlain -> "text/plain"
                        }
                        ShareCompat.IntentBuilder(context)
                            .setType(type)
                            .setChooserTitle(it.title)
                            .setText(it.url)
                            .startChooser()
                    }
                    is InteropRequest.DownvotersList -> context.startActivity(DownvotersActivity.createIntent(it.linkId, context))
                    is InteropRequest.UpvotersList -> context.startActivity(UpvotersActivity.createIntent(it.linkId, context))
                    is InteropRequest.OpenPlayer -> context.startActivity(EmbedViewActivity.createIntent(context, it.url))
                    is InteropRequest.OpenYoutube -> context.startActivity(YoutubeActivity.createIntent(context, it.url))
                    is InteropRequest.ShowGif -> context.startActivity(PhotoViewActivity.createIntent(context, it.url))
                    is InteropRequest.ShowImage -> context.startActivity(PhotoViewActivity.createIntent(context, it.url))
                }
                    .run { }
            }
        }
    }
}
