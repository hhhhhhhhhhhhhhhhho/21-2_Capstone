package com.nanioi.closetapplication

import android.app.Application
import android.content.Context
import com.nanioi.closetapplication.retrofit.RetrofitService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class closetApplication : Application() {

    lateinit var service: RetrofitService

    companion object {
        var appContext: Context? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        //Stetho.initializeWithDefaults(this)
        createRetrofit()
        //chrome://inspect/#devices
    }
    fun createRetrofit(){
        val header = Interceptor {
            val request = it.request().newBuilder()
               // .header("Authorization","token ")
                .build()
            it.proceed(request)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(header)
            //.addNetworkInterceptor(StethoInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://mellowcode.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        service = retrofit.create(RetrofitService::class.java)
    }
    override fun onTerminate() {
        super.onTerminate()
        appContext = null
    }
}