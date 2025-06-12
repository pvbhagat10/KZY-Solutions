package com.kzysolutions.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.PlatformFee

class FlatApprovalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val buildingId = intent.getStringExtra("buildingId") ?: ""
        Log.i("buildingId", buildingId)

        enableEdgeToEdge()
        setContent {
            KZYSolutionsTheme {
                FlatApprovalScreen(buildingId = buildingId)
            }
        }
    }
}

@Composable
fun FlatApprovalScreen(buildingId: String) {
    val context = LocalContext.current
    val dbRef = FirebaseDatabase.getInstance().reference
    var approvalList by remember { mutableStateOf<List<FlatApprovalData>>(emptyList()) }
    var platformFeeStatus by remember { mutableStateOf("loading") }

    LaunchedEffect(buildingId) {

        PlatformFee.getPlatformFeeStatus { status ->
            platformFeeStatus = status
        }
        val joinRequestsRef = dbRef.child("FlatJoinRequests").child(buildingId)

        joinRequestsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = mutableListOf<FlatApprovalData>()
                val flatRequests = snapshot.children.toList()

                flatRequests.forEach { flatSnap ->
                    val flatId = flatSnap.key ?: return@forEach
                    val flatRequest =
                        flatSnap.getValue(FlatJoinRequest::class.java) ?: return@forEach

                    val userId = flatRequest.requestedBy ?: return@forEach

                    dbRef.child("Users").child(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val name = userSnapshot.child("name").getValue(String::class.java)
                                    ?: "Unknown"
                                val phone = userSnapshot.child("phone").getValue(String::class.java)
                                    ?: "Unknown"

                                val approvalData = FlatApprovalData(
                                    flatId = flatId,
                                    userId = userId,
                                    name = name,
                                    phone = phone,
                                    flatNumber = flatRequest.flatNumber,
                                    wingNumber = flatRequest.wingNumber
                                )

                                // Add and sort by timestamp
                                newList.add(approvalData)
                                approvalList =
                                    newList.sortedByDescending { it.flatNumber + it.wingNumber }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, "Failed to load user", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load approvals", Toast.LENGTH_SHORT).show()
            }
        })
    }

    TopBar("Approve Flat Users") {
        if (platformFeeStatus == "unpaid"){
            BannerAdView()
        }
        ItemsList(
            items = approvalList.map {
                ItemData(
                    imageUrl = "",
                    title = "${it.name} (${it.phone})",
                    subtitle = "Flat: ${it.flatNumber}, Wing: ${it.wingNumber}",
                    id = it.flatId,
                    uid = it.userId,
                    flatNumber = it.flatNumber,
                    wingNumber = it.wingNumber,
                    flatId = it.flatId
                )
            },
            onClick = {},
            buttonActions = listOf(
                "Approve" to { item -> approveUser(item, buildingId, context) },
                "Reject" to { item -> rejectUser(item, buildingId, context) }
            )
        )
        if (platformFeeStatus == "unpaid"){
            BannerAdView()
        }
    }
}

@Keep
data class FlatJoinRequest(
    val flatNumber: String = "",
    val requestedBy: String = "",
    val timestamp: Long = 0,
    val wingNumber: String = ""
)

data class FlatApprovalData(
    val flatId: String,
    val userId: String,
    val name: String,
    val phone: String,
    val flatNumber: String,
    val wingNumber: String
)

fun approveUser(item: ItemData, buildingId: String, context: Context) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val flatId = item.flatId
    val userId = item.uid

    dbRef.child("Flats").child(flatId).child("users").child(userId).setValue(true)
        .addOnSuccessListener {

            dbRef.child("Flats").child(flatId).child("pendingUsers").child(userId).removeValue()

            dbRef.child("Users").child(userId).child("flatIds").get()
                .addOnSuccessListener { snapshot ->
                    val existingIds =
                        snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                            ?: emptyList()
                    val updatedList = existingIds.toMutableList().apply { add(flatId) }
                    dbRef.child("Users").child(userId).child("flatIds").setValue(updatedList)
                }

            dbRef.child("FlatJoinRequests").child(buildingId).child(flatId).removeValue()

            Toast.makeText(context, "User approved successfully", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Approval failed", Toast.LENGTH_SHORT).show()
        }
}

fun rejectUser(item: ItemData, buildingId: String, context: Context) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val flatId = item.flatId
    val userId = item.uid

    dbRef.child("Flats").child(flatId).child("pendingUsers").child(userId).removeValue()

    dbRef.child("FlatJoinRequests").child(buildingId).child(flatId).removeValue()
        .addOnSuccessListener {
            Toast.makeText(context, "Request rejected", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Rejection failed", Toast.LENGTH_SHORT).show()
        }
}