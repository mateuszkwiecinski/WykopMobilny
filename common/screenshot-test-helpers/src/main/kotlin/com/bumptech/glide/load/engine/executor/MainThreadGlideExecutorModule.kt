package com.bumptech.glide.load.engine.executor

import android.annotation.SuppressLint
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.StreamAssetPathFetcher
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

/**
 * https://github.com/bumptech/glide/issues/1440
 */
@GlideModule
class MainThreadGlideExecutorModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(GlideUrl::class.java, InputStream::class.java, AssetLoadingLoaderFactory(context))
    }

    @SuppressLint("VisibleForTests")
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val executor = GlideExecutor(DirectExecutorService())

        builder.apply {
            setDiskCacheExecutor(executor)
            setSourceExecutor(executor)
            setAnimationExecutor(executor)
        }
    }
}

private class AssetLoadingLoaderFactory(private val context: Context) : ModelLoaderFactory<GlideUrl, InputStream> {

    override fun build(multiFactory: MultiModelLoaderFactory) = object : ModelLoader<GlideUrl, InputStream> {

        override fun buildLoadData(model: GlideUrl, width: Int, height: Int, options: Options) =
            ModelLoader.LoadData(
                ObjectKey(model),
                StreamAssetPathFetcher(context.assets, model.toStringUrl().replaceFirst("https://www.wykop.pl/cdn/", "responses/")),
            )

        override fun handles(model: GlideUrl) = true
    }

    override fun teardown() = Unit
}

private class DirectExecutorService : AbstractExecutorService() {

    @Volatile
    private var terminated: Boolean = false

    override fun execute(command: Runnable) =
        command.run()

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
