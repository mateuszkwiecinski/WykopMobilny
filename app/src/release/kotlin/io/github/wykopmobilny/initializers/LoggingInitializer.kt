package io.github.wykopmobilny.initializers

import android.content.Context
import androidx.startup.Initializer
import io.github.aakira.napier.Napier

internal class LoggingInitializer : Initializer<Napier> {

    override fun create(context: Context): Napier {
        Napier.base(CrashlyticsAntilog())

        return Napier
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
