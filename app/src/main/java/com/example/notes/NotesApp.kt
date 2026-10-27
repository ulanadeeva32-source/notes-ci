package com.example.notes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Точка входа для Hilt: создаёт граф зависимостей приложения. */
@HiltAndroidApp
class NotesApp : Application()
