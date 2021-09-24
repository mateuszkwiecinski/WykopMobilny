package io.github.wykopmobilny.wykop.remote

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

@Module
internal class RetrofitModule {

    @Provides
    @PathFixingInterceptor
    fun AppKeyReplacingInterceptor.pathFixingInterceptor(): Interceptor = this

    @Provides
    @Reusable
    fun moshi() = Moshi.Builder().apply {
        add(InstantAdapter())
    }
        .build()

    @Reusable
    @Provides
    fun retrofit(
        okHttpClient: OkHttpClient,
        @PathFixingInterceptor pathFixing: Interceptor,
        @SigningInterceptor signing: Interceptor,
        @BaseUrl apiUrl: String,
        cacheDir: File,
        moshi: Moshi,
    ) =
        Retrofit.Builder()
            .client(
                okHttpClient.newBuilder()
                    .cache(Cache(cacheDir, maxSize = CACHE_SIZE))
                    .addInterceptor(pathFixing)
                    .addInterceptor(signing)
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .build(),
            )
            .baseUrl(apiUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    companion object {

        private const val CACHE_SIZE = 10 * 1024 * 1024L
        private const val DEFAULT_TIMEOUT = 30L
    }
}
