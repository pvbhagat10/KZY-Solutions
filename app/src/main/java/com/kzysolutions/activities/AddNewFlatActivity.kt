package com.kzysolutions.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.BuildingDetails
import com.kzysolutions.utils.PlatformFee

class AddNewFlatActivity : ComponentActivity() {
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

        enableEdgeToEdge()
        setContent {
            KZYSolutionsTheme {
                val context = LocalContext.current
                var buildingList by remember { mutableStateOf(listOf<ItemData>()) }
                var searchText by remember { mutableStateOf("") }

                LaunchedEffect(true) {
                    val dbRef = FirebaseDatabase.getInstance().getReference("Buildings")
                    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val items = snapshot.children.mapNotNull {
                                val data = it.getValue(BuildingDetails::class.java)
                                data?.let { b ->
                                    ItemData(
                                        imageUrl = b.imageUrl,
                                        title = b.building,
                                        subtitle = b.address,
                                        id = it.key ?: "",
                                        uid = b.adminUID,
                                        flatNumber = "",
                                        wingNumber = "",
                                        flatId = ""
                                    )
                                }
                            }
                            buildingList = items
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Failed to load buildings", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                TopBar(heading = "Select Building") {

                    if (platformFeeStatus == "unpaid"){
                        BannerAdView()
                    }

                    TextField(
                        label = "Search Building",
                        textValue = searchText,
                        onValueChange = { searchText = it }
                    )

                    val filteredList = buildingList.filter {
                        it.title.contains(searchText, ignoreCase = true)
                    }

                    ItemsList2(items = filteredList) { selectedItem ->
                        val intent = Intent(context, AddNewFlatDetailsActivity::class.java).apply {
                            putExtra("buildingId", selectedItem.id)
                            putExtra("buildingName", selectedItem.title)
                            putExtra("buildingAddress", selectedItem.subtitle)
                            putExtra("Building Admin uid", selectedItem.uid)
                        }
                        context.startActivity(intent)
                    }

                    if (platformFeeStatus == "unpaid"){
                        BannerAdView()
                    }
                }
            }
        }
    }
}