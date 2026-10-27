# Notes — МДК 01.03, практические работы №8 и №9

Приложение «Заметки» + модуль авторизации (Auth Feature).
Архитектура: MVVM + UDF (StateFlow), Clean Architecture (presentation / domain / data), Hilt.

## Структура
```
com.example.notes
├── NotesApp.kt                          @HiltAndroidApp
├── domain/                              чистый Kotlin, без Android SDK
│   ├── model/                           AuthUser, AuthException
│   ├── repository/AuthRepository        контракт
│   ├── usecase/                         AuthUseCase, AddNoteUseCase
│   └── validation/EmailValidator
├── data/
│   ├── api/                             AuthApi (Retrofit), MockAuthInterceptor
│   ├── dto/                             LoginRequestDto, LoginResponseDto
│   ├── repository/AuthRepositoryImpl    ошибки сети -> доменные ошибки
│   └── local/                           Room: Note, NoteDao (Flow), NoteDatabase
├── repository/NoteRepository            заметки (Flow)
├── di/                                  AppModule (Room), NetworkModule (Retrofit), RepositoryModule (@Binds)
└── ui/
    ├── auth/                            AuthActivity, AuthScreen (Compose), AuthViewModel, AuthUiState
    ├── util/UiText                      текст ошибки без Context во ViewModel
    ├── MainActivity                     список заметок (XML), собирает StateFlow
    └── viewmodel/                       NoteViewModel, NotesUiState
```

## ПР 9
- Часть 2: `NotesUiState` + `NoteViewModel` на `StateFlow` (вместо LiveData).
- Часть 3: `AuthUseCase`, `AddNoteUseCase` в слое domain.
- Часть 4: `AuthRepositoryImpl` (Data), интерфейс `AuthRepository` (Domain).
- Часть 5, блок 1: экран входа на Compose, кнопка активна только при валидном email и непустом пароле
  (`isLoginEnabled` = `uiState.map {}`), при запросе кнопка блокируется и показывается индикатор.
- Блок 2: `AuthUiState.error: UiText?`, ошибка показывается через `SnackbarHost` (Material3).
- Блок 3: Hilt — `NetworkModule` (Retrofit), `AppModule` (Room), `@HiltViewModel`, `@Binds` для репозитория.
- Unit-тест: `app/src/test/.../AuthUseCaseTest.kt` (подделка репозитория, без эмулятора).

## Пользователи и профиль
- Регистрация и вход на одном экране (переключатель внизу). Аккаунты хранит заглушка сервера на телефоне.
- `SessionStorage` помнит, кто вошёл; при следующем запуске приложение сразу открывает заметки.
- У заметки есть `ownerEmail`; `NoteRepository` сохраняет и показывает только заметки текущего пользователя
  (Room: версия 2 + миграция 1→2). Новый пользователь видит пустой список.
- В Toolbar под названием виден email; иконка профиля открывает диалог с кнопкой «Выйти».

## Демо-вход (сервер имитирует MockAuthInterceptor)
- `user@example.com` / `123456` — успешный вход
- `offline@example.com` — ошибка сети
- любые другие данные — «Неверный email или пароль»
- свой аккаунт: «Нет аккаунта? Зарегистрироваться» (пароль не короче 6 символов)

## Запуск
File → Open → папка проекта → Gradle Sync → Run ▶ (минимум Android 8.0, API 26).
