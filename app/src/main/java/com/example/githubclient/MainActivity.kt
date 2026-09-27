package com.example.githubclient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.githubclient.navigation.Destinations
import com.example.githubclient.ui.screens.*
import com.example.githubclient.ui.theme.GitHubClientTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = application as GitHubClientApp
                @Suppress("UNCHECKED_CAST")
                return AppViewModel(app.tokenManager, app.clients) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GitHubClientTheme {
                Surface {
                    HubApp(
                        viewModel = viewModel,
                        refreshApiAndGet = {
                            val app = application as GitHubClientApp
                            app.refreshApi()
                            app.clients!!
                        }
                    )
                }
            }
        }
    }
}

private data class BottomDestination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun HubApp(viewModel: AppViewModel, refreshApiAndGet: () -> com.example.githubclient.data.GitHubClients) {
    if (!viewModel.isLoggedIn) {
        LoginScreen(
            onTokenSaved = {},
            isValidating = viewModel.isValidatingToken,
            errorMessage = viewModel.loginError,
            onSubmitToken = { token ->
                viewModel.submitToken(token) {
                    val newApi = refreshApiAndGet()
                    viewModel.updateApi(newApi)
                }
            }
        )
        return
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    val navController = rememberNavController()
    val context = LocalContext.current
    val bottomDestinations = listOf(
        BottomDestination(Destinations.REPOS, "Repos", Icons.Filled.Folder),
        BottomDestination(Destinations.NOTIFICATIONS, "Alerts", Icons.Filled.Notifications),
        BottomDestination(Destinations.PROFILE, "Profile", Icons.Filled.Person)
    )

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            // Only show the bottom bar on the three top-level tabs, not on detail screens.
            val showBar = bottomDestinations.any { it.route == currentRoute?.route }
            if (showBar) {
                NavigationBar {
                    bottomDestinations.forEach { dest ->
                        val selected = currentRoute?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.REPOS,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destinations.REPOS) {
                ReposScreen(
                    user = viewModel.user,
                    repos = viewModel.repos,
                    onRetry = { viewModel.loadRepos() },
                    onRepoClick = { repo ->
                        viewModel.loadRepoDetail(repo.owner.login, repo.name)
                        navController.navigate(Destinations.repoDetail(repo.owner.login, repo.name))
                    }
                )
            }
            composable(Destinations.NOTIFICATIONS) {
                NotificationsScreen(
                    notifications = viewModel.notifications,
                    onRetry = { viewModel.loadNotifications() },
                    onNotificationClick = { /* Could deep-link into the relevant issue/PR */ }
                )
            }
            composable(Destinations.PROFILE) {
                ProfileScreen(
                    user = viewModel.user,
                    onRetry = { viewModel.loadUser() },
                    onSignOut = {
                        viewModel.signOut()
                        navController.navigate(Destinations.REPOS) { popUpTo(0) }
                    }
                )
            }
            composable(
                route = Destinations.REPO_DETAIL,
                arguments = listOf(navArgument("owner") { type = NavType.StringType }, navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                val name = backStackEntry.arguments?.getString("name").orEmpty()
                var tab by remember { mutableStateOf(RepoTab.ISSUES) }

                RepoDetailScreen(
                    repoFullName = "$owner/$name",
                    selectedTab = tab,
                    onTabSelected = { tab = it },
                    issues = viewModel.issues,
                    pullRequests = viewModel.pullRequests,
                    onBack = { navController.popBackStack() },
                    onIssueClick = { issue ->
                        viewModel.loadIssueDetail(owner, name, issue.number)
                        navController.navigate(Destinations.issueDetail(owner, name, issue.number))
                    },
                    onPrClick = { pr ->
                        viewModel.loadPrDetail(owner, name, pr.number)
                        navController.navigate(Destinations.prDetail(owner, name, pr.number))
                    },
                    onRetry = { viewModel.loadRepoDetail(owner, name) },
                    onBrowseFiles = { navController.navigate(Destinations.repoFiles(owner, name)) },
                    onNewIssue = { navController.navigate(Destinations.createIssue(owner, name)) },
                    onNewPr = { navController.navigate(Destinations.createPr(owner, name)) }
                )
            }
            composable(
                route = Destinations.ISSUE_DETAIL,
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType },
                    navArgument("number") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                val name = backStackEntry.arguments?.getString("name").orEmpty()
                val number = backStackEntry.arguments?.getInt("number") ?: 0

                IssueDetailScreen(
                    issue = viewModel.currentIssue,
                    comments = viewModel.issueComments,
                    onBack = { navController.popBackStack() },
                    onRetry = { viewModel.loadIssueDetail(owner, name, number) },
                    onSendComment = { body -> viewModel.sendComment(owner, name, number, body) },
                    isSendingComment = viewModel.isSendingComment
                )
            }
            composable(
                route = Destinations.PR_DETAIL,
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType },
                    navArgument("number") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                val name = backStackEntry.arguments?.getString("name").orEmpty()
                val number = backStackEntry.arguments?.getInt("number") ?: 0

                PullRequestDetailScreen(
                    pr = viewModel.currentPr,
                    onBack = { navController.popBackStack() },
                    onRetry = { viewModel.loadPrDetail(owner, name, number) },
                    onMerge = { viewModel.mergePr(owner, name, number) },
                    isMerging = viewModel.isMerging,
                    mergeError = viewModel.mergeError,
                    onViewFiles = { navController.navigate(Destinations.prFiles(owner, name, number)) }
                )
            }
            composable(
                route = Destinations.PR_FILES,
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType },
                    navArgument("number") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                val name = backStackEntry.arguments?.getString("name").orEmpty()
                val number = backStackEntry.arguments?.getInt("number") ?: 0

                LaunchedEffect(owner, name, number) { viewModel.loadPrFiles(owner, name, number) }

                PrFilesScreen(
                    prNumber = number,
                    files = viewModel.prFiles,
                    onBack = { navController.popBackStack() },
                    onRetry = { viewModel.loadPrFiles(owner, name, number) }
                )
            }
            composable(
                route = Destinations.REPO_FILES,
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType },
                    navArgument("path") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                val name = backStackEntry.arguments?.getString("name").orEmpty()
                val path = backStackEntry.arguments?.getString("path").orEmpty()

                LaunchedEffect(owner, name, path) { viewModel.loadRepoContents(owner, name, path) }

                RepoFileBrowserScreen(
                    repoFullName = "$owner/$name",
                    currentPath = path,
                    contents = viewModel.repoBrowser,
                    onBack = { navController.popBackStack() },
                    onRetry = { viewModel.loadRepoContents(owner, name, path) },
                    onOpenFolder = { newPath -> navController.navigate(Destinations.repoFiles(owner, name, newPath)) },
                    onOpenFile = { entry -> navController.navigate(Destinations.fileViewer(owner, name, entry.path)) }
                )
            }
            composable(
                route = Destinations.FILE_VIEWER,
                arguments = listOf(
                    navArgument("owner") { type = NavType.StringType },
                    navArgument("name") { type = NavType.StringType },
                    navArgument("path") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                val name = backStackEntry.arguments?.getString("name").orEmpty()
                val path = backStackEntry.arguments?.getString("path").orEmpty()

                LaunchedEffect(owner, name, path) { viewModel.loadFileContent(owner, name, path) }

                FileViewerScreen(
                    file = viewModel.fileContent,
                    onBack = { navController.popBackStack() },
                    onRetry = { viewModel.loadFileContent(owner, name, path) },
                    onOpenInBrowser = { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                )
            }
            composable(
                route = Destinations.CREATE_ISSUE,
                arguments = listOf(navArgument("owner") { type = NavType.StringType }, navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                val name = backStackEntry.arguments?.getString("name").orEmpty()

                CreateIssueScreen(
                    repoFullName = "$owner/$name",
                    isSubmitting = viewModel.isCreatingIssue,
                    errorMessage = viewModel.createIssueError,
                    onBack = { navController.popBackStack() },
                    onSubmit = { title, body ->
                        viewModel.createIssue(owner, name, title, body) { issue ->
                            navController.popBackStack()
                            viewModel.loadRepoDetail(owner, name)
                            viewModel.loadIssueDetail(owner, name, issue.number)
                            navController.navigate(Destinations.issueDetail(owner, name, issue.number))
                        }
                    }
                )
            }
            composable(
                route = Destinations.CREATE_PR,
                arguments = listOf(navArgument("owner") { type = NavType.StringType }, navArgument("name") { type = NavType.StringType })
            ) { backStackEntry ->
                val owner = backStackEntry.arguments?.getString("owner").orEmpty()
                val name = backStackEntry.arguments?.getString("name").orEmpty()

                LaunchedEffect(owner, name) { viewModel.loadBranches(owner, name) }

                CreatePrScreen(
                    repoFullName = "$owner/$name",
                    branches = viewModel.branches,
                    isSubmitting = viewModel.isCreatingPr,
                    errorMessage = viewModel.createPrError,
                    onBack = { navController.popBackStack() },
                    onSubmit = { title, body, head, base, draft ->
                        viewModel.createPullRequest(owner, name, title, body, head, base, draft) { pr ->
                            navController.popBackStack()
                            viewModel.loadRepoDetail(owner, name)
                            viewModel.loadPrDetail(owner, name, pr.number)
                            navController.navigate(Destinations.prDetail(owner, name, pr.number))
                        }
                    }
                )
            }
        }
    }
}
