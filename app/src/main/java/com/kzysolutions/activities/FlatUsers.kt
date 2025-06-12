package com.kzysolutions.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.functions.YourAuthTokenProvider
import com.kzysolutions.utils.UserFlatDetails
import kotlinx.coroutines.launch
import org.json.JSONObject

class FlatUsers : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val buildingId = intent.getStringExtra("buildingId") ?: ""

        enableEdgeToEdge()
        setContent {
            KZYSolutionsTheme {
                ShowFlatUsers(buildingId)
            }
        }
    }
}

@Composable
fun ShowFlatUsers(buildingId: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var userList by remember { mutableStateOf<List<UserFlatDetails>>(emptyList()) }
    var platformFeeStatus by remember { mutableStateOf("loading") }

    LaunchedEffect(buildingId) {
        val flatRef = FirebaseDatabase.getInstance().getReference("Flats")
        flatRef.orderByChild("buildingId").equalTo(buildingId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = mutableListOf<UserFlatDetails>()
                    for (flatSnapshot in snapshot.children) {
                        val flatNumber = flatSnapshot.child("flatNumber").getValue(String::class.java) ?: ""
                        val wingNumber = flatSnapshot.child("wingNumber").getValue(String::class.java) ?: ""
                        val usersMap = flatSnapshot.child("users").value as? Map<String, Boolean> ?: emptyMap()

                        for ((userId, _) in usersMap) {
                            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val name = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                                    val phone = userSnapshot.child("phone").getValue(String::class.java) ?: "N/A"

                                    val userDetails = UserFlatDetails(
                                        name = name,
                                        phone = phone,
                                        flatNumber = flatNumber,
                                        wingNumber = wingNumber
                                    )
                                    users.add(userDetails)
                                    userList = users.toList()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(context, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch flats", Toast.LENGTH_SHORT).show()
                }
            })
    }

    TopBar(heading = "Flat Users") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {

            if (platformFeeStatus == "unpaid"){
                BannerAdView()
            }

            WrapButtonWithBackground(
                toDoFunction = {

                    coroutineScope.launch {
                        val adminUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                        val flatsRef = FirebaseDatabase.getInstance().getReference("Flats")
                        val fcmRef = FirebaseDatabase.getInstance().getReference("FCMTokens")

                        flatsRef.get().addOnSuccessListener { snapshot ->
                            var totalNotifications = 0
                            for (flatSnap in snapshot.children) {
                                val flatAdmin = flatSnap.child("adminUid").getValue(String::class.java) ?: continue
                                if (flatAdmin != adminUid) continue // Only send for this admin

                                val netDue = flatSnap.child("netDue").getValue(Int::class.java) ?: 0
                                if (netDue <= 0) continue

                                val usersNode = flatSnap.child("users")
                                val flatUsers = usersNode.children.mapNotNull { it.key }

                                for (userUid in flatUsers) {
                                    fcmRef.child(userUid).get().addOnSuccessListener { tokenSnap ->
                                        val token = tokenSnap.getValue(String::class.java)
                                        if (!token.isNullOrEmpty()) {
                                            sendNotificationToToken(
                                                context,
                                                token,
                                                "Maintenance Payment Pending",
                                                "Your payment of ₹$netDue is pending. Please pay now."
                                            )
                                            totalNotifications++
                                        }
                                    }
                                }
                            }

                            Toast.makeText(
                                context,
                                "Notifications sent for all pending payments.",
                                Toast.LENGTH_LONG
                            ).show()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to fetch flats", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                label = "Send Notification"
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(userList) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Name: ${user.name}")
                        Text("Phone: ${user.phone}")
                        Text("Flat No: ${user.flatNumber}")
                        Text("Wing: ${user.wingNumber}")
                    }
                }
            }
        }

        if (platformFeeStatus == "unpaid"){
            BannerAdView()
        }
    }

}

fun sendNotificationToToken(context: Context, token: String, title: String, body: String) {
    val json = JSONObject()
    val notification = JSONObject()
    val message = JSONObject()
    val androidConfig = JSONObject()

    notification.put("title", title)
    notification.put("body", body)
    androidConfig.put("priority", "high")

    message.put("token", token)
    message.put("notification", notification)
    message.put("android", androidConfig)
    json.put("message", message)

    val request = object : StringRequest(
        Method.POST,
        "https://fcm.googleapis.com/v1/projects/KZY-Building-mgmt/messages:send",
        Response.Listener { },
        Response.ErrorListener { error -> Log.e("FCM", "Error sending: $error") }
    ) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] = "Bearer ya29.c.c0ASRK0GYesS4TrRnHrRq-4pjRS03cHW7hNwxR0riOeLM__JLbQl8Sok8pEoCMi8qoWjM2XhozufhFrdRBj1fAzFNE3NRQK0KGG0hXt5GjT6bS4joMlBu00cIxzJy6k4R5-pGKEem4l1WrMOliW8O8weCXbUfHyYiYcYV5jY0OWRwnA5rYU4P-joszLzH_lp0uPzHtKy88g3JuUl9JQh6a0Di6hikjSYdDSK3LDC0E_xu2nHfMY-5wgEY7BKP-n-z8aiaQjj86vGEOGcf_HUGM2R46ET2q-i2q-DrfqsBkLFJ_qiI_wxM7iawZNylqWEWAJ153aHH2cFYArXe6HgP9yHo4-5gI5QSvbgVTCaW7cK47JKoYCFL_KGNyH385Dim88mXZodc4qohc9az15-5MWcqqg4_d9UF-8oJoYjROUtV18w9FgMUqikrU3afdtpj8yl8qwx0MMOmIFw3rhxvwY6iZy47MsRzn474jzR8_MXqrV5WrfMjiiWvYakOm3Z1Xumona0g-hcSWIRztJdF88m-u2-l5uSj57IzkpIYSRzdBsqwb1221iY6ujrxJIbFJQdR0IU2t9OkxatBcZhnq9yW2hF1BUi5M8cg8yqWy6-kel1qWn1rt_kwJBFeMsueyq8JV2Ulkih-O2oMBod8vYw_tkalMYX41jlpeg42Rc5tyzWbsJxbt0zaa37u2njRF9Yy_OrSRywMui65Odht5WZ_zly4pq-QXaVaay-zmdJxhJtFQioyX1XJ32mf25BVWhxZcBcik74QO9ogyl2lSFkbccjSXQ-zJdbzjyrnw6Mir8QVidqMneoBVcghF_Jd-Yqd0J00OV0caB_dmOQZyMe_yB55_xS33_tu6fmY7-ph1RZMUSMgx1Md7VX8VdMBl0ogn9fe_-m45kBQbeW5bb3_3o0s7R7OgW77Jl3BXIYVsBx1haM_s71vZIvBJkVzSYueh889SR8caBS94M2qM6S41UFl6-83nstg4Sp856y74fnd0h041Rhq"
            headers["Content-Type"] = "application/json"
            return headers
        }

        override fun getBody(): ByteArray = json.toString().toByteArray(Charsets.UTF_8)
    }

    Volley.newRequestQueue(context).add(request)
}