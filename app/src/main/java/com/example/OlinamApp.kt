package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class OlinamApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:1007973994144:android:b3d230c8ae84ddd7d6c3c5")
                    .setApiKey("AIzaSyBXoDNyLKGJFpS6Q2T4dvC6BrpjLVdx8yY")
                    .setProjectId("olinam-90d42")
                    .setStorageBucket("olinam-90d42.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("OlinamApp", "FirebaseApp initialized with project olinam-90d42")
            } else {
                Log.d("OlinamApp", "FirebaseApp initialized by Provider")
            }
        } catch (t: Throwable) {
            Log.e("OlinamApp", "FirebaseApp initialization exception: ${t.message}", t)
        }
    }
}
