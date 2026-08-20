# FamChat

Android чат приложение за семейство (родители + дете) с реален realtime чат през Firebase.

## Технологии
- Kotlin, Jetpack Compose, Navigation Compose
- Firebase Auth + Firestore (realtime)
- Gradle 8.2, Kotlin plugin 1.9.20, Java 21

## Основни функции
- Регистрация/вход по никнейм (не email), с избор на аватар (животинче + цвят)
- Постоянно активен групов "Семеен чат" с колона членове отстрани
- Тап на член от колоната отваря личен (1-на-1) чат с него
- Realtime съобщения, различен цвят балончета за групов (синьо) и личен (розово) чат

## Build & run
```bash
cd /workspaces/famchat
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Виж `AGENTS.md` за инструкции при работа с AI агент и `HANDOFF.md` за текущ статус на проекта.
