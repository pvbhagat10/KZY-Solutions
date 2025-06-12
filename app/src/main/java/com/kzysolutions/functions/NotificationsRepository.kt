package com.kzysolutions.functions

import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object NotificationsRepo {
    private val functions = Firebase.functions

    suspend fun sendPaymentReminder(title: String, body: String): Pair<Int, Int> {
        val result = functions
            .getHttpsCallable("notifyPendingPayments")
            .call(mapOf("title" to title, "body" to body))
            .await()
        val data = result.data as Map<*, *>
        return Pair((data["successCount"] as Number).toInt(), (data["failureCount"] as Number).toInt())
    }
}
