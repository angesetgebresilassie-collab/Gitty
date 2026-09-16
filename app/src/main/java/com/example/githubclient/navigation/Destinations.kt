package com.example.githubclient.navigation

object Destinations {
    const val LOGIN = "login"
    const val REPOS = "repos"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val REPO_DETAIL = "repo/{owner}/{name}"
    const val ISSUE_DETAIL = "repo/{owner}/{name}/issue/{number}"
    const val PR_DETAIL = "repo/{owner}/{name}/pr/{number}"

    fun repoDetail(owner: String, name: String) = "repo/$owner/$name"
    fun issueDetail(owner: String, name: String, number: Int) = "repo/$owner/$name/issue/$number"
    fun prDetail(owner: String, name: String, number: Int) = "repo/$owner/$name/pr/$number"
}
