package com.kzysolutions.functions

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import java.io.InputStream

object YourAuthTokenProvider {
    private var token: String? = null
    private var tokenExpiry: Long = 0

    fun getAccessToken(context: Context): String {
        val now = System.currentTimeMillis()
        if (token != null && now < tokenExpiry) return token!!

        val credentialsStream: InputStream = context.assets.open("fcm_service_account.json")
        val credentials = GoogleCredentials.fromStream(credentialsStream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        credentials.refreshIfExpired()

        token = credentials.accessToken.tokenValue
        tokenExpiry = credentials.accessToken.expirationTime.time - 60000 // 1 minute buffer

        return token!!
    }
}
