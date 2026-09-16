package com.example.githubclient.data.graphql

import com.example.githubclient.data.model.*

/**
 * The app's screens and ViewModel were written against the REST-shaped
 * models in data/model/Models.kt. Rather than fork the UI per data source,
 * every GraphQL response gets mapped back onto those same model classes.
 * A couple of REST-only fields don't exist on every GraphQL node — those
 * are filled with a harmless default where the query didn't fetch them,
 * since nothing on screen reads them in that context.
 */
object GraphQLMapper {

    private fun actorToUser(actor: GqlActor?): GitHubUser =
        GitHubUser(
            login = actor?.login ?: "ghost",
            avatarUrl = actor?.avatarUrl,
            htmlUrl = actor?.url
        )

    fun toAuthenticatedUser(viewer: GqlViewer): AuthenticatedUser =
        AuthenticatedUser(
            login = viewer.login,
            name = viewer.name,
            avatarUrl = viewer.avatarUrl,
            bio = viewer.bio,
            publicRepos = viewer.repositories.totalCount,
            followers = viewer.followers.totalCount,
            following = viewer.following.totalCount
        )

    fun toRepos(viewer: GqlViewer): List<Repo> = viewer.repositories.nodes.map { r ->
        Repo(
            id = r.databaseId,
            name = r.name,
            fullName = r.nameWithOwner,
            description = r.description,
            stars = r.stargazerCount,
            forks = r.forkCount,
            openIssuesCount = r.issues.totalCount,
            language = r.primaryLanguage?.name,
            private = r.isPrivate,
            owner = actorToUser(r.owner),
            updatedAt = r.updatedAt,
            htmlUrl = r.url
        )
    }

    fun toIssue(g: GqlIssue): Issue = Issue(
        id = g.databaseId,
        number = g.number,
        title = g.title,
        body = g.body,
        state = g.state.lowercase(),
        user = actorToUser(g.author),
        labels = g.labels?.nodes?.map { Label(it.name, it.color) } ?: emptyList(),
        comments = g.comments.totalCount,
        createdAt = g.createdAt,
        closedAt = null,
        pullRequestRef = null,
        htmlUrl = g.url
    )

    fun toIssue(g: GqlIssueWithComments): Issue = Issue(
        id = g.databaseId,
        number = g.number,
        title = g.title,
        body = g.body,
        state = g.state.lowercase(),
        user = actorToUser(g.author),
        labels = g.labels?.nodes?.map { Label(it.name, it.color) } ?: emptyList(),
        comments = g.comments.nodes.size,
        createdAt = g.createdAt,
        closedAt = null,
        pullRequestRef = null,
        htmlUrl = g.url
    )

    fun toIssueComments(g: GqlIssueWithComments): List<IssueComment> = g.comments.nodes.map(::toComment)

    fun toComment(c: GqlComment): IssueComment = IssueComment(
        id = c.databaseId,
        user = actorToUser(c.author),
        body = c.body,
        createdAt = c.createdAt
    )

    fun toPullRequest(g: GqlPullRequest): PullRequest {
        // GraphQL folds "merged" into the state enum (OPEN/CLOSED/MERGED);
        // the REST model separates state from a merged boolean, so reconcile here.
        val merged = g.merged || g.state.equals("MERGED", ignoreCase = true)
        val restState = if (merged) "closed" else g.state.lowercase()
        return PullRequest(
            id = g.databaseId,
            number = g.number,
            title = g.title,
            body = g.body,
            state = restState,
            merged = merged,
            draft = g.isDraft,
            user = actorToUser(g.author),
            createdAt = g.createdAt,
            mergedAt = g.mergedAt,
            commitCount = g.commits?.totalCount,
            additions = g.additions,
            deletions = g.deletions,
            changedFiles = g.changedFiles,
            comments = null,
            reviewComments = null,
            head = Branch(ref = g.headRefName, label = null),
            base = Branch(ref = g.baseRefName, label = null),
            htmlUrl = g.url
        )
    }

    /** Node IDs (GraphQL's global `ID`) are needed for mutations and don't exist in the REST models at all. */
    fun nodeId(issue: GqlIssueWithComments) = issue.id
    fun nodeId(pr: GqlPullRequest) = pr.id
}
