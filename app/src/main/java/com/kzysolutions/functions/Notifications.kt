package com.kzysolutions.functions

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

object NotificationSender {
    fun sendNotificationToToken(context: Context, token: String, title: String, body: String) {
        val json = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", token)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                })
                put("android", JSONObject().apply {
                    put("priority", "high")
                })
            })
        }

        val request = object : StringRequest(
            Method.POST,
            "https://fcm.googleapis.com/v1/projects/KZY-Building-mgmt/messages:send",
            Response.Listener { Log.d("FCM", "Success") },
            Response.ErrorListener { error -> Log.e("FCM", "Error: ${error.message}") }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer ${YourAuthTokenProvider.getAccessToken(context)}",
                    "Content-Type" to "application/json"
                )
            }

            override fun getBody(): ByteArray = json.toString().toByteArray(Charsets.UTF_8)
        }

        Volley.newRequestQueue(context).add(request)
    }
}
