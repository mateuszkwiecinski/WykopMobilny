package io.github.wykopmobilny.initializers

import android.content.Context
import androidx.startup.Initializer
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

internal class LoggingInitializer : Initializer<Napier> {

    override fun create(context: Context): Napier {
        Napier.base(DebugAntilog())

        return Napier
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
