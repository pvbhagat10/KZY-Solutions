package com.kzysolutions.activities

/*class PaymentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val flatId = intent.getStringExtra("flatId") ?: ""
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (flatId.isEmpty()) {
            Toast.makeText(this, "Invalid flat ID", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Step 1: Fetch flat data
        val flatRef = FirebaseDatabase.getInstance().getReference("Flats").child(flatId)
        flatRef.get().addOnSuccessListener { flatSnap ->
            val flatNumber = flatSnap.child("flatNumber").getValue(String::class.java) ?: "N/A"
            val wingNumber = flatSnap.child("wingNumber").getValue(String::class.java) ?: "N/A"
            val netDue = flatSnap.child("netDue").getValue(Int::class.java) ?: 0
            val buildingId = flatSnap.child("buildingId").getValue(String::class.java) ?: ""

            Log.d("PaymentsActivity", "Flat loaded: $flatNumber, Wing: $wingNumber, NetDue: $netDue, BuildingId: $buildingId")

            // Step 2: Fetch building UPI ID
            val buildingRef = FirebaseDatabase.getInstance().getReference("Buildings").child(buildingId)
            buildingRef.get().addOnSuccessListener { buildingSnap ->
                val upiId = buildingSnap.child("upi").getValue(String::class.java) ?: ""

                Log.d("PaymentsActivity", "Building loaded, UPI: $upiId")

                enableEdgeToEdge()
                setContent {
                    KZYSolutionsTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            PaymentScreen(
                                flatId = flatId,
                                buildingId = buildingId,
                                flatNumber = flatNumber,
                                wingNumber = wingNumber,
                                netDue = netDue,
                                userUid = userUid,
                                upiId = upiId
                            )
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("PaymentsActivity", "Failed to load building details", e)
                Toast.makeText(this, "Error loading building details", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { e ->
            Log.e("PaymentsActivity", "Failed to load flat details", e)
            Toast.makeText(this, "Error loading flat details", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun PaymentScreen(
    flatId: String,
    buildingId: String,
    flatNumber: String,
    wingNumber: String,
    netDue: Int,
    userUid: String,
    upiId: String
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val session = PaymentSession.restore()
        val response = result.data?.getStringExtra("response") ?: "No response"
        Log.d("UPI_Response", "ResultCode=${result.resultCode}, Response=$response")

        if (result.resultCode == Activity.RESULT_OK || result.resultCode == 11) {
            val db = FirebaseDatabase.getInstance()
            val now = System.currentTimeMillis()
            val formattedDate =
                SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(now))

            val monthsPaid =
                if (session.amount != 0) (session.flatDetails?.netDue ?: 0) / session.amount else 1

            val paymentData = mapOf(
                "userUid" to session.userId,
                "timestamp" to now,
                "formattedDate" to formattedDate,
                "amount" to session.amount,
                "monthsPaid" to monthsPaid
            )

            db.getReference("Payments").child(session.flatId).push().setValue(paymentData)
            db.getReference("Flats").child(session.flatId).child("netDue").setValue(0)

            Toast.makeText(context, "Payment successful & saved", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Payment cancelled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch building details inside composable
    var buildingName by remember { mutableStateOf("") }
    var buildingAddress by remember { mutableStateOf("") }

    LaunchedEffect(buildingId) {
        val buildingRef = FirebaseDatabase.getInstance().getReference("Buildings").child(buildingId)
        buildingRef.get().addOnSuccessListener { snapshot ->
            buildingName = snapshot.child("building").getValue(String::class.java) ?: ""
            buildingAddress = snapshot.child("address").getValue(String::class.java) ?: ""
            Log.d("PaymentScreen", "Building Name: $buildingName, Address: $buildingAddress")
        }.addOnFailureListener {
            Log.e("PaymentScreen", "Failed to fetch building info", it)
        }
    }

    val monthlyRate = if (netDue > 0) netDue else 1
    val monthsDue = if (monthlyRate != 0) netDue / monthlyRate else 1

    TopBar(heading = "Make Payment") { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Building: $buildingName", style = MaterialTheme.typography.titleLarge)
                    Text("Address: $buildingAddress", style = MaterialTheme.typography.bodyMedium)
                    Text("Flat No: $flatNumber", style = MaterialTheme.typography.bodyMedium)
                    Text("Wing: $wingNumber", style = MaterialTheme.typography.bodyMedium)
                    Text("Net Due: ₹$netDue", style = MaterialTheme.typography.bodyMedium)
                    Text("Total Months Due: $monthsDue", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            WrapButtonWithBackground(
                toDoFunction = {
                    PaymentSession.store(
                        flatId = flatId,
                        userId = userUid,
                        amount = netDue,
                        buildingId = buildingId,
                        flatDetails = FlatDetailsPayments(flatNumber, wingNumber, netDue)
                    )

                    val uri = "upi://pay".toUri().buildUpon()
                        .appendQueryParameter("pa", upiId)
                        .appendQueryParameter("pn", "Flat $flatNumber")
                        .appendQueryParameter("tn", "Maintenance Payment")
                        .appendQueryParameter("am", netDue.toString())
                        .appendQueryParameter("cu", "INR")
                        .build()

                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    val chooser = Intent.createChooser(intent, "Pay with UPI")
                    launcher.launch(chooser)
                },
                label = "Pay Now"
            )
        }
    }
}*/


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.FlatDetailsPayments
import com.kzysolutions.utils.PaymentSession
import com.kzysolutions.utils.PlatformFee
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentsActivity : ComponentActivity() {
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

        val flatId = intent.getStringExtra("flatId") ?: ""
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (flatId.isEmpty()) {
            Toast.makeText(this, "Invalid flat ID", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val flatRef = FirebaseDatabase.getInstance().getReference("Flats").child(flatId)
        flatRef.get().addOnSuccessListener { flatSnap ->
            val flatNumber = flatSnap.child("flatNumber").getValue(String::class.java) ?: "N/A"
            val wingNumber = flatSnap.child("wingNumber").getValue(String::class.java) ?: "N/A"
            val netDue = flatSnap.child("netDue").getValue(Int::class.java) ?: 0
            val buildingId = flatSnap.child("buildingId").getValue(String::class.java) ?: ""

            val buildingRef = FirebaseDatabase.getInstance().getReference("Buildings").child(buildingId)
            buildingRef.get().addOnSuccessListener { buildingSnap ->
                val upiId = buildingSnap.child("upi").getValue(String::class.java) ?: ""

                setContent {
                    KZYSolutionsTheme {
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            PaymentScreen(
                                flatId = flatId,
                                buildingId = buildingId,
                                flatNumber = flatNumber,
                                wingNumber = wingNumber,
                                netDue = netDue,
                                userUid = userUid,
                                upiId = upiId,
                                platformFeeStatus
                            )
                        }
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error loading building details", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error loading flat details", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun PaymentScreen(
    flatId: String,
    buildingId: String,
    flatNumber: String,
    wingNumber: String,
    netDue: Int,
    userUid: String,
    upiId: String,
    platformFeeStatus: String
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val session = PaymentSession.restore()
        val response = result.data?.getStringExtra("response") ?: "No response"

        if (result.resultCode == Activity.RESULT_OK || result.resultCode == 11) {
            val db = FirebaseDatabase.getInstance()
            val now = System.currentTimeMillis()
            val formattedDate = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date(now))
            val monthsPaid = if (session.amount != 0) (session.flatDetails?.netDue ?: 0) / session.amount else 1

            val paymentData = mapOf(
                "userUid" to session.userId,
                "timestamp" to now,
                "formattedDate" to formattedDate,
                "amount" to session.amount,
                "monthsPaid" to monthsPaid
            )

            db.getReference("Payments").child(session.flatId).push().setValue(paymentData)
            db.getReference("Flats").child(session.flatId).child("netDue").setValue(0)

            Toast.makeText(context, "Payment successful & saved", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Payment cancelled or failed", Toast.LENGTH_SHORT).show()
        }
    }

    var buildingName by remember { mutableStateOf("") }
    var buildingAddress by remember { mutableStateOf("") }

    LaunchedEffect(buildingId) {

        val buildingRef = FirebaseDatabase.getInstance().getReference("Buildings").child(buildingId)
        buildingRef.get().addOnSuccessListener { snapshot ->
            buildingName = snapshot.child("building").getValue(String::class.java) ?: ""
            buildingAddress = snapshot.child("address").getValue(String::class.java) ?: ""
        }
    }

    val monthlyRate = if (netDue > 0) netDue else 1
    val monthsDue = if (monthlyRate != 0) netDue / monthlyRate else 1

    TopBar(heading = "Make Payment") { paddingValues ->

        if (platformFeeStatus == "unpaid"){
            BannerAdView()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Building: $buildingName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Address: $buildingAddress")
                    Text("Flat No: $flatNumber")
                    Text("Wing: $wingNumber")
                    Text("Net Due: ₹$netDue")
                    Text("Total Months Due: $monthsDue")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            WrapButtonWithBackground(
                toDoFunction = {
                    PaymentSession.store(
                        flatId = flatId,
                        userId = userUid,
                        amount = netDue,
                        buildingId = buildingId,
                        flatDetails = FlatDetailsPayments(flatNumber, wingNumber, netDue)
                    )
                    val uri = "upi://pay".toUri().buildUpon()
                        .appendQueryParameter("pa", upiId)
                        .appendQueryParameter("pn", "Flat $flatNumber")
                        .appendQueryParameter("tn", "Maintenance Payment")
                        .appendQueryParameter("am", netDue.toString())
                        .appendQueryParameter("cu", "INR")
                        .build()

                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    val chooser = Intent.createChooser(intent, "Pay with UPI")
                    launcher.launch(chooser)
                },
                label = "Pay Now"
            )

            if (platformFeeStatus == "unpaid"){
                BannerAdView()
            }
        }
    }
}
