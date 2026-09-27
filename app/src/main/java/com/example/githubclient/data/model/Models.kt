package com.example.githubclient.data.model

import com.google.gson.annotations.SerializedName

data class GitHubUser(
    val login: String,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("html_url") val htmlUrl: String?
)

data class Repo(
    val id: Long,
    val name: String,
    @SerializedName("full_name") val fullName: String,
    val description: String?,
    @SerializedName("stargazers_count") val stars: Int,
    @SerializedName("forks_count") val forks: Int,
    @SerializedName("open_issues_count") val openIssuesCount: Int,
    val language: String?,
    val private: Boolean,
    val owner: GitHubUser,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("html_url") val htmlUrl: String
)

data class Label(
    val name: String,
    val color: String
)

// GitHub's REST API returns pull requests as a subtype of issues.
// The presence of this field on an "issue" is how we tell them apart when listing.
data class PullRequestRef(
    val url: String?
)

data class Issue(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String?,
    val state: String, // "open" or "closed"
    val user: GitHubUser,
    val labels: List<Label>,
    val comments: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("closed_at") val closedAt: String?,
    @SerializedName("pull_request") val pullRequestRef: PullRequestRef?,
    @SerializedName("html_url") val htmlUrl: String
) {
    val isPullRequest: Boolean get() = pullRequestRef != null
}

data class PullRequest(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String?,
    val state: String, // "open" or "closed"
    val merged: Boolean,
    val draft: Boolean,
    val user: GitHubUser,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("merged_at") val mergedAt: String?,
    @SerializedName("commits") val commitCount: Int?,
    @SerializedName("additions") val additions: Int?,
    @SerializedName("deletions") val deletions: Int?,
    @SerializedName("changed_files") val changedFiles: Int?,
    val comments: Int?,
    @SerializedName("review_comments") val reviewComments: Int?,
    val head: Branch,
    val base: Branch,
    @SerializedName("html_url") val htmlUrl: String
)

data class Branch(
    val ref: String,
    val label: String?
)

data class IssueComment(
    val id: Long,
    val user: GitHubUser,
    val body: String,
    @SerializedName("created_at") val createdAt: String
)

data class GhNotification(
    val id: String,
    val unread: Boolean,
    val reason: String,
    val subject: NotificationSubject,
    val repository: Repo,
    @SerializedName("updated_at") val updatedAt: String
)

data class NotificationSubject(
    val title: String,
    val type: String, // "Issue", "PullRequest", "Release", etc.
    val url: String?
)

data class AuthenticatedUser(
    val login: String,
    val name: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    val bio: String?,
    @SerializedName("public_repos") val publicRepos: Int,
    val followers: Int,
    val following: Int
)

// --- PR diff viewer ---

data class PrFile(
    val filename: String,
    val status: String, // "added" | "removed" | "modified" | "renamed"
    val additions: Int,
    val deletions: Int,
    val changes: Int,
    // Unified diff text for this file. Absent for binary files or very large diffs.
    val patch: String?
)

// --- Create PR (branch pickers) ---

data class BranchRef(
    val name: String
)

// --- Repo file browser ---

data class RepoContent(
    val name: String,
    val path: String,
    val type: String, // "file" or "dir"
    val size: Int,
    // Base64 file content. Only present when fetching a single file, and only
    // when the file is under GitHub's ~1MB inline-content limit.
    val content: String?,
    val encoding: String?,
    @SerializedName("download_url") val downloadUrl: String?,
    @SerializedName("html_url") val htmlUrl: String?
) {
    val isDirectory: Boolean get() = type == "dir"
}

/** Decodes [RepoContent.content] to text, or null if it's missing/binary/undecodable. */
fun RepoContent.decodedText(): String? {
    if (content.isNullOrBlank() || !encoding.equals("base64", ignoreCase = true)) return null
    return try {
        val clean = content.replace("\n", "")
        val bytes = android.util.Base64.decode(clean, android.util.Base64.DEFAULT)
        val text = String(bytes, Charsets.UTF_8)
        // A decoded "text" file full of replacement characters is actually binary.
        if (text.count { it == '\uFFFD' } > text.length / 10) null else text
    } catch (e: Exception) {
        null
    }
}
