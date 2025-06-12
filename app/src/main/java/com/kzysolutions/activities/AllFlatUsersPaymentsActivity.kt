package com.kzysolutions.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.PlatformFee
import kotlinx.coroutines.tasks.await

class AllFlatUsersPaymentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val buildingId = intent.getStringExtra("buildingId") ?: ""

        enableEdgeToEdge()
        setContent {
            KZYSolutionsTheme {
                FlatUserPaymentsScreen(buildingId = buildingId)
            }
        }
    }
}

data class PaymentEntry(
    val flatNumber: String = "",
    val wingNumber: String = "",
    val amount: Int = 0,
    val formattedDate: String = "",
    val userName: String = ""
)

@Composable
fun FlatUserPaymentsScreen(buildingId: String) {
    var platformFeeStatus by remember { mutableStateOf("loading") }
    val context = LocalContext.current
    val db = FirebaseDatabase.getInstance()
    val paymentList = remember { mutableStateListOf<PaymentEntry>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(buildingId) {
        try {
            PlatformFee.getPlatformFeeStatus { status ->
                platformFeeStatus = status
            }

            val flatsSnap = db.getReference("Flats")
                .orderByChild("buildingId")
                .equalTo(buildingId)
                .get()
                .await()

            for (flatSnap in flatsSnap.children) {
                val flatId = flatSnap.key ?: continue
                val flatNumber = flatSnap.child("flatNumber").getValue(String::class.java) ?: "?"
                val wingNumber = flatSnap.child("wingNumber").getValue(String::class.java) ?: "?"

                val usersMap =
                    flatSnap.child("users").value as? Map<*, *> ?: emptyMap<String, Boolean>()

                val paymentsSnap = db.getReference("Payments").child(flatId).get().await()
                for (paymentSnap in paymentsSnap.children) {
                    val amount = paymentSnap.child("amount").getValue(Int::class.java) ?: 0
                    val formattedDate =
                        paymentSnap.child("formattedDate").getValue(String::class.java) ?: ""
                    val userUid = paymentSnap.child("userUid").getValue(String::class.java) ?: ""

                    val userName = if (usersMap.containsKey(userUid)) {
                        val userSnap = db.getReference("Users").child(userUid).get().await()
                        userSnap.child("name").getValue(String::class.java) ?: "Unknown"
                    } else {
                        "Unknown"
                    }

                    paymentList.add(
                        PaymentEntry(
                            flatNumber = flatNumber,
                            wingNumber = wingNumber,
                            amount = amount,
                            formattedDate = formattedDate,
                            userName = userName
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading payments: ${e.message}", Toast.LENGTH_LONG)
                .show()
        } finally {
            isLoading = false
        }
    }


    TopBar("Flat Users Payments") {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (platformFeeStatus == "unpaid"){
                BannerAdView()
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(paymentList.sortedByDescending { it.formattedDate }) { payment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Flat: ${payment.flatNumber}, Wing: ${payment.wingNumber}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Amount: ₹${payment.amount}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Date: ${payment.formattedDate}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Paid By: ${payment.userName}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (platformFeeStatus == "unpaid"){
                BannerAdView()
            }
        }
    }
}