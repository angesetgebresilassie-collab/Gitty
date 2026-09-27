package com.example.githubclient.navigation

import android.net.Uri

object Destinations {
    const val LOGIN = "login"
    const val REPOS = "repos"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val REPO_DETAIL = "repo/{owner}/{name}"
    const val ISSUE_DETAIL = "repo/{owner}/{name}/issue/{number}"
    const val PR_DETAIL = "repo/{owner}/{name}/pr/{number}"
    const val PR_FILES = "repo/{owner}/{name}/pr/{number}/files"
    const val REPO_FILES = "repo/{owner}/{name}/files?path={path}"
    const val FILE_VIEWER = "repo/{owner}/{name}/file?path={path}"
    const val CREATE_ISSUE = "repo/{owner}/{name}/issue/new"
    const val CREATE_PR = "repo/{owner}/{name}/pr/new"

    fun repoDetail(owner: String, name: String) = "repo/$owner/$name"
    fun issueDetail(owner: String, name: String, number: Int) = "repo/$owner/$name/issue/$number"
    fun prDetail(owner: String, name: String, number: Int) = "repo/$owner/$name/pr/$number"
    fun prFiles(owner: String, name: String, number: Int) = "repo/$owner/$name/pr/$number/files"
    fun repoFiles(owner: String, name: String, path: String = "") = "repo/$owner/$name/files?path=${Uri.encode(path)}"
    fun fileViewer(owner: String, name: String, path: String) = "repo/$owner/$name/file?path=${Uri.encode(path)}"
    fun createIssue(owner: String, name: String) = "repo/$owner/$name/issue/new"
    fun createPr(owner: String, name: String) = "repo/$owner/$name/pr/new"
}
