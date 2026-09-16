package com.example.githubclient.data.graphql

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * GitHub's GraphQL API is a single endpoint — every query and mutation is a
 * POST to /graphql with a {query, variables} body. Retrofit picks the right
 * response shape per call based on the declared return type here, so each
 * query gets its own method even though they all hit the same URL.
 */
interface GraphQLApi {

    @POST("graphql")
    suspend fun viewerAndRepos(@Body body: GraphQLRequestBody): GraphQLEnvelope<ViewerAndReposData>

    @POST("graphql")
    suspend fun repoDetail(@Body body: GraphQLRequestBody): GraphQLEnvelope<RepoDetailData>

    @POST("graphql")
    suspend fun issueDetail(@Body body: GraphQLRequestBody): GraphQLEnvelope<IssueDetailData>

    @POST("graphql")
    suspend fun prDetail(@Body body: GraphQLRequestBody): GraphQLEnvelope<PrDetailData>

    @POST("graphql")
    suspend fun addComment(@Body body: GraphQLRequestBody): GraphQLEnvelope<AddCommentData>

    @POST("graphql")
    suspend fun mergePr(@Body body: GraphQLRequestBody): GraphQLEnvelope<MergePrData>
}
