package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class MoshiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class MoshiContent(
    @Json(name = "parts") val parts: List<MoshiPart>
)

@JsonClass(generateAdapter = true)
data class MoshiGenerateContentRequest(
    @Json(name = "contents") val contents: List<MoshiContent>
)

@JsonClass(generateAdapter = true)
data class MoshiCandidate(
    @Json(name = "content") val content: MoshiContent
)

@JsonClass(generateAdapter = true)
data class MoshiGenerateContentResponse(
    @Json(name = "candidates") val candidates: List<MoshiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: MoshiGenerateContentRequest
    ): MoshiGenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

suspend fun getGeminiCoachInsight(prompt: String): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext "Identify your mood triggers, delay action by 10 minutes when an urge strikes, and reinforce self-control daily to construct healthy neural pathways."
    }
    
    val request = MoshiGenerateContentRequest(
        contents = listOf(
            MoshiContent(
                parts = listOf(MoshiPart(text = prompt))
            )
        )
    )

    try {
        val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Resist urges with deliberate replacement habits. Focus on early awareness of triggers."
    } catch (e: Exception) {
        "Resist urges with deliberate replacement habits. Focus on early awareness of triggers."
    }
}
