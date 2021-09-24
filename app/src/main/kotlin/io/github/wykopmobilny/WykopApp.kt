package io.github.wykopmobilny

import android.app.Activity
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.github.wykopmobilny.api.ApiSignInterceptor
import io.github.wykopmobilny.data.cache.sqldelight.DaggerApplicationCacheComponent
import io.github.wykopmobilny.di.DaggerAppComponent
import io.github.wykopmobilny.domain.blacklist.di.BlacklistScope
import io.github.wykopmobilny.domain.di.DomainComponent
import io.github.wykopmobilny.domain.di.HasScopeInitializer
import io.github.wykopmobilny.domain.login.ConnectConfig
import io.github.wykopmobilny.domain.login.di.LoginScope
import io.github.wykopmobilny.domain.navigation.InteropRequest
import io.github.wykopmobilny.domain.navigation.android.DaggerFrameworkComponent
import io.github.wykopmobilny.domain.profile.di.ProfileScope
import io.github.wykopmobilny.domain.search.di.SearchScope
import io.github.wykopmobilny.domain.settings.di.SettingsScope
import io.github.wykopmobilny.domain.styles.di.StylesScope
import io.github.wykopmobilny.domain.work.di.WorkScope
import io.github.wykopmobilny.storage.android.DaggerStoragesComponent
import io.github.wykopmobilny.storage.api.SettingsPreferencesApi
import io.github.wykopmobilny.styles.StylesDependencies
import io.github.wykopmobilny.ui.base.AppDispatchers
import io.github.wykopmobilny.ui.base.AppScopes
import io.github.wykopmobilny.ui.blacklist.BlacklistDependencies
import io.github.wykopmobilny.ui.login.LoginDependencies
import io.github.wykopmobilny.ui.modules.blacklist.BlacklistActivity
import io.github.wykopmobilny.ui.modules.input.entry.add.AddEntryActivity
import io.github.wykopmobilny.ui.modules.pm.conversation.ConversationActivity
import io.github.wykopmobilny.ui.profile.ProfileDependencies
import io.github.wykopmobilny.ui.search.SearchDependencies
import io.github.wykopmobilny.ui.settings.SettingsDependencies
import io.github.wykopmobilny.utils.ApplicationInjector
import io.github.wykopmobilny.utils.usermanager.SimpleUserManagerApi
import io.github.wykopmobilny.utils.usermanager.UserCredentials
import io.github.wykopmobilny.utils.usermanager.UserManagerApi
import io.github.wykopmobilny.work.WorkDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import okhttp3.Cache
import okhttp3.OkHttpClient
import javax.inject.Inject
import kotlin.reflect.KClass

open class WykopApp : DaggerApplication(), ApplicationInjector, AppScopes {

    companion object {

        const val WYKOP_API_URL = "https://a2.wykop.pl"
    }

    @Inject
    lateinit var userManagerApi: Lazy<UserManagerApi>

    @Inject
    lateinit var settingsPreferencesApi: Lazy<SettingsPreferencesApi>

    override val applicationScope = CoroutineScope(Job() + Dispatchers.Default)

    private val okHttpClient = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        doInterop()
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
                val appKey = FirebaseRemoteConfig.getInstance().getString("wykop_app_key")
                ConnectConfig(connectUrl = "https://a2.wykop.pl/login/connect/appkey/$appKey")
            },
            clock = Clock.System,
            storages = storages,
            scraper = scraper,
            wykop = wykopApi,
            framework = framework,
            applicationCache = applicationCache,
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
            appKey = { FirebaseRemoteConfig.getInstance().getString("wykop_app_key") },
            cacheDir = cacheDir.resolve("okhttp/wykop"),
            signingInterceptor = ApiSignInterceptor(
                object : SimpleUserManagerApi {

                    override fun getUserCredentials(): UserCredentials? = userManagerApi.get().getUserCredentials()
                },
            ),
        )
    }

    protected open val framework by lazy {
        DaggerFrameworkComponent.factory().create(
            application = this,
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
    override fun <T : Any> getDependency(clazz: KClass<T>, scopeId: String?): T =
        when (clazz) {
            LoginDependencies::class -> getOrPutScope<LoginScope>(scopeId) { domainComponent.login() }
            StylesDependencies::class -> getOrPutScope<StylesScope>(scopeId) { domainComponent.styles() }
            SettingsDependencies::class -> getOrPutScope<SettingsScope>(scopeId) { domainComponent.settings() }
            BlacklistDependencies::class -> getOrPutScope<BlacklistScope>(scopeId) { domainComponent.blacklist() }
            WorkDependencies::class -> getOrPutScope<WorkScope>(scopeId) { domainComponent.work() }
            SearchDependencies::class -> getOrPutScope<SearchScope>(scopeId) { domainComponent.search() }
            ProfileDependencies::class -> {
                getOrPutScope<ProfileScope>(scopeId) { domainComponent.profile().create(profileId = checkNotNull(scopeId)) }
            }
            else -> error("Unknown dependency type $clazz")
        }.dependencyContainer as T

    private inline fun <reified T : Any> scopeKey(id: String?) = "${T::class.simpleName}=$id"

    private inline fun <reified T : Any> getOrPutScope(id: String?, container: () -> Any) =
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

    override fun <T : Any> destroyDependency(clazz: KClass<T>, scopeId: String?) {
        when (clazz) {
            LoginDependencies::class -> scopes.remove(scopeKey<LoginScope>(scopeId))
            StylesDependencies::class -> scopes.remove(scopeKey<StylesScope>(scopeId))
            SettingsDependencies::class -> scopes.remove(scopeKey<SettingsScope>(scopeId))
            BlacklistDependencies::class -> scopes.remove(scopeKey<BlacklistScope>(scopeId))
            WorkDependencies::class -> scopes.remove(scopeKey<WorkScope>(scopeId))
            SearchDependencies::class -> scopes.remove(scopeKey<SearchScope>(scopeId))
            ProfileDependencies::class -> scopes.remove(scopeKey<ProfileScope>(scopeId))
            else -> error("Unknown dependency type $clazz")
        }?.coroutineScope?.cancel()
    }

    override fun <T : Any> launchScoped(clazz: KClass<T>, id: String?, block: suspend CoroutineScope.() -> Unit) =
        scopes.getValue("${clazz.simpleName}=$id").coroutineScope.launch(block = block)

    private fun doInterop() {
        applicationScope.launch {
            var currentActivity: Activity? = null
            registerActivityLifecycleCallbacks(
                object : ActivityLifecycleCallbacks {
                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

                    override fun onActivityStarted(activity: Activity) = Unit

                    override fun onActivityResumed(activity: Activity) {
                        currentActivity = activity
                    }

                    override fun onActivityPaused(activity: Activity) {
                        currentActivity = null
                    }

                    override fun onActivityStopped(activity: Activity) = Unit

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

                    override fun onActivityDestroyed(activity: Activity) = Unit
                },
            )
            domainComponent.navigation().request.collect {
                val context = currentActivity ?: return@collect
                when (it) {
                    InteropRequest.BlackListScreen -> context.startActivity(BlacklistActivity.createIntent(context))
                    is InteropRequest.ShowToast -> {
                        withContext(AppDispatchers.Main) {
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }
                    }
                    is InteropRequest.PrivateMessage ->
                        context.startActivity(ConversationActivity.createIntent(context, it.profileId))
                    is InteropRequest.NewEntry ->
                        context.startActivity(AddEntryActivity.createIntent(context, null, "@${it.profileId}: "))
                }
                    .let { }
            }
        }
    }
}
