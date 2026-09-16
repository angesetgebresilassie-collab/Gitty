package com.example.githubclient.data.graphql

/**
 * Raw GraphQL documents sent to https://api.github.com/graphql.
 *
 * Kept as plain strings rather than using Apollo's codegen: codegen needs the
 * full GitHub schema downloaded at build time via an authenticated
 * introspection call, which is a heavier setup than this app's scope
 * justifies. A hand-written query + Gson-mapped response gets the same
 * "fetch nested data in one round trip" benefit with far less build
 * complexity.
 */
object GraphQLQueries {

    val VIEWER_AND_REPOS = """
        query ViewerAndRepos(${'$'}first: Int!) {
          viewer {
            login
            name
            avatarUrl
            bio
            followers { totalCount }
            following { totalCount }
            repositories(first: ${'$'}first, ownerAffiliations: [OWNER, COLLABORATOR], orderBy: {field: UPDATED_AT, direction: DESC}) {
              totalCount
              nodes {
                id
                databaseId
                name
                nameWithOwner
                description
                url
                isPrivate
                stargazerCount
                forkCount
                updatedAt
                primaryLanguage { name }
                issues(states: [OPEN]) { totalCount }
                owner { login avatarUrl url }
              }
            }
          }
        }
    """.trimIndent()

    val REPO_DETAIL = """
        query RepoDetail(${'$'}owner: String!, ${'$'}name: String!) {
          repository(owner: ${'$'}owner, name: ${'$'}name) {
            issues(first: 50, states: [OPEN], orderBy: {field: CREATED_AT, direction: DESC}) {
              nodes {
                id
                databaseId
                number
                title
                state
                url
                createdAt
                author { login avatarUrl url }
                labels(first: 10) { nodes { name color } }
                comments { totalCount }
              }
            }
            pullRequests(first: 50, states: [OPEN], orderBy: {field: CREATED_AT, direction: DESC}) {
              nodes {
                id
                databaseId
                number
                title
                state
                isDraft
                url
                createdAt
                author { login avatarUrl url }
                headRefName
                baseRefName
              }
            }
          }
        }
    """.trimIndent()

    val ISSUE_DETAIL = """
        query IssueDetail(${'$'}owner: String!, ${'$'}name: String!, ${'$'}number: Int!) {
          repository(owner: ${'$'}owner, name: ${'$'}name) {
            issue(number: ${'$'}number) {
              id
              databaseId
              number
              title
              state
              body
              url
              createdAt
              author { login avatarUrl url }
              labels(first: 20) { nodes { name color } }
              comments(first: 100) {
                nodes {
                  id
                  databaseId
                  body
                  createdAt
                  author { login avatarUrl url }
                }
              }
            }
          }
        }
    """.trimIndent()

    val PR_DETAIL = """
        query PrDetail(${'$'}owner: String!, ${'$'}name: String!, ${'$'}number: Int!) {
          repository(owner: ${'$'}owner, name: ${'$'}name) {
            pullRequest(number: ${'$'}number) {
              id
              databaseId
              number
              title
              state
              isDraft
              merged
              body
              url
              createdAt
              mergedAt
              author { login avatarUrl url }
              headRefName
              baseRefName
              additions
              deletions
              changedFiles
              commits { totalCount }
            }
          }
        }
    """.trimIndent()

    val ADD_COMMENT = """
        mutation AddComment(${'$'}subjectId: ID!, ${'$'}body: String!) {
          addComment(input: {subjectId: ${'$'}subjectId, body: ${'$'}body}) {
            commentEdge {
              node {
                id
                databaseId
                body
                createdAt
                author { login avatarUrl url }
              }
            }
          }
        }
    """.trimIndent()

    val MERGE_PR = """
        mutation MergePR(${'$'}pullRequestId: ID!) {
          mergePullRequest(input: {pullRequestId: ${'$'}pullRequestId}) {
            pullRequest {
              id
              state
              merged
              mergedAt
            }
          }
        }
    """.trimIndent()
}
