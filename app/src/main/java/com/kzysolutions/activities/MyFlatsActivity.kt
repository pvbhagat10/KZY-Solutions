package com.kzysolutions.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.BuildingDetailsSC
import com.kzysolutions.utils.FlatDetails
//
//class MyFlatsActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        enableEdgeToEdge()
//        setContent {
//            KZYSolutionsTheme {
//                MyFlatsScreen("MyFlatsActivity")
//            }
//        }
//    }
//}
//
//@Composable
//fun MyFlatsScreen(screenType: String) {
//    val context = LocalContext.current
//    val itemList = remember { mutableStateListOf<ItemData>() }
//    val userUid = FirebaseAuth.getInstance().currentUser?.uid
//
//    LaunchedEffect(Unit) {
//        if (userUid != null) {
//            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid)
//            val flatsRef = FirebaseDatabase.getInstance().getReference("Flats")
//            val buildingsRef = FirebaseDatabase.getInstance().getReference("Buildings")
//
//            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val flatIds = snapshot.child("flatIds").children.mapNotNull { it.getValue(String::class.java) }
//
//                    flatIds.forEach { flatId ->
//                        flatsRef.child(flatId).addListenerForSingleValueEvent(object : ValueEventListener {
//                            override fun onDataChange(flatSnapshot: DataSnapshot) {
//                                val flatData = flatSnapshot.getValue(FlatDetails::class.java)
//                                val buildingId = flatSnapshot.child("buildingId").getValue(String::class.java)
//
//                                if (flatData != null && !buildingId.isNullOrEmpty()) {
//                                    buildingsRef.child(buildingId)
//                                        .addListenerForSingleValueEvent(object : ValueEventListener {
//                                            override fun onDataChange(buildingSnapshot: DataSnapshot) {
//                                                val building = buildingSnapshot.getValue(BuildingDetailsSC::class.java)
//                                                if (building != null) {
//                                                    val flatName = "Flat no." + flatData.flatNumber + ", Wing no." + flatData.wingNumber
//                                                    val item = ItemData(
//                                                        imageUrl = building.imageUrl,
//                                                        title = building.building,
//                                                        subtitle = flatName,
//                                                        id = buildingSnapshot.key ?: "",
//                                                        uid = building.adminUID,
//                                                        flatNumber = flatData.flatNumber,
//                                                        wingNumber = flatData.wingNumber,
//                                                        flatId = flatId // ✅ Pass the flatId here
//                                                    )
//                                                    itemList.add(item)
//                                                }
//                                            }
//
//                                            override fun onCancelled(error: DatabaseError) {
//                                                Toast.makeText(context, "Building load failed", Toast.LENGTH_SHORT).show()
//                                            }
//                                        })
//                                }
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                Toast.makeText(context, "Flat load failed", Toast.LENGTH_SHORT).show()
//                            }
//                        })
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(context, "User data load failed", Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
//    }
//
//    TopBarForLazyColumns(heading = screenType) {
//        if (itemList.isEmpty()) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text("No flats found", style = MaterialTheme.typography.bodyLarge)
//            }
//        } else {
//            ItemsList(items = itemList) { item ->
//                when (screenType) {
//                    "MyFlatsActivity" -> {
//                        val intent = Intent(context, FlatDetailsActivity::class.java).apply {
//                            putExtra("buildingId", item.id)
//                            putExtra("buildingName", item.title)
//                            putExtra("buildingAddress", item.subtitle)
//                            putExtra("flatNumber", item.flatNumber)
//                            putExtra("wingNumber", item.wingNumber)
//                            putExtra("imageUrl", item.imageUrl)
//                        }
//                        context.startActivity(intent)
//                    }
//
//                    "Pay Maintenance" -> {
//                        val intent = Intent(context, PaymentsActivity::class.java).apply {
//                            putExtra("buildingId", item.id)
//                            putExtra("buildingName", item.title)
//                            putExtra("flatId", item.flatId)
//                            putExtra("flatNumber", item.flatNumber)
//                            putExtra("wingNumber", item.wingNumber)
//                        }
//                        context.startActivity(intent)
//                    }
//
//                    else -> {
//                        Toast.makeText(context, "Unknown destination", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//    }
//}