# Hub — a native GitHub client for Android

A Kotlin + Jetpack Compose Android app that talks to GitHub's REST API using
your own personal access token. Material 3 UI, rounded cards throughout,
dark mode support, and plain-English explainer cards on the Issues and
Pull Request screens for anyone who isn't already fluent in GitHub jargon.

## What it does

- **Sign in with a token** — paste a GitHub personal access token; it's stored
  with `EncryptedSharedPreferences` (backed by the Android Keystore), never
  in plain text.
- **Repos** — lists your repositories, searchable, with stars/forks/language.
- **Issues** — list + detail, with comments and the ability to post a new
  comment. An explainer card describes what an issue actually is.
- **Pull Requests** — list + detail, showing diff stats (files/commits/
  additions/deletions), branch info, and a merge button. Same plain-English
  treatment for what a PR is and what merging does.
- **Notifications** — your GitHub notification inbox.
- **Profile** — your account stats and sign-out.

## What it deliberately doesn't do (yet)

This is a strong, working foundation — not full parity with github.com.
Realistic next additions, roughly in order of value:
- Inline diff/file viewer for PRs (currently shows stats, not the diff itself)
- Creating new issues/PRs from the app
- Push notifications (currently pull-to-refresh / manual reload)
- Code search, Actions/CI status, repo file browser
- OAuth App flow (for if you ever want to publish this rather than use it
  yourself with a personal token)

## Setup

1. Open this folder in Android Studio (Koala or newer recommended).
2. Let Gradle sync — it will pull dependencies from Google, Maven Central,
   and JitPack (used for one markdown-rendering library).
3. Run on a device or emulator running API 26+.
4. On first launch, tap **"Create a token on GitHub"** — this opens GitHub's
   token creation page pre-filled with the scopes the app needs
   (`repo`, `notifications`, `read:user`). Generate it, copy it, paste it
   into the app.

## Architecture, briefly

- `data/` — Retrofit API interface, models matching GitHub's JSON, the
  encrypted token store, and the OkHttp client that injects the
  `Authorization` header per request.
- `ui/screens/` — one file per screen, all stateless Composables driven by
  a simple `LoadState<T>` (Loading/Success/Error) sealed class.
- `AppViewModel.kt` — the single source of truth; owns all network calls
  and exposes state via Compose `mutableStateOf`.
- `MainActivity.kt` — wires up the `NavHost`, bottom navigation, and
  the login gate.

## A note on the token

A classic PAT with `repo` scope can read and write to every repo you have
access to. For anything beyond your own experimentation, use a
**fine-grained personal access token** scoped to specific repositories —
GitHub's token creation page lets you choose this.
