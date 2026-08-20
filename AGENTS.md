# Инструкции за AI агент, работещ по FamChat

## Golden rule
**Commit + push веднага след всяка успешна build стъпка.** Некомитнатите промени са се губили при рестарт на Codespace сесия — случвало се е многократно. Никога не се трупа работа некомитната.

## Среда
- GitHub Codespace, path `/workspaces/famchat`
- Repo: `marinovkostadin9-beep/famchat`, branch `main`
- Потребителят комуникира само чрез копиране на команди в Codespace терминала — AI агентът няма директен достъп до средата, дава команди и чете резултата, който потребителят пейства обратно.

## Задължителна настройка при нов/пресен Codespace
`local.properties` и Android SDK НЕ са в git (умишлено, машинно-специфични):
```bash
mkdir -p $HOME/android-sdk/cmdline-tools
cd /tmp && curl -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q cmdline-tools.zip -d $HOME/android-sdk/cmdline-tools
mv $HOME/android-sdk/cmdline-tools/cmdline-tools $HOME/android-sdk/cmdline-tools/latest
export ANDROID_HOME=$HOME/android-sdk
cd $ANDROID_HOME/cmdline-tools/latest/bin
yes | ./sdkmanager --licenses --sdk_root=$ANDROID_HOME
./sdkmanager --sdk_root=$ANDROID_HOME "platform-tools" "platforms;android-34" "build-tools;34.0.0"
echo "sdk.dir=$HOME/android-sdk" > /workspaces/famchat/local.properties
```

## Известни капани
1. **Gradle 8.2 + системна Java 25+ = `HasConvention`/`NoClassDefFoundError`.** `gradle.properties` съдържа `org.gradle.java.home=/usr/local/sdkman/candidates/java/21.0.10-ms` — задължително за съвместимост. Провери реалния path с `ls /usr/local/sdkman/candidates/java/`.
2. **`gradlew` дава синтактична грешка/съдържа "429" текст**: wrapper файловете са се повредили при curl от GitHub (rate limit). Работещо решение: `gradle init --type basic --dsl kotlin` в temp директория + `gradle wrapper --gradle-version 8.2`, после копирай `gradle-wrapper.jar`/`gradlew`/`gradlew.bat` в проекта.
3. **Firestore заявки с `whereArrayContains`/`whereEqualTo` + `orderBy` на друго поле** изискват composite index — без него заявката тихо връща нищо. Сортирай client-side (`.sortedByDescending {}`) вместо `orderBy` в Firestore, освен ако композитен индекс е изрично създаден.
4. **Vector drawables, не shape drawables за аватари.** `<shape>` XML-и предизвикват crash при `painterResource()`/`Image()` в Compose. Реалната визуална идентичност на аватарите не идва от самите drawable файлове (те са само плоски цветни кръгове) — идва от `avatarOptions` списъка в `AvatarGallery.kt` (resId → емоджи + цвят). Използвай `AvatarCircle(resId, size)` composable-а навсякъде за показване на аватар, не directly `Image(painterResource(...))`.
5. **Kotlin daemon connection failure** при build понякога — гейтит fallback compile без daemon, build пак минава успешно, просто по-бавно. Не е блокиращо.

## Data модел (Firestore)
- `users/{uid}`: `userId, nickname, avatarResId (Int), isOnline, lastSeen, createdAt, fcmToken`
- `chats/{chatId}`: `chatId, type ("group"|"private"), name, participants (List<userId>), lastMessage, lastMessageTime, createdBy, createdAt, deletedFor`
- `chats/{chatId}/messages/{messageId}`: `messageId, chatId, senderId, senderName, senderAvatarResId, text, timestamp, type, imageUrl`
- Груповият чат винаги има фиксиран `chatId = "family_group"` (константа `FAMILY_GROUP_ID` в `MainActivity.kt`)
- Личните чатове имат deterministic `chatId = "${по-малкия userId}_${по-големия userId}"` (сортирани лексикографски), за да не се дублират

## Auth
Login/register е по **никнейм**, не email — вътрешно `"$nickname@famchat.local"` за Firebase Auth. Виж `data/FirebaseAuthManager.kt`.

## Навигация
След успешен login/register — директно към груповия чат (`Screen.Chat.createRoute(FAMILY_GROUP_ID, ...)`), не към `ChatListScreen` (тя вече не е start destination, но route-ът все още съществува в кода, засега неизползван). Navigation transition анимациите са изключени (`EnterTransition.None` и т.н. в `NavHost` в `MainActivity.kt`) по изричен избор на потребителя — изглеждаха "лагаво".

## Дизайн система (потвърдена с потребителя чрез мокъпи)
- Primary/групов чат: `#38BDF8` (PrimaryBlue)
- Личен чат: `#F472B6` (PrivatePink)
- Online: `#22C55E`, Offline: сиво
- Заоблени полета/бутони: 14px радиус
- Balончета: моите вдясно (цветни според тип чат), чужди вляво (сиво `ChatBubbleOther`)
- Груповият чат има постоянна тясна вертикална колона (`MemberRail`) отляво с всички регистрирани потребители (не просто "членове на групата" — цялото приложение има фиксирано малък брой потребители, семейство). Тап на друг член отваря директно личен чат с него.

## Преди да пишеш код
Провери `HANDOFF.md` за текущия статус — какво е завършено, какво е в процес, какви UI решения вече са одобрени от потребителя чрез мокъпи (не предлагай отново вече одобрени неща без причина).
