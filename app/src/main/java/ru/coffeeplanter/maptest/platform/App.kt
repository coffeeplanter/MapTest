package ru.coffeeplanter.maptest.platform

import android.app.Application
import android.widget.Toast
import io.realm.Realm
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.coffeeplanter.maptest.BuildConfig
import ru.coffeeplanter.maptest.R
import ru.coffeeplanter.maptest.data.remote.ServerApi
import java.util.concurrent.TimeUnit

class App : Application() {

    private var mRetrofit: Retrofit? = null

    override fun onCreate() {
        super.onCreate()
        app = this
        Realm.init(this)
        val okHttpClientBuilder = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            okHttpClientBuilder.addInterceptor(logging)
        }
        mRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        serverApi = mRetrofit?.create(ServerApi::class.java)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Toast.makeText(this, getString(R.string.low_memory_caution), Toast.LENGTH_SHORT).show()
    }

    companion object {

        @Suppress("unused")
        private val TAG = App::class.java.simpleName

        private const val BASE_URL = "https://work.gofura.com/"

        var serverApi: ServerApi? = null
            private set

        var app: App? = null
            private set
    }

}
