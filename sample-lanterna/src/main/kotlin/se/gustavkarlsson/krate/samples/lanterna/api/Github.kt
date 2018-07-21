package se.gustavkarlsson.krate.samples.lanterna.api

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

val github: GithubApi by lazy {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GithubApi::class.java)
}
