package com.example.androidproject

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) { // Get updated InstanceID token.
        Log.d(TAG, "Refreshed token: $token")
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        val msgBody = remoteMessage.notification?.body
        Log.d(TAG, "Message Notification Body: $msgBody")
    }
    companion object {
        const val TAG = "MyFirebaseMessaging"
    }
}