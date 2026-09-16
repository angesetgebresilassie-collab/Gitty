package com.example.githubclient.data

import com.example.githubclient.data.model.*
import retrofit2.http.*

interface GitHubApi {

    @GET("user")
    suspend fun getAuthenticatedUser(): AuthenticatedUser

    @GET("user/repos")
    suspend fun getMyRepos(
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 50
    ): List<Repo>

    @GET("search/repositories")
    suspend fun searchRepos(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 30
    ): SearchReposResponse

    @GET("repos/{owner}/{repo}/issues")
    suspend fun getIssues(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "open",
        @Query("per_page") perPage: Int = 50
    ): List<Issue>

    @GET("repos/{owner}/{repo}/issues/{number}")
    suspend fun getIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int
    ): Issue

    @GET("repos/{owner}/{repo}/issues/{number}/comments")
    suspend fun getIssueComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int
    ): List<IssueComment>

    @POST("repos/{owner}/{repo}/issues/{number}/comments")
    suspend fun postIssueComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int,
        @Body body: CommentBody
    ): IssueComment

    @PATCH("repos/{owner}/{repo}/issues/{number}")
    suspend fun updateIssueState(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int,
        @Body body: StateBody
    ): Issue

    @GET("repos/{owner}/{repo}/pulls")
    suspend fun getPullRequests(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "open",
        @Query("per_page") perPage: Int = 50
    ): List<PullRequest>

    @GET("repos/{owner}/{repo}/pulls/{number}")
    suspend fun getPullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int
    ): PullRequest

    @PUT("repos/{owner}/{repo}/pulls/{number}/merge")
    suspend fun mergePullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int
    ): retrofit2.Response<Unit>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("all") all: Boolean = false
    ): List<GhNotification>

    @PATCH("notifications/threads/{id}")
    suspend fun markNotificationRead(
        @Path("id") id: String
    ): retrofit2.Response<Unit>
}

data class SearchReposResponse(
    @com.google.gson.annotations.SerializedName("total_count") val totalCount: Int,
    val items: List<Repo>
)

data class CommentBody(val body: String)
data class StateBody(val state: String)
