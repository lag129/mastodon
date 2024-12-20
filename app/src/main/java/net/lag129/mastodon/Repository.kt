package net.lag129.mastodon

import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.lag129.mastodon.data.Status
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/v1/timelines/home")
    suspend fun fetchHomeData(
        @Query("max_id") maxId: String? = null,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int? = 20
    ): List<Status>

    @GET("/api/v1/timelines/public")
    suspend fun fetchLocalData(
        @Query("max_id") maxId: String? = null,
        @Query("local") local: Boolean? = true,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int? = 20
    ): List<Status>

    @GET("/api/v1/timelines/public")
    suspend fun fetchGlobalData(
        @Query("max_id") maxId: String? = null,
        @Query("local") local: Boolean? = false,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int? = 20
    ): List<Status>

    @GET("/api/v1/accounts/{id}/statuses")
    suspend fun fetchAccountData(
        @Path("id") id: String,
        @Query("max_id") maxId: String? = null,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int? = 20
    ): List<Status>

    @PUT("/api/v1/statuses/{id}/emoji_reactions/{emoji}")
    suspend fun postReaction(
        @Path("id") id: String,
        @Path("emoji") emoji: String
    ): Status

    @DELETE("/api/v1/statuses/{id}/emoji_reactions/{emoji}")
    suspend fun deleteReaction(
        @Path("id") id: String,
        @Path("emoji") emoji: String
    ): Status
}

object ApiClient {
    private const val BASE_URL = "https://fedibird.com"
    private const val BEARER_TOKEN = BuildConfig.BEARER_TOKEN

    private val gson: Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private val authInterceptor = Interceptor { chain ->
        val newRequest: Request = chain
            .request()
            .newBuilder()
            .addHeader("Authorization", "Bearer $BEARER_TOKEN")
            .build()
        chain.proceed(newRequest)
    }

    private val okHttpClient: OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(authInterceptor)
        .build()

    private val retrofit = Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
