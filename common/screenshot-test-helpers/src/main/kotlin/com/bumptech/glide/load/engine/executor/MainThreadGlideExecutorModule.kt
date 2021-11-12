package com.bumptech.glide.load.engine.executor

import android.annotation.SuppressLint
import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

/**
 * https://github.com/bumptech/glide/issues/1440
 */
@GlideModule
class MainThreadGlideExecutorModule : AppGlideModule() {

    @SuppressLint("VisibleForTests")
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val executor = GlideExecutor(DirectExecutorService())

        builder.setDiskCacheExecutor(executor)
        builder.setSourceExecutor(executor)
        builder.setAnimationExecutor(executor)
    }
}

private class DirectExecutorService : AbstractExecutorService() {

    @Volatile
    private var terminated: Boolean = false

    override fun execute(command: Runnable) {
        command.run()
    }

    override fun isTerminated() = terminated

    override fun shutdown() {
        terminated = true
    }

    override fun shutdownNow(): List<Runnable> {
        shutdown()
        return listOf()
    }

    override fun isShutdown() = terminated

    override fun awaitTermination(timeout: Long, unit: TimeUnit?) = terminated
}
