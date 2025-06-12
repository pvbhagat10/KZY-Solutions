package com.kzysolutions.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kzysolutions.activities.ui.theme.Blue2
import com.kzysolutions.activities.ui.theme.White

data class BuildingItemWithActions(
    val id: String,
    val imageUrl: String,
    val title: String,
    val subtitle: String,
    val uid: String
)

/*
class MyBuildingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val buildings = remember { mutableStateOf<List<BuildingItemWithActions>>(emptyList()) }
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            LaunchedEffect(uid) {
                if (uid.isNotEmpty()) {
                    val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
                    val buildingsRef = FirebaseDatabase.getInstance().getReference("Buildings")

                    usersRef.child("buildingIds")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val buildingIds = snapshot.children.mapNotNull {
                                    it.getValue(String::class.java)
                                }

                                buildingsRef.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(buildingSnapshot: DataSnapshot) {
                                        val list = mutableListOf<BuildingItemWithActions>()

                                        for (buildingId in buildingIds) {
                                            val buildingNode = buildingSnapshot.child(buildingId)
                                            if (buildingNode.exists()) {
                                                val imageUrl = buildingNode.child("imageUrl")
                                                    .getValue(String::class.java) ?: ""
                                                val title = buildingNode.child("building")
                                                    .getValue(String::class.java) ?: "No Name"
                                                val subtitle = buildingNode.child("address")
                                                    .getValue(String::class.java) ?: "No Address"
                                                list.add(
                                                    BuildingItemWithActions(
                                                        id = buildingId,
                                                        imageUrl = imageUrl,
                                                        title = title,
                                                        subtitle = subtitle,
                                                        uid = uid
                                                    )
                                                )
                                            }
                                        }
                                        buildings.value = list
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(
                                            this@MyBuildingsActivity,
                                            "Error loading buildings",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@MyBuildingsActivity,
                                    "Error loading user data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }

            TopBar("My Buildings") { padding ->
                if (buildings.value.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No buildings found.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(padding)
                    ) {
                        items(buildings.value) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = item.imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(item.title, fontWeight = FontWeight.Bold)
                                            Text(item.subtitle, color = Color.Gray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    ActionButtonsRow { label ->
                                        Toast.makeText(
                                            context,
                                            "Clicked $label - ${item.id}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        val intent = when (label) {
                                            "Payments" -> Intent(context, PaymentsActivity::class.java)
                                            "Users" -> Intent(context, FlatUsers::class.java)
                                            "Notifications" -> Intent(context, MainActivity::class.java)
                                            "Approval" -> Intent(context, MainActivity::class.java)
                                            else -> null
                                        }

                                        intent?.apply {
                                            putExtra("buildingId", item.id)
                                            putExtra("action", label)
                                            context.startActivity(this)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtonsRow(onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton("Payments", onClick, Modifier.weight(1f))
            ActionButton("Users", onClick, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton("Notifications", onClick, Modifier.weight(1f))
            ActionButton("Approval", onClick, Modifier.weight(1f))
        }
    }
}

@Composable
fun ActionButton(label: String, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = { onClick(label) },
        modifier = modifier
            .height(48.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = Blue2)
    ) {
        Text(label, color = White, fontWeight = FontWeight.Bold)
    }
}*/

/*
class MyBuildingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val userBuildings = remember { mutableStateOf<List<ItemData>>(emptyList()) }
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            LaunchedEffect(uid) {
                if (uid.isNotEmpty()) {
                    val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
                    val buildingsRef = FirebaseDatabase.getInstance().getReference("Buildings")

                    usersRef.child("buildingIds")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val buildingIds = snapshot.children.mapNotNull {
                                    it.getValue(String::class.java)
                                }

                                buildingsRef.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(buildingSnapshot: DataSnapshot) {
                                        val buildingsList = mutableListOf<ItemData>()

                                        for (buildingId in buildingIds) {
                                            val buildingNode = buildingSnapshot.child(buildingId)
                                            if (buildingNode.exists()) {
                                                val imageUrl =
                                                    buildingNode.child("imageUrl")
                                                        .getValue(String::class.java) ?: ""
                                                val title =
                                                    buildingNode.child("building")
                                                        .getValue(String::class.java)
                                                        ?: "No Name"
                                                val subtitle =
                                                    buildingNode.child("address")
                                                        .getValue(String::class.java)
                                                        ?: "No Address"
                                                buildingsList.add(
                                                    ItemData(
                                                        imageUrl = imageUrl,
                                                        title = title,
                                                        subtitle = subtitle,
                                                        id = buildingId,
                                                        uid = uid,
                                                        flatNumber = "",
                                                        wingNumber = "",
                                                        flatId = ""
                                                    )
                                                )
                                            }
                                        }

                                        Log.i("buildingsList", buildingsList.toString())
                                        userBuildings.value = buildingsList
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(
                                            this@MyBuildingsActivity,
                                            "Error loading buildings",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@MyBuildingsActivity,
                                    "Error loading user data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }

//            TopBarForLazyColumns(heading = "My Buildings") { innerPadding ->
//                if (userBuildings.value.isEmpty()) {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(innerPadding)
//                            .padding(top = 24.dp), // extra spacing just in case
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("No buildings found.", color = Color.Gray)
//                    }
//                } else {
//                    ItemsList2(
//                        items = userBuildings.value,
//                        modifier = Modifier.padding(innerPadding),
//                        onClick = { item ->
//                            val intent = Intent(this@MyBuildingsActivity,
//                                FlatUsers::class.java)
//                            intent.putExtra("buildingId", item.id)
//                            startActivity(intent)
//                        }
//                    )
//                }
//            }
        }
    }
}*/
