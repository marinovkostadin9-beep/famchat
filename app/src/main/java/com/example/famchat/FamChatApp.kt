package com.example.famchat

import android.app.Application
import com.google.firebase.FirebaseApp

class FamChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
