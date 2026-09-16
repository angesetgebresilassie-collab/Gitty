package com.example.githubclient.data.graphql

import com.google.gson.annotations.SerializedName

data class GraphQLRequestBody(val query: String, val variables: Map<String, Any?> = emptyMap())

data class GraphQLError(val message: String)

data class GraphQLEnvelope<T>(val data: T?, val errors: List<GraphQLError>?) {
    fun unwrap(): T {
        if (data != null) return data
        val message = errors?.firstOrNull()?.message ?: "GitHub's GraphQL API returned no data."
        throw GraphQLException(message)
    }
}

class GraphQLException(message: String) : Exception(message)

// --- Shared fragments ---

data class GqlActor(val login: String, val avatarUrl: String?, val url: String?)

data class GqlLabelNodes(val nodes: List<GqlLabel>)
data class GqlLabel(val name: String, val color: String)

data class GqlCountConnection(val totalCount: Int)

// --- ViewerAndRepos ---

data class ViewerAndReposData(val viewer: GqlViewer)

data class GqlViewer(
    val login: String,
    val name: String?,
    val avatarUrl: String?,
    val bio: String?,
    val followers: GqlCountConnection,
    val following: GqlCountConnection,
    val repositories: GqlRepoConnection
)

data class GqlRepoConnection(val totalCount: Int, val nodes: List<GqlRepo>)

data class GqlRepo(
    val id: String,
    val databaseId: Long,
    val name: String,
    val nameWithOwner: String,
    val description: String?,
    val url: String,
    val isPrivate: Boolean,
    val stargazerCount: Int,
    val forkCount: Int,
    val updatedAt: String,
    val primaryLanguage: GqlLanguage?,
    val issues: GqlCountConnection,
    val owner: GqlActor
)

data class GqlLanguage(val name: String)

// --- RepoDetail ---

data class RepoDetailData(val repository: GqlRepoDetail?)

data class GqlRepoDetail(
    val issues: GqlIssueConnection,
    val pullRequests: GqlPrConnection
)

data class GqlIssueConnection(val nodes: List<GqlIssue>)
data class GqlPrConnection(val nodes: List<GqlPullRequest>)

data class GqlIssue(
    val id: String,
    val databaseId: Long,
    val number: Int,
    val title: String,
    val state: String, // OPEN | CLOSED
    val url: String,
    val createdAt: String,
    val author: GqlActor?,
    val labels: GqlLabelNodes?,
    val comments: GqlCountConnection,
    val body: String? = null // present only on the IssueDetail query
)

data class GqlPullRequest(
    val id: String,
    val databaseId: Long,
    val number: Int,
    val title: String,
    val state: String, // OPEN | CLOSED | MERGED
    val isDraft: Boolean,
    val merged: Boolean = false,
    val url: String,
    val createdAt: String,
    val mergedAt: String? = null,
    val author: GqlActor?,
    val headRefName: String,
    val baseRefName: String,
    val body: String? = null,
    val additions: Int? = null,
    val deletions: Int? = null,
    val changedFiles: Int? = null,
    val commits: GqlCountConnection? = null
)

// --- IssueDetail ---

data class IssueDetailData(val repository: GqlIssueDetailRepo?)
data class GqlIssueDetailRepo(val issue: GqlIssueWithComments?)

data class GqlIssueWithComments(
    val id: String,
    val databaseId: Long,
    val number: Int,
    val title: String,
    val state: String,
    val body: String?,
    val url: String,
    val createdAt: String,
    val author: GqlActor?,
    val labels: GqlLabelNodes?,
    val comments: GqlCommentConnection
)

data class GqlCommentConnection(val nodes: List<GqlComment>)

data class GqlComment(
    val id: String,
    val databaseId: Long,
    val body: String,
    val createdAt: String,
    val author: GqlActor?
)

// --- PrDetail ---

data class PrDetailData(val repository: GqlPrDetailRepo?)
data class GqlPrDetailRepo(val pullRequest: GqlPullRequest?)

// --- Mutations ---

data class AddCommentData(val addComment: GqlAddCommentPayload?)
data class GqlAddCommentPayload(val commentEdge: GqlCommentEdge?)
data class GqlCommentEdge(val node: GqlComment)

data class MergePrData(val mergePullRequest: GqlMergePayload?)
data class GqlMergePayload(val pullRequest: GqlMergedPr?)
data class GqlMergedPr(val id: String, val state: String, val merged: Boolean, val mergedAt: String?)
