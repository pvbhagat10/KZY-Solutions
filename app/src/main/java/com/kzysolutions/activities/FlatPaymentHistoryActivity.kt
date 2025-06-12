package com.kzysolutions.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.PlatformFee
import kotlinx.coroutines.tasks.await

class FlatPaymentHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var platformFeeStatus by remember { mutableStateOf("loading") }

            LaunchedEffect(Unit) {
                PlatformFee.getPlatformFeeStatus { status ->
                    platformFeeStatus = status
                }
            }

            KZYSolutionsTheme {
                FlatPaymentHistoryScreen(platformFeeStatus)
            }
        }
    }
}

@Composable
fun FlatPaymentHistoryScreen(platformFeeStatus: String) {
    val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = LocalContext.current
    val paymentHistoryList = remember { mutableStateListOf<PaymentHistoryItem>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userUid) {
        try {
            val paymentsRef = FirebaseDatabase.getInstance().getReference("Payments")
            val snapshot = paymentsRef.get().await()
            paymentHistoryList.clear()

            for (flatEntry in snapshot.children) {
                val flatId = flatEntry.key ?: continue
                for (payment in flatEntry.children) {
                    val userId = payment.child("userUid").getValue(String::class.java)
                    if (userId == userUid) {
                        val amount = payment.child("amount").getValue(Int::class.java) ?: 0
                        val formattedDate = payment.child("formattedDate").getValue(String::class.java) ?: ""
                        val monthsPaid = payment.child("monthsPaid").getValue(Int::class.java) ?: 1

                        paymentHistoryList.add(
                            PaymentHistoryItem(
                                flatId = flatId,
                                amount = amount,
                                formattedDate = formattedDate,
                                monthsPaid = monthsPaid
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to fetch payments", Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    TopBar("Flat Payment History") {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (platformFeeStatus == "unpaid") {
                BannerAdView()
            }

            if (paymentHistoryList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No payments found", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(paymentHistoryList.sortedByDescending { it.formattedDate }) { payment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Flat ID: ${payment.flatId}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Amount Paid: ₹${payment.amount}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Months Paid: ${payment.monthsPaid}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Paid On: ${payment.formattedDate}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                if (platformFeeStatus == "unpaid") {
                    BannerAdView()
                }
            }
        }
    }
}

data class PaymentHistoryItem(
    val flatId: String,
    val amount: Int,
    val formattedDate: String,
    val monthsPaid: Int
)