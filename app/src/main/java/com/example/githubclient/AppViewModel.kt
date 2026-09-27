package com.example.githubclient

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.githubclient.data.GitHubClients
import com.example.githubclient.data.TokenManager
import com.example.githubclient.data.CreateIssueBody
import com.example.githubclient.data.CreatePrBody
import com.example.githubclient.data.graphql.*
import com.example.githubclient.data.model.*
import com.example.githubclient.ui.screens.LoadState
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AppViewModel(
    private val tokenManager: TokenManager,
    private var clients: GitHubClients?
) : ViewModel() {

    // --- Auth ---
    var isLoggedIn by mutableStateOf(tokenManager.hasToken())
        private set
    var isValidatingToken by mutableStateOf(false)
        private set
    var loginError by mutableStateOf<String?>(null)
        private set

    // --- Global data ---
    var user by mutableStateOf<LoadState<AuthenticatedUser>>(LoadState.Loading)
        private set
    var repos by mutableStateOf<LoadState<List<Repo>>>(LoadState.Loading)
        private set
    var notifications by mutableStateOf<LoadState<List<GhNotification>>>(LoadState.Loading)
        private set

    // --- Repo detail data ---
    var issues by mutableStateOf<LoadState<List<Issue>>>(LoadState.Loading)
        private set
    var pullRequests by mutableStateOf<LoadState<List<PullRequest>>>(LoadState.Loading)
        private set

    // --- Issue detail ---
    var currentIssue by mutableStateOf<LoadState<Issue>>(LoadState.Loading)
        private set
    var issueComments by mutableStateOf<LoadState<List<IssueComment>>>(LoadState.Loading)
        private set
    var isSendingComment by mutableStateOf(false)
        private set
    // GraphQL global node ID for the open issue -- REST's numeric id won't work for the addComment mutation.
    private var currentIssueNodeId: String? = null

    // --- PR detail ---
    var currentPr by mutableStateOf<LoadState<PullRequest>>(LoadState.Loading)
        private set
    var isMerging by mutableStateOf(false)
        private set
    var mergeError by mutableStateOf<String?>(null)
        private set
    private var currentPrNodeId: String? = null

    // --- PR diff viewer ---
    var prFiles by mutableStateOf<LoadState<List<PrFile>>>(LoadState.Loading)
        private set

    // --- Repo file browser ---
    var repoBrowser by mutableStateOf<LoadState<List<RepoContent>>>(LoadState.Loading)
        private set
    var fileContent by mutableStateOf<LoadState<RepoContent>>(LoadState.Loading)
        private set

    // --- Branches (for the create-PR branch pickers) ---
    var branches by mutableStateOf<LoadState<List<BranchRef>>>(LoadState.Loading)
        private set

    // --- Create issue / PR ---
    var isCreatingIssue by mutableStateOf(false)
        private set
    var createIssueError by mutableStateOf<String?>(null)
        private set
    var isCreatingPr by mutableStateOf(false)
        private set
    var createPrError by mutableStateOf<String?>(null)
        private set

    fun updateApi(newClients: GitHubClients) {
        clients = newClients
    }

    fun submitToken(token: String, onRefreshApi: () -> Unit) {
        loginError = null
        isValidatingToken = true
        tokenManager.saveToken(token)
        onRefreshApi()
        viewModelScope.launch {
            try {
                val envelope = clients?.graphql?.viewerAndRepos(
                    GraphQLRequestBody(GraphQLQueries.VIEWER_AND_REPOS, mapOf("first" to 1))
                )
                val viewer = envelope?.unwrap()?.viewer
                if (viewer != null) {
                    isLoggedIn = true
                    loadInitialData()
                } else {
                    throw IllegalStateException("Client not ready")
                }
            } catch (e: HttpException) {
                tokenManager.clearToken()
                loginError = if (e.code() == 401) "That token was rejected. Double-check it and try again." else "GitHub returned an error (${e.code()})."
            } catch (e: Exception) {
                tokenManager.clearToken()
                loginError = "Couldn't reach GitHub. Check your connection and try again."
            } finally {
                isValidatingToken = false
            }
        }
    }

    fun signOut() {
        tokenManager.clearToken()
        isLoggedIn = false
        user = LoadState.Loading
        repos = LoadState.Loading
        notifications = LoadState.Loading
    }

    fun loadInitialData() {
        loadViewerAndRepos()
        loadNotifications()
    }

    fun loadViewerAndRepos() {
        user = LoadState.Loading
        repos = LoadState.Loading
        viewModelScope.launch {
            try {
                val viewer = clients!!.graphql.viewerAndRepos(
                    GraphQLRequestBody(GraphQLQueries.VIEWER_AND_REPOS, mapOf("first" to 50))
                ).unwrap().viewer
                user = LoadState.Success(GraphQLMapper.toAuthenticatedUser(viewer))
                repos = LoadState.Success(GraphQLMapper.toRepos(viewer))
            } catch (e: Exception) {
                val err = LoadState.Error(friendlyMessage(e))
                user = err
                repos = err
            }
        }
    }

    fun loadUser() = loadViewerAndRepos()
    fun loadRepos() = loadViewerAndRepos()

    fun loadNotifications() {
        notifications = LoadState.Loading
        viewModelScope.launch {
            notifications = try {
                LoadState.Success(clients!!.rest.getNotifications())
            } catch (e: Exception) {
                LoadState.Error(friendlyMessage(e))
            }
        }
    }

    fun loadRepoDetail(owner: String, name: String) {
        issues = LoadState.Loading
        pullRequests = LoadState.Loading
        viewModelScope.launch {
            try {
                val repo = clients!!.graphql.repoDetail(
                    GraphQLRequestBody(GraphQLQueries.REPO_DETAIL, mapOf("owner" to owner, "name" to name))
                ).unwrap().repository

                issues = LoadState.Success(repo?.issues?.nodes?.map(GraphQLMapper::toIssue) ?: emptyList())
                pullRequests = LoadState.Success(repo?.pullRequests?.nodes?.map(GraphQLMapper::toPullRequest) ?: emptyList())
            } catch (e: Exception) {
                val err = LoadState.Error(friendlyMessage(e))
                issues = err
                pullRequests = err
            }
        }
    }

    fun loadIssueDetail(owner: String, name: String, number: Int) {
        currentIssue = LoadState.Loading
        issueComments = LoadState.Loading
        currentIssueNodeId = null
        viewModelScope.launch {
            try {
                val issue = clients!!.graphql.issueDetail(
                    GraphQLRequestBody(GraphQLQueries.ISSUE_DETAIL, mapOf("owner" to owner, "name" to name, "number" to number))
                ).unwrap().repository?.issue

                if (issue == null) {
                    val err = LoadState.Error("That issue couldn't be found.")
                    currentIssue = err
                    issueComments = err
                } else {
                    currentIssueNodeId = GraphQLMapper.nodeId(issue)
                    currentIssue = LoadState.Success(GraphQLMapper.toIssue(issue))
                    issueComments = LoadState.Success(GraphQLMapper.toIssueComments(issue))
                }
            } catch (e: Exception) {
                val err = LoadState.Error(friendlyMessage(e))
                currentIssue = err
                issueComments = err
            }
        }
    }

    fun sendComment(owner: String, name: String, number: Int, body: String) {
        if (body.isBlank()) return
        val subjectId = currentIssueNodeId ?: return
        isSendingComment = true
        viewModelScope.launch {
            try {
                val newComment = clients!!.graphql.addComment(
                    GraphQLRequestBody(GraphQLQueries.ADD_COMMENT, mapOf("subjectId" to subjectId, "body" to body))
                ).unwrap().addComment?.commentEdge?.node

                if (newComment != null) {
                    val mapped = GraphQLMapper.toComment(newComment)
                    val current = issueComments
                    if (current is LoadState.Success) {
                        issueComments = LoadState.Success(current.data + mapped)
                    }
                }
            } catch (e: Exception) {
                // Non-fatal: leave existing comments as-is.
            } finally {
                isSendingComment = false
            }
        }
    }

    fun createIssue(owner: String, name: String, title: String, body: String, onSuccess: (Issue) -> Unit) {
        if (title.isBlank()) return
        isCreatingIssue = true
        createIssueError = null
        viewModelScope.launch {
            try {
                val issue = clients!!.rest.createIssue(owner, name, CreateIssueBody(title, body.ifBlank { null }))
                onSuccess(issue)
            } catch (e: Exception) {
                createIssueError = friendlyMessage(e)
            } finally {
                isCreatingIssue = false
            }
        }
    }

    fun loadPrDetail(owner: String, name: String, number: Int) {
        currentPr = LoadState.Loading
        mergeError = null
        currentPrNodeId = null
        viewModelScope.launch {
            try {
                val pr = clients!!.graphql.prDetail(
                    GraphQLRequestBody(GraphQLQueries.PR_DETAIL, mapOf("owner" to owner, "name" to name, "number" to number))
                ).unwrap().repository?.pullRequest

                if (pr == null) {
                    currentPr = LoadState.Error("That pull request couldn't be found.")
                } else {
                    currentPrNodeId = GraphQLMapper.nodeId(pr)
                    currentPr = LoadState.Success(GraphQLMapper.toPullRequest(pr))
                }
            } catch (e: Exception) {
                currentPr = LoadState.Error(friendlyMessage(e))
            }
        }
    }

    fun mergePr(owner: String, name: String, number: Int) {
        val pullRequestId = currentPrNodeId ?: return
        isMerging = true
        mergeError = null
        viewModelScope.launch {
            try {
                val result = clients!!.graphql.mergePr(
                    GraphQLRequestBody(GraphQLQueries.MERGE_PR, mapOf("pullRequestId" to pullRequestId))
                ).unwrap().mergePullRequest?.pullRequest

                if (result != null) {
                    loadPrDetail(owner, name, number)
                } else {
                    mergeError = "GitHub couldn't merge this. It may need review or have conflicts."
                }
            } catch (e: Exception) {
                mergeError = friendlyMessage(e)
            } finally {
                isMerging = false
            }
        }
    }

    fun loadPrFiles(owner: String, name: String, number: Int) {
        prFiles = LoadState.Loading
        viewModelScope.launch {
            prFiles = try {
                LoadState.Success(clients!!.rest.getPullRequestFiles(owner, name, number))
            } catch (e: Exception) {
                LoadState.Error(friendlyMessage(e))
            }
        }
    }

    fun createPullRequest(
        owner: String,
        name: String,
        title: String,
        body: String,
        head: String,
        base: String,
        draft: Boolean,
        onSuccess: (PullRequest) -> Unit
    ) {
        if (title.isBlank() || head.isBlank() || base.isBlank()) return
        isCreatingPr = true
        createPrError = null
        viewModelScope.launch {
            try {
                val pr = clients!!.rest.createPullRequest(owner, name, CreatePrBody(title, body.ifBlank { null }, head, base, draft))
                onSuccess(pr)
            } catch (e: Exception) {
                createPrError = friendlyMessage(e)
            } finally {
                isCreatingPr = false
            }
        }
    }

    fun loadBranches(owner: String, name: String) {
        branches = LoadState.Loading
        viewModelScope.launch {
            branches = try {
                LoadState.Success(clients!!.rest.getBranches(owner, name))
            } catch (e: Exception) {
                LoadState.Error(friendlyMessage(e))
            }
        }
    }

    fun loadRepoContents(owner: String, name: String, path: String) {
        repoBrowser = LoadState.Loading
        viewModelScope.launch {
            repoBrowser = try {
                val items = clients!!.rest.getDirectoryContents(owner, name, path)
                    .sortedWith(compareByDescending<RepoContent> { it.isDirectory }.thenBy { it.name.lowercase() })
                LoadState.Success(items)
            } catch (e: Exception) {
                LoadState.Error(friendlyMessage(e))
            }
        }
    }

    fun loadFileContent(owner: String, name: String, path: String) {
        fileContent = LoadState.Loading
        viewModelScope.launch {
            fileContent = try {
                LoadState.Success(clients!!.rest.getFileContent(owner, name, path))
            } catch (e: Exception) {
                LoadState.Error(friendlyMessage(e))
            }
        }
    }

    private fun friendlyMessage(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            401 -> "Your token is no longer valid. Try signing out and back in."
            403 -> "GitHub blocked this request -- you may have hit a rate limit."
            404 -> "That couldn't be found. It may have been deleted or you lack access."
            422 -> "GitHub rejected that -- double-check the details and try again."
            else -> "GitHub returned an error (${e.code()})."
        }
        is GraphQLException -> e.message ?: "GitHub's GraphQL API returned an error."
        else -> "Couldn't reach GitHub. Check your connection."
    }
}
