package com.example.githubclient.data

import com.example.githubclient.data.graphql.GraphQLApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()
        val request = chain.request().newBuilder().apply {
            addHeader("Accept", "application/vnd.github+json")
            addHeader("X-GitHub-Api-Version", "2022-11-28")
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}

/** Bundles both clients so the rest of the app only needs to carry one object around. */
data class GitHubClients(val rest: GitHubApi, val graphql: GraphQLApi)

object NetworkModule {

    fun buildClients(tokenManager: TokenManager): GitHubClients {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // Both APIs authenticate the same way (Bearer token) and live under
        // the same host, so they share one OkHttp client and interceptor.
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return GitHubClients(
            rest = retrofit.create(GitHubApi::class.java),
            graphql = retrofit.create(GraphQLApi::class.java)
        )
    }
}
