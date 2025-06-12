package com.kzysolutions.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object PlatformFee {

    fun getPlatformFeeStatus(callback: (String) -> Unit) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid)

        userRef.child("platformFeeStatus").get().addOnSuccessListener {
            val status = it.getValue(String::class.java) ?: "unpaid"
            callback(status)
        }.addOnFailureListener {
            callback("unpaid") // default to unpaid if error
        }
    }
}
