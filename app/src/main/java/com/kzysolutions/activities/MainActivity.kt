package com.kzysolutions.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.kzysolutions.R
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.BuildingDetailsSC
import com.kzysolutions.utils.FlatDetails
import com.kzysolutions.utils.PlatformFee

class MainActivity : ComponentActivity() {
    var platformFeeStatus = "unpaid"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("In Main", "MainActivity")

        MobileAds.initialize(this) { initializationStatus ->
            Log.d("AdMob", "AdMob Initialized")
        }

        val testDeviceIds = listOf("C196008631B68B13B13A523F57B49701")
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        )

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                saveFCMToken()
            }
        }

        checkIfPlatformFeeDue()
        PlatformFee.getPlatformFeeStatus { status ->
            platformFeeStatus = if (status == "unpaid") {
                "unpaid"
            }else {
                "paid"
            }
        }

        val userID = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        val phoneFromIntent = intent.getStringExtra("phone") ?: ""
        val logoutIntent = intent.getStringExtra("Log out") ?: ""

        if (logoutIntent != "") {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, PhoneLogin::class.java))
        }

        enableEdgeToEdge()
        setContent {
            KZYSolutionsTheme {
                MainScreen(userID, phoneFromIntent, this, platformFeeStatus)
            }
        }
    }

    fun checkIfPlatformFeeDue() {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid)

        userRef.child("lastPlatformFeePaidAt").get().addOnSuccessListener {
            val lastPaid = it.value?.toString()?.toLongOrNull() ?: 0
            val now = System.currentTimeMillis()
            val oneMonthInMillis = 30L * 24 * 60 * 60 * 1000

            if (now - lastPaid > oneMonthInMillis) {
                userRef.child("platformFeeStatus").setValue("unpaid")
                platformFeeStatus = "unpaid"
            } else {
                platformFeeStatus = "paid"
            }
        }
    }


    private fun saveFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val userId =
                    FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                FirebaseDatabase.getInstance().getReference("FCMTokens").child(userId)
                    .setValue(token)
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Failed to get FCM token", e)
            }

    }
}

@Composable
fun MainScreen(
    userID: String,
    phoneFromIntent: String,
    activity: ComponentActivity,
    platformFeeStatus: String
) {
    var role by remember { mutableStateOf<String?>(null) }
    var redirectTo by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(userID)
        val flatsRef = FirebaseDatabase.getInstance().getReference("Flats")
        val buildingsRef = FirebaseDatabase.getInstance().getReference("Buildings")

        userRef.keepSynced(true)
        flatsRef.keepSynced(true)
        buildingsRef.keepSynced(true)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val fetchedRole = snapshot.child("role").getValue(String::class.java)
                    role = fetchedRole
                } else {
                    redirectTo = if (phoneFromIntent.isBlank()) "PhoneLogin" else "CreateNewUser"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    when (redirectTo) {
        "PhoneLogin" -> {
            LaunchedEffect(Unit) {
                Firebase.auth.signOut()
                context.startActivity(Intent(context, PhoneLogin::class.java))
                activity.finish()
            }
        }

        "CreateNewUser" -> {
            LaunchedEffect(Unit) {
                val intent = Intent(context, CreateNewUserActivity::class.java)
                intent.putExtra("phone", phoneFromIntent)
                context.startActivity(intent)
                activity.finish()
            }
        }

        null -> {
            if (role == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (role) {
                    "Admin" -> UI({ BuildingsUI(platformFeeStatus) }, platformFeeStatus)
                    "Flat User" -> UI({}, platformFeeStatus)
                    else -> Text("Unsupported role: $role")
                }
            }
        }
    }
}

@Composable
fun UI(buildingsUI: @Composable () -> Unit, platformFeeStatus: String) {
    val context = LocalContext.current

    TopBarForLazyColumnsLogOut(stringResource(id = R.string.app_name)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (platformFeeStatus == "unpaid") {
                BannerAdView(platformFeeStatus)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UnderlineButton({
                        val intent = Intent(context, PayPlatformFeeActivity::class.java)
                        context.startActivity(intent)
                    }, "Remove Ads")
                }
            }

            buildingsUI()

            Spacer(modifier = Modifier.height(24.dp))

            val textColor = MaterialTheme.colorScheme.onSurface // or any color you prefer

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Flats",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 24.sp,
                    color = textColor
                )

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        context.startActivity(Intent(context, AddNewFlatActivity::class.java))
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New Flat",
                        modifier = Modifier.size(20.dp),
                        tint = textColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add New Flat",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 24.sp,
                        textDecoration = TextDecoration.Underline,
                        color = textColor
                    )
                }
            }

            OutlinedCard(
                border = BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    FlatsPanel()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            BannerAdView(platformFeeStatus)
        }
    }
}

@Composable
fun BuildingsUI(platformFeeStatus: String) {
    val context = LocalContext.current

    val textColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Buildings",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 24.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = {
                context.startActivity(Intent(context, NewBuildingDetailsActivity::class.java))
            },
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add New Building",
                modifier = Modifier.size(16.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Add New Building",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp,
                textDecoration = TextDecoration.Underline,
                color = textColor
            )
        }
    }

    OutlinedCard(
        border = BorderStroke(1.dp, Color.Gray),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            BuildingsPanel()
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    BannerAdView(platformFeeStatus)
}

@Composable
fun BuildingsPanel() {
    val context = LocalContext.current
    val adminUID = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var buildingList by remember { mutableStateOf<List<ItemData>>(emptyList()) }

    LaunchedEffect(Unit) {
        val buildingsRef = FirebaseDatabase.getInstance().getReference("Buildings")
        buildingsRef.keepSynced(true)
        buildingsRef.orderByChild("adminUID").equalTo(adminUID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tempList = mutableListOf<ItemData>()
                    for (buildingSnapshot in snapshot.children) {
                        val buildingId = buildingSnapshot.key ?: continue
                        val imageUrl =
                            buildingSnapshot.child("imageUrl").getValue(String::class.java)
                                .orEmpty()
                        val title = buildingSnapshot.child("building").getValue(String::class.java)
                            .orEmpty()
                        val subtitle =
                            buildingSnapshot.child("address").getValue(String::class.java).orEmpty()

                        val item = ItemData(
                            imageUrl = imageUrl,
                            title = title,
                            subtitle = subtitle,
                            id = buildingId,
                            uid = adminUID,
                            flatNumber = "",
                            wingNumber = "",
                            flatId = ""
                        )
                        tempList.add(item)
                    }
                    buildingList = tempList
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        buildingList.forEach { item ->
            ItemRow(
                item = item,
                onClick = {
                    val intent = Intent(context, FlatUsers::class.java)
                    intent.putExtra("buildingId", item.id)
                    context.startActivity(intent)
                },
                buttonActions = listOf(
                    "Payments" to { item ->
                        val intent = Intent(context, AllFlatUsersPaymentsActivity::class.java)
                        intent.putExtra("buildingId", item.id)
                        context.startActivity(intent)
                    },
//                    "Flat Users" to { item ->
//                    },
                    "Approve" to { item ->
                        val intent = Intent(context, FlatApprovalActivity::class.java)
                        intent.putExtra("buildingId", item.id)
                        context.startActivity(intent)
                    }
//                    "Notify" to { /** Notify */ }
                )
            )
        }
    }
}

@Composable
fun FlatsPanel() {
    val context = LocalContext.current
    val itemList = remember { mutableStateListOf<ItemData>() }
    val userUid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(Unit) {
        if (userUid != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid)
            val flatsRef = FirebaseDatabase.getInstance().getReference("Flats")
            val buildingsRef = FirebaseDatabase.getInstance().getReference("Buildings")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val flatIds =
                        snapshot.child("flatIds").children.mapNotNull { it.getValue(String::class.java) }

                    flatIds.forEach { flatId ->
                        flatsRef.child(flatId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(flatSnapshot: DataSnapshot) {
                                    val flatData = flatSnapshot.getValue(FlatDetails::class.java)
                                    val buildingId = flatSnapshot.child("buildingId")
                                        .getValue(String::class.java)

                                    if (flatData != null && !buildingId.isNullOrEmpty()) {
                                        buildingsRef.child(buildingId)
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(buildingSnapshot: DataSnapshot) {
                                                    val building =
                                                        buildingSnapshot.getValue(BuildingDetailsSC::class.java)
                                                    if (building != null) {
                                                        val flatName =
                                                            "Flat no. ${flatData.flatNumber}, Wing no. ${flatData.wingNumber}"
                                                        val item = ItemData(
                                                            imageUrl = building.imageUrl,
                                                            title = building.building,
                                                            subtitle = flatName,
                                                            id = buildingSnapshot.key ?: "",
                                                            uid = building.adminUID,
                                                            flatNumber = flatData.flatNumber,
                                                            wingNumber = flatData.wingNumber,
                                                            flatId = flatId
                                                        )
                                                        itemList.add(item)
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Building not found",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Toast.makeText(
                                                        context,
                                                        "Building load failed",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            })
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Flat/building data invalid",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(context, "Flat load failed", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "User data load failed", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        itemList.forEach { item ->
            ItemRow(
                item = item,
                onClick = {
                    val intent = Intent(context, FlatDetailsActivity::class.java)
                    intent.putExtra("flatNumber", item.flatNumber)
                    intent.putExtra("wingNumber", item.wingNumber)
                    intent.putExtra("buildingName", item.title)
                    intent.putExtra("buildingAddress", item.subtitle)
                    intent.putExtra("imageUrl", item.imageUrl)
                    context.startActivity(intent)
                },
                buttonActions = listOf(
                    "Pay" to { item ->
                        val intent = Intent(context, PaymentsActivity::class.java)
                        intent.putExtra("flatId", item.flatId)
                        context.startActivity(intent)
                    },
                    "History" to { item ->
                        val intent = Intent(context, FlatPaymentHistoryActivity::class.java)
                        context.startActivity(intent)
                    }
                )
            )
        }
    }
}