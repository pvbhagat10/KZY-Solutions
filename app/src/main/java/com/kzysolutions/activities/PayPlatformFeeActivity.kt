package com.kzysolutions.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.PaymentSession
import com.kzysolutions.utils.PlatformFee
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PayPlatformFeeActivity : ComponentActivity() {
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
                PayPlatformFeeScreen(platformFeeStatus)
            }
        }
    }
}

@Composable
fun PayPlatformFeeScreen(platformFeeStatus: String) {
    val context = LocalContext.current
    val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val database = FirebaseDatabase.getInstance().reference

    var superAdminUpiId by remember { mutableStateOf("") }
    var platformFeeAmount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = result.data?.getStringExtra("response") ?: "No response"
        Log.d("UPI_Response", "ResultCode=${result.resultCode}, Response=$response")

        if (result.resultCode == Activity.RESULT_OK || result.resultCode == 11) {
            val now = System.currentTimeMillis()
            val formattedDate = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(now))

            database.child("Users").child(userUid).apply {
                child("platformFeeStatus").setValue("paid")
                child("lastPlatformFeePaidAt").setValue(now)
            }

            Toast.makeText(context, "Platform fee paid successfully", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Payment cancelled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        try {
            val upiSnap = database.child("superadminUpi").get().await()
            superAdminUpiId = upiSnap.getValue(String::class.java) ?: ""

            val feeSnap = database.child("platformfee").get().await()
            platformFeeAmount = feeSnap.getValue(Int::class.java) ?: 0
        } catch (e: Exception) {
            Log.e("PayPlatformFeeScreen", "Failed to fetch fee/upi", e)
        } finally {
            isLoading = false
        }
    }

    TopBar("Pay Platform Fee") {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (platformFeeStatus == "unpaid") {
                BannerAdView()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Platform Fee Amount: ₹$platformFeeAmount",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "To be paid by: You (Admin)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Log.i("superadminUpi", superAdminUpiId)
                Log.i("superadminUpi platformfee", platformFeeAmount.toString())

                WrapButtonWithBackground(
                    toDoFunction = {
                        PaymentSession.store(
                            flatId = "PlatformFee",
                            userId = userUid,
                            amount = platformFeeAmount,
                            buildingId = "NA",
                            flatDetails = null
                        )

                        val uri = "upi://pay".toUri().buildUpon()
                            .appendQueryParameter("pa", superAdminUpiId)
                            .appendQueryParameter("pn", "KZY Platform Fee")
                            .appendQueryParameter("tn", "Admin Platform Fee Payment")
                            .appendQueryParameter("am", platformFeeAmount.toString())
                            .appendQueryParameter("cu", "INR")
                            .build()

                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        val chooser = Intent.createChooser(intent, "Pay with UPI")
                        launcher.launch(chooser)
                    },
                    label = "Pay Platform Fee"
                )

                if (platformFeeStatus == "unpaid") {
                    Spacer(modifier = Modifier.height(16.dp))
                    BannerAdView()
                }
            }
        }
    }
}