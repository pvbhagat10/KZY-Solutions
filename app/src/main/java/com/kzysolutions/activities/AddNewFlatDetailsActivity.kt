package com.kzysolutions.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.PlatformFee

class AddNewFlatDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var platformFeeStatus = "unpaid"

        PlatformFee.getPlatformFeeStatus { status ->
            platformFeeStatus = if (status == "unpaid") {
                "unpaid"
            }else {
                "paid"
            }
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        enableEdgeToEdge()
        setContent {
            KZYSolutionsTheme {
                val context = LocalContext.current
                val buildingId = intent.getStringExtra("buildingId") ?: ""
                val buildingName = intent.getStringExtra("buildingName") ?: ""
                val buildingAddress = intent.getStringExtra("buildingAddress") ?: ""
                val adminUid = intent.getStringExtra("Building Admin uid") ?: ""

                var adminName by remember { mutableStateOf<String?>(null) }
                var flatNumber by remember { mutableStateOf("") }
                var wingNumber by remember { mutableStateOf("") }

                LaunchedEffect(adminUid) {
                    val adminRef = FirebaseDatabase.getInstance().reference.child("Users").child(adminUid)
                    adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                adminName = snapshot.child("name").getValue(String::class.java)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                TopBar(heading = "Add New Flat") {

                    if (platformFeeStatus == "unpaid"){
                        BannerAdView()
                    }

                    Text(text = "Admin: ${adminName ?: "Loading..."}")

                    TextField(
                        label = "Building Name",
                        textValue = buildingName,
                        onValueChange = {},
                        enabled = false
                    )
                    TextField(
                        label = "Address",
                        textValue = buildingAddress,
                        onValueChange = {},
                        enabled = false
                    )
                    TextField(
                        label = "Flat Number",
                        textValue = flatNumber,
                        onValueChange = { flatNumber = it }
                    )
                    TextField(
                        label = "Wing Number",
                        textValue = wingNumber,
                        onValueChange = { wingNumber = it }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    WrapButtonWithBackground(
                        toDoFunction = {
                            if (flatNumber.isNotEmpty() && wingNumber.isNotEmpty()) {
                                val dbRef = FirebaseDatabase.getInstance().reference
                                val flatId = "${buildingId}_${wingNumber.trim().uppercase()}_${flatNumber.trim()}"
                                val flatRef = dbRef.child("Flats").child(flatId)

                                flatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val joinRef = dbRef.child("FlatJoinRequests").child(buildingId).child(flatId)
                                            val joinRequest = mapOf(
                                                "requestedBy" to uid,
                                                "flatNumber" to flatNumber,
                                                "wingNumber" to wingNumber,
                                                "timestamp" to ServerValue.TIMESTAMP
                                            )

                                            joinRef.setValue(joinRequest).addOnSuccessListener {
                                                Toast.makeText(context, "Join request sent to admin", Toast.LENGTH_SHORT).show()
                                                finish()
                                            }.addOnFailureListener {
                                                Toast.makeText(context, "Failed to send join request", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            val flatDetails = mapOf(
                                                "buildingId" to buildingId,
                                                "flatNumber" to flatNumber,
                                                "wingNumber" to wingNumber,
                                                "admin" to adminName,
                                                "adminUid" to adminUid,
                                                "pendingUsers" to mapOf(uid to true),
                                                "netDue" to 0
                                            )

                                            flatRef.setValue(flatDetails).addOnSuccessListener {
                                                val joinRef = dbRef.child("FlatJoinRequests").child(buildingId).child(flatId)
                                                val joinRequest = mapOf(
                                                    "requestedBy" to uid,
                                                    "flatNumber" to flatNumber,
                                                    "wingNumber" to wingNumber,
                                                    "timestamp" to ServerValue.TIMESTAMP
                                                )

                                                joinRef.setValue(joinRequest).addOnSuccessListener {
                                                    Toast.makeText(context, "Flat created and request sent to admin", Toast.LENGTH_SHORT).show()
                                                    finish()
                                                }
                                            }.addOnFailureListener {
                                                Toast.makeText(context, "Failed to create flat", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            } else {
                                Toast.makeText(context, "Enter all details", Toast.LENGTH_SHORT).show()
                            }
                        },
                        label = "Save"
                    )

                    if (platformFeeStatus == "unpaid"){
                        BannerAdView()
                    }
                }
            }
        }
    }
}
