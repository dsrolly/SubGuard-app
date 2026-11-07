<p align="center">
  <img src="app/ic_subguard_logo.png" alt="SubGuard logo" width="120">
</p>

# SubGuard

Smart recurring subscription and renewal tracker for Android, built with Kotlin and Jetpack Compose.

SubGuard helps you keep track of all your subscriptions in one place, see where your money goes, and never miss a renewal date.

## Features

- Track recurring subscriptions with categories and tags
- Dashboard and spending analytics
- Calendar view of upcoming renewals
- Renewal reminders through notifications
- Multiple currencies with a built-in currency converter
- Backup and CSV export
- Appearance settings with light and dark themes
- Guest mode, with your data migrated when you create an account
- Sign-in with email verification (Firebase Authentication)

## Tech stack

- Kotlin
- Jetpack Compose and Material 3
- MVVM architecture (ViewModels and repositories)
- Firebase Authentication and Cloud Firestore
- Gradle Kotlin DSL with a version catalog

## Download

- Latest APK: see the [subguard-releases](https://github.com/dsrolly/subguard-releases) repository
- Web app: https://subguard-265d6.web.app

## Run the project locally

The Firebase configuration file is not included in this repository, so you need to connect your own Firebase project:

1. Clone this repository and open it in Android Studio.
2. Create a project in the [Firebase console](https://console.firebase.google.com/).
3. Add an Android app with the package name `com.davinder.subguard`.
4. Enable Authentication and Cloud Firestore for the project.
5. Download `google-services.json` and place it in the `app/` folder. The file `app/google-services.json.example` shows the expected structure.
6. Sync Gradle and run the app.

`google-services.json` is listed in `.gitignore`, so it will not be committed by accident.

## Author

Built by Rolly ([@dsrolly](https://github.com/dsrolly)) as part of my MCA studies at Punjabi University, Patiala.
LinkedIn: https://www.linkedin.com/in/dsrolly
