package io.github.wykopmobilny.tests.rules

import android.content.Context
import android.webkit.CookieManager
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import io.github.wykopmobilny.TestApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class CleanupRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            val application = ApplicationProvider.getApplicationContext<TestApp>()
            application.getSharedPreferences("Preferences", Context.MODE_PRIVATE).edit { clear() }

            runBlocking {
                withContext(Dispatchers.Main) {
                    suspendCoroutine<Unit> { continuation ->
                        CookieManager.getInstance().removeAllCookies { continuation.resume(Unit) }
                    }
                }
            }
            application.storages.storage().linksQueries.deleteAll()
            application.storages.storage().preferencesQueries.deleteAll()
            application.storages.storage().blacklistQueries.deleteAll()
            application.storages.storage().suggestionsQueries.deleteAll()

            base.evaluate()
        }
    }
}
