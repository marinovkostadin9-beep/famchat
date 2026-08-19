# FamChat Project Overview

FamChat is a single-module Android family messaging app built with Kotlin and Jetpack Compose. Firebase Authentication and Cloud Firestore provide account management, user profiles, chats, and real-time messages.

## Main Features

- Nickname/password registration and login using synthetic Firebase email addresses.
- A hard-coded family group chat (`family_group`).
- Private chat model with deterministic IDs made from two sorted user IDs.
- Avatar selection and avatar rendering in the member rail and message bubbles.
- Online status and last-seen metadata.
- Real-time Firestore listeners for messages, members, and chat lists.

## Structure

- `app/src/main/java/com/example/famchat/MainActivity.kt`: application entry point and navigation host.
- `app/src/main/java/com/example/famchat/FamChatApp.kt`: Firebase initialization.
- `app/src/main/java/com/example/famchat/navigation/`: navigation route definitions.
- `app/src/main/java/com/example/famchat/ui/screens/`: Compose screens and UI behavior.
- `app/src/main/java/com/example/famchat/data/FirebaseAuthManager.kt`: authentication and user document operations.
- `app/src/main/java/com/example/famchat/model/`: Firestore data models.
- `app/src/main/java/com/example/famchat/ui/theme/`: Compose colors, typography, and theme.
- `app/src/main/res/drawable/`: avatar assets and other resources.

## Firestore Shape

```text
users/{userId}
chats/{chatId}
chats/{chatId}/messages/{messageId}
```

Messages store sender metadata, including the sender avatar resource ID. User and chat access is currently implemented directly from Compose screens; there are no repositories, ViewModels, local database, or Firebase security-rules files in the repository.

## Build And Checks

The project uses Gradle 8.2, Android Gradle Plugin 8.2.0, Kotlin 1.9.20, Java 17, compile/target SDK 34, and minimum SDK 24.

```bash
./gradlew :app:assembleDebug
./gradlew test
./gradlew lint
```

There is currently no project-authored `test` or `androidTest` source set.

## Important Notes

- Most UI copy is Bulgarian and is currently hard-coded in Kotlin.
- `app/google-services.json` is local configuration and should not be added to commits; use `google-services.json.placeholder` as a template.
- Firebase security rules are not part of this repository and must be reviewed separately before production use.
- Keep changes focused and preserve the existing Compose and Firebase patterns unless the task specifically calls for architectural work.
