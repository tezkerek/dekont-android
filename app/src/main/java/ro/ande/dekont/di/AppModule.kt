package ro.ande.dekont.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.threeten.bp.LocalDate
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ro.ande.dekont.R
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.db.CategoryDao
import ro.ande.dekont.db.DekontDatabase
import ro.ande.dekont.db.TransactionDao
import ro.ande.dekont.ui.AuthActivity
import ro.ande.dekont.util.*
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(app: Application): OkHttpClient =
            OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val token = app.getSharedPreferences(AuthActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString("token", null)

                        val originalRequest = chain.request()

                        val request =
                                if (token != null) originalRequest.newBuilder()
                                        .addHeader("Authorization", "Token $token")
                                        .build()
                                else originalRequest

                        chain.proceed(request)
                    }
                    .addInterceptor { chain ->
                        val request = chain.request()

                        Log.d("LoggingInterceptor", "Sending request ${request.url()} on ${chain.connection()} with ${request.headers()}")

                        val response = chain.proceed(request)

                        Log.d("LoggingInterceptor", "Received response for ${response.request().url()} with ${response.headers()}")

                        response
                    }
                    .addInterceptor { chain ->
                        // Throw exception if internet is not available
                        val connectivityManager = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        if (connectivityManager.activeNetworkInfo?.isConnected == false)
                            throw IOException(app.getString(R.string.error_missing_internet_connection))

                        chain.proceed(chain.request())
                    }
                    .build()

    @Provides
    @Singleton
    fun provideDekontService(client: OkHttpClient, gson: Gson): DekontService =
            Retrofit.Builder()
                    .baseUrl(DEKONT_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(LiveDataCallAdapterFactory())
                    .addCallAdapterFactory(ApiResponseCallAdapterFactory())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .build()
                    .create(DekontService::class.java)

    @Provides
    fun provideGson(): Gson =
            GsonBuilder()
                    .registerTypeAdapter(LocalDate::class.java, GsonLocalDateSerializer())
                    .registerTypeAdapter(LocalDate::class.java, GsonLocalDateDeserializer())
                    .registerTypeAdapter(Currency::class.java, GsonCurrencySerializer())
                    .registerTypeAdapter(Currency::class.java, GsonCurrencyDeserializer())
                    .create()

    @Provides
    @Singleton
    fun provideDatabase(app: Application): DekontDatabase =
            Room.databaseBuilder(app, DekontDatabase::class.java, "dekont.db")
                    .fallbackToDestructiveMigration()
                    .build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: DekontDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideCategoryDao(db: DekontDatabase): CategoryDao = db.categoryDao()

    companion object {
        const val DEKONT_BASE_URL = "http://192.168.43.108:8080"
    }
}
