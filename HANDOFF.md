# FamChat — текущ статус (Handoff)

Последно обновено след: аватарите показват емоджи+цвят вместо генерична иконка/плосък кръг.

## Статус: Sprint 1, 2, 3 завършени и потвърдени работещи на реално устройство

### Sprint 1 — Auth
- ✅ Регистрация (никнейм + парола + избор на аватар) — работи с реален Firebase
- ✅ Login — работи
- ✅ Logout — преместен в header-а на груповия чат (иконка горе вдясно)

### Sprint 2 — Групов realtime чат
- ✅ Потвърден работещ с ДВА отделни тестови акаунта — съобщения се виждат realtime и от двете страни
- ✅ Firestore composite index капан фиксиран (сортиране client-side вместо `orderBy` в заявката)

### Sprint 3 — Личен чат + визуален redesign
- ✅ Личен (1-на-1) чат между двама потребители, deterministic chatId
- ✅ Странична вертикална колона (`MemberRail`) в груповия чат — показва всички регистрирани потребители, тап отваря директно личен чат с тях
- ✅ Chat bubbles: розово за лични чатове, синьо за групов (дизайн система)
- ✅ Икона на приложението (синьо чат мехурче, adaptive icon)
- ✅ LoginScreen redesign: бяла картичка с рамка, заоблено лого, филирани полета (не outline+floating label анимация — премахнато по изричен избор на потребителя, "лагаво")
- ✅ След login/register — директно се зарежда груповият чат, не списък с чатове
- ✅ Navigation transition анимации изключени (потребителят намери default slide/fade анимациите за лагави/бъгави)
- ✅ Аватари: реални VectorDrawable вместо счупени `<shape>` XML-и (fix за crash при login)
- ✅ Аватари показват емоджи+цвят (🐶🐱🦊🐼🐻🐰) вместо плосък цветен кръг или генерична 👤 икона — визуалната идентичност идва от `avatarOptions` списъка в `AvatarGallery.kt`, преизползвана през нов `AvatarCircle(resId, size)` composable

## Одобрени чрез мокъп UI решения (НЕ предлагай отново без причина)
Потребителят одобри следните конкретни решения чрез интерактивни мокъпи преди имплементация:
1. Колоната с членове е **вертикална, отстрани** (не хоризонтален ред отгоре) — потвърдено изрично след първоначален грешен опит с хоризонтален ред
2. Тап на кръгче в колоната отваря **директно** личен чат, без междинно меню (по-опростено от първоначалния мокъп с "Лично съобщение"/"Виж профил" бутони)
3. Груповият чат е **винаги активен/стартова точка**, не отделен избираем чат от списък
4. Логин екран: бяла картичка + синьо мехурче лого + филирани полета без floating label — без анимация при фокус

## Известни отворени точки / следващи стъпки
- **Профилен екран** — не съществува още (мокъпа показва: смяна на аватар, смяна на парола, notifications toggle). Обсъдено, но не приоритизирано.
- **ChatListScreen** — вече не е start destination, route-ът технически още съществува в `MainActivity.kt` navigation graph, но е неизползван в текущия потребителски поток. Не е премахнат от кода.
- **Изтриване на чат** ("Изтрий за мен"/"Изтрий за всички" от мокъпа) — не имплементирано.
- Изпращане на снимки (`Message.imageUrl` в модела, няма upload логика)
- Push notifications (`User.fcmToken` в модела, празен стринг, няма FCM интеграция)
- Firestore security rules — непроверено дали са зададени (вероятно test mode/отворени)
- `Divider` deprecated warning-и в 3 файла (ChatListScreen, ChatScreen, NewChatScreen) — козметично, не блокира build

## Файлова структура
app/src/main/java/com/example/famchat/
├── MainActivity.kt (NavHost, FAMILY_GROUP_ID/NAME константи, start destination логика)
├── FamChatApp.kt
├── data/FirebaseAuthManager.kt
├── model/Chat.kt, Message.kt (с senderAvatarResId), User.kt
├── navigation/Screen.kt (login, register, chat_list, new_chat, chat/{chatId}/{chatName})
├── ui/components/AvatarGallery.kt (avatarOptions списък + AvatarGallery + AvatarCircle composables)
├── ui/screens/LoginScreen.kt, RegisterScreen.kt, ChatListScreen.kt, ChatScreen.kt, NewChatScreen.kt
└── ui/theme/Color.kt, Theme.kt, Type.kt
app/src/main/res/
├── values/, drawable/ (avatar_*.xml като VectorDrawable кръгове, ic_launcher_foreground/background.xml)
└── mipmap-anydpi-v26/ (adaptive icon)

Виж `AGENTS.md` за инфраструктурни инструкции и капани, `README.md` за общ преглед.
