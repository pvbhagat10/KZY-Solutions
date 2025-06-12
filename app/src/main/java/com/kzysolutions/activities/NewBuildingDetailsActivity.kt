package com.kzysolutions.activities

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.storage.FirebaseStorage
import com.kzysolutions.activities.ui.theme.Blue2
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
/*

class NewBuildingDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            KZYSolutionsTheme {
                NewBuildingDetailsForm()
            }
        }
    }
}

@Composable
fun NewBuildingDetailsForm() {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val adminUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val storageRef = FirebaseStorage.getInstance().reference
    val databaseRef = FirebaseDatabase.getInstance().reference.child("Buildings")
    val usersRef = FirebaseDatabase.getInstance().reference.child("Users")

    var upi by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var maintenance by remember { mutableStateOf("") }

    var billingDay by remember { mutableStateOf("1") }
    val billingDayOptions = (1..28).map { it.toString() }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { selectedImageUri = it } }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = cameraImageUri
            Toast.makeText(context, "Image Captured", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraImageUri = createImageUri(context)
            cameraImageUri?.let { cameraLauncher.launch(it) }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    TopBar(heading = "Building Details") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Apartment,
                contentDescription = "Profile Icon",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable {
                        showImagePickerDialog(
                            context,
                            onGallery = { galleryLauncher.launch("image/*") },
                            onCamera = {
                                val permission = Manifest.permission.CAMERA
                                if (ContextCompat.checkSelfPermission(context, permission)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    cameraImageUri = createImageUri(context)
                                    cameraImageUri?.let { cameraLauncher.launch(it) }
                                } else {
                                    permissionLauncher.launch(permission)
                                }
                            }
                        )
                    },
                tint = Blue2
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(label = "Building Name", textValue = building, onValueChange = { building = it })
            TextField(label = "Address", textValue = address, onValueChange = { address = it })
            TextField(label = "City", textValue = city, onValueChange = { city = it })
            TextField(label = "Pincode", textValue = pincode, onValueChange = { pincode = it }, textType = "phone")
            TextField(label = "Maintenance", textValue = maintenance, onValueChange = { maintenance = it })
            TextField(label = "UPI ID", textValue = upi, onValueChange = { upi = it })

            DropdownTextField(
                label = "Billing Day (1 - 28)",
                selectedOption = billingDay,
                options = billingDayOptions,
                onOptionSelected = { billingDay = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            WrapButtonWithBackground(
                toDoFunction = {
                    // Validations
                    val maintenanceInt = maintenance.toIntOrNull()
                    val pincodeInt = pincode.toIntOrNull()
                    val upiPattern = Regex("^[\\w.-]{2,256}@\\w{2,64}$")
                    val isUpiValid = upi.matches(upiPattern)

                    if (building.isBlank() || address.isBlank() || city.isBlank()
                        || pincode.isBlank() || maintenance.isBlank() || upi.isBlank()
                    ) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    if (maintenanceInt == null || maintenanceInt <= 0) {
                        Toast.makeText(context, "Enter a valid maintenance amount", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    if (pincodeInt == null || pincodeInt.toString().length != 6) {
                        Toast.makeText(context, "Enter a valid 6-digit pincode", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    if (!isUpiValid) {
                        Toast.makeText(context, "Enter a valid UPI ID", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    if (selectedImageUri == null) {
                        Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    // Upload image and save data
                    val fileName = "building_$adminUID${System.currentTimeMillis()}.jpg"
                    val imageRef = storageRef.child("BuildingImages/$fileName")

                    imageRef.putFile(selectedImageUri!!)
                        .addOnSuccessListener {
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                val newBuildingRef = databaseRef.push()
                                val buildingId = newBuildingRef.key ?: ""

                                val buildingDetails = mutableMapOf<String, Any>(
                                    "building" to building,
                                    "address" to address,
                                    "city" to city,
                                    "pincode" to pincodeInt,
                                    "upi" to upi,
                                    "maintenance" to maintenanceInt,
                                    "adminUID" to adminUID,
                                    "imageUrl" to uri.toString(),
                                    "billingDay" to (billingDay.toIntOrNull() ?: 1),
                                    "platformFeeEnabled" to true,
                                    "approvalLevels" to 2
                                )

                                newBuildingRef.setValue(buildingDetails)
                                    .addOnSuccessListener {
                                        usersRef.child(adminUID).child("buildingIds")
                                            .runTransaction(object : Transaction.Handler {
                                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                                    val currentList = currentData.getValue(object : GenericTypeIndicator<MutableList<String>>() {}) ?: mutableListOf()
                                                    if (!currentList.contains(buildingId)) {
                                                        currentList.add(buildingId)
                                                        currentData.value = currentList
                                                    }
                                                    return Transaction.success(currentData)
                                                }

                                                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                                                    Toast.makeText(context, "Details saved successfully", Toast.LENGTH_SHORT).show()
                                                    val intent = Intent(context, MainActivity::class.java)
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                    context.startActivity(intent)
                                                }
                                            })
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to save details", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        }
                },
                label = "Save"
            )
        }
    }
}

private fun createImageUri(context: Context): Uri? {
    val contentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "temp_image_${System.currentTimeMillis()}.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KZYSolutions")
        }
    }
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}

private fun showImagePickerDialog(
    context: Context,
    onGallery: () -> Unit,
    onCamera: () -> Unit
) {
    val options = arrayOf("Select from Gallery", "Take a Photo")
    AlertDialog.Builder(context)
        .setTitle("Choose Profile Picture")
        .setItems(options) { _, which ->
            when (which) {
                0 -> onGallery()
                1 -> onCamera()
            }
        }
        .show()
}

@Preview
@Composable
fun Preview(){
    NewBuildingDetailsForm()
}*/
 */

class NewBuildingDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KZYSolutionsTheme {
                NewBuildingDetailsForm()
            }
        }
    }
}

@Composable
fun NewBuildingDetailsForm() {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val adminUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val storageRef = FirebaseStorage.getInstance().reference
    val approvalRef = FirebaseDatabase.getInstance().reference.child("BuildingApprovalRequests")

    var upi by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var maintenance by remember { mutableStateOf("") }

    var billingDay by remember { mutableStateOf("1") }
    val billingDayOptions = (1..28).map { it.toString() }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { selectedImageUri = it } }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = cameraImageUri
            Toast.makeText(context, "Image Captured", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraImageUri = createImageUri(context)
            cameraImageUri?.let { cameraLauncher.launch(it) }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    TopBarForLazyColumns(   heading = "Building Details") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "Profile Icon",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable {
                        showImagePickerDialog(
                            context,
                            onGallery = { galleryLauncher.launch("image/*") },
                            onCamera = {
                                val permission = Manifest.permission.CAMERA
                                if (ContextCompat.checkSelfPermission(context, permission)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    cameraImageUri = createImageUri(context)
                                    cameraImageUri?.let { cameraLauncher.launch(it) }
                                } else {
                                    permissionLauncher.launch(permission)
                                }
                            }
                        )
                    },
                tint = Blue2
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(label = "Building Name", textValue = building, onValueChange = { building = it })
            TextField(label = "Address", textValue = address, onValueChange = { address = it })
            TextField(label = "City", textValue = city, onValueChange = { city = it })
            TextField(label = "Pincode", textValue = pincode, onValueChange = { pincode = it }, textType = "phone")
            TextField(label = "Maintenance", textValue = maintenance, onValueChange = { maintenance = it })
            TextField(label = "UPI ID", textValue = upi, onValueChange = { upi = it })

            DropdownTextField(
                label = "Billing Day (1 - 28)",
                selectedOption = billingDay,
                options = billingDayOptions,
                onOptionSelected = { billingDay = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            WrapButtonWithBackground(
                toDoFunction = {
                    val maintenanceInt = maintenance.toIntOrNull()
                    val pincodeInt = pincode.toIntOrNull()
                    val upiPattern = Regex("^[\\w.-]{2,256}@\\w{2,64}$")
                    val isUpiValid = upi.matches(upiPattern)

                    if (building.isBlank() || address.isBlank() || city.isBlank()
                        || pincode.isBlank() || maintenance.isBlank() || upi.isBlank()
                    ) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    if (maintenanceInt == null || maintenanceInt <= 0) {
                        Toast.makeText(context, "Enter a valid maintenance amount", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    if (pincodeInt == null || pincodeInt.toString().length != 6) {
                        Toast.makeText(context, "Enter a valid 6-digit pincode", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    if (!isUpiValid) {
                        Toast.makeText(context, "Enter a valid UPI ID", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    if (selectedImageUri == null) {
                        Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        return@WrapButtonWithBackground
                    }

                    val buildingId = generateBuildingId(building, address, pincode)

                    approvalRef.child(buildingId).get().addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            Toast.makeText(context, "A request for this building already exists", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        val fileName = "building_${adminUID}_${System.currentTimeMillis()}.jpg"
                        val imageRef = storageRef.child("BuildingImages/$fileName")

                        imageRef.putFile(selectedImageUri!!)
                            .addOnSuccessListener {
                                imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    val buildingDetails = mapOf(
                                        "buildingId" to buildingId,
                                        "building" to building,
                                        "address" to address,
                                        "city" to city,
                                        "pincode" to pincodeInt,
                                        "upi" to upi,
                                        "maintenance" to maintenanceInt,
                                        "adminUID" to adminUID,
                                        "imageUrl" to uri.toString(),
                                        "billingDay" to (billingDay.toIntOrNull() ?: 1),
                                        "platformFeeEnabled" to true,
                                        "approvalLevels" to 2,
                                        "timestamp" to ServerValue.TIMESTAMP,
                                        "status" to "pending"
                                    )

                                    approvalRef.child(buildingId).setValue(buildingDetails)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Sent for approval", Toast.LENGTH_SHORT).show()
                                            val intent = Intent(context, MainActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            context.startActivity(intent)
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to send for approval", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                label = "Submit for Approval"
            )
        }
    }
}

fun generateBuildingId(building: String, address: String, pincode: String): String {
    val input = "${building.trim().lowercase()}_${address.trim().lowercase()}_${pincode.trim()}"
    return input.hashCode().toString()
}

private fun createImageUri(context: Context): Uri? {
    val contentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "temp_image_${System.currentTimeMillis()}.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KZYSolutions")
        }
    }
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}

private fun showImagePickerDialog(
    context: Context,
    onGallery: () -> Unit,
    onCamera: () -> Unit
) {
    val options = arrayOf("Select from Gallery", "Take a Photo")
    AlertDialog.Builder(context)
        .setTitle("Choose Building Image")
        .setItems(options) { _, which ->
            when (which) {
                0 -> onGallery()
                1 -> onCamera()
            }
        }
        .show()
}
