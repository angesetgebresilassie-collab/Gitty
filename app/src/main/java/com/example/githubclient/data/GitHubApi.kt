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

    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CreateIssueBody
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

    @GET("repos/{owner}/{repo}/pulls/{number}/files")
    suspend fun getPullRequestFiles(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int,
        @Query("per_page") perPage: Int = 100
    ): List<PrFile>

    @PUT("repos/{owner}/{repo}/pulls/{number}/merge")
    suspend fun mergePullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: Int
    ): retrofit2.Response<Unit>

    @POST("repos/{owner}/{repo}/pulls")
    suspend fun createPullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CreatePrBody
    ): PullRequest

    @GET("repos/{owner}/{repo}/branches")
    suspend fun getBranches(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 100
    ): List<BranchRef>

    // GitHub's contents endpoint returns a JSON array for a directory and a
    // single object for a file, at the exact same URL. We declare it twice
    // with different return types and call whichever one fits the caller's
    // context (a directory listing already told us which entries are files).
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getDirectoryContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String = "",
        @Query("ref") ref: String? = null
    ): List<RepoContent>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Query("ref") ref: String? = null
    ): RepoContent

    // --- GitHub Actions ---

    @GET("repos/{owner}/{repo}/commits/{ref}/check-runs")
    suspend fun getCheckRuns(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("ref") ref: String,
        @Query("per_page") perPage: Int = 50
    ): CheckRunsResponse

    @GET("repos/{owner}/{repo}/actions/runs")
    suspend fun getWorkflowRuns(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 30
    ): WorkflowRunsResponse

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

data class CheckRunsResponse(
    @com.google.gson.annotations.SerializedName("total_count") val totalCount: Int,
    @com.google.gson.annotations.SerializedName("check_runs") val checkRuns: List<CheckRun>
)

data class WorkflowRunsResponse(
    @com.google.gson.annotations.SerializedName("total_count") val totalCount: Int,
    @com.google.gson.annotations.SerializedName("workflow_runs") val workflowRuns: List<WorkflowRun>
)

data class CommentBody(val body: String)
data class StateBody(val state: String)
data class CreateIssueBody(val title: String, val body: String?)
data class CreatePrBody(val title: String, val body: String?, val head: String, val base: String, val draft: Boolean = false)
