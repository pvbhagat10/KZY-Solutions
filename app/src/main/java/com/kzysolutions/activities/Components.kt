package com.kzysolutions.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.kzysolutions.R
import com.kzysolutions.activities.ui.theme.Blue2
import com.kzysolutions.activities.ui.theme.Transparent
import com.kzysolutions.activities.ui.theme.White
import com.kzysolutions.activities.ui.theme.Yellow
import com.kzysolutions.activities.ui.theme.Yellow_Deep

data class MenuItem(
    val icon: ImageVector,
    val title: String,
    val activityClass: Class<out Activity>,
    val extra: String
)

data class ItemData(
    val imageUrl: String,
    val title: String,
    val subtitle: String,
    val id: String,
    val uid: String,
    val flatNumber: String,
    val wingNumber: String,
    val flatId: String
)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(heading: String, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(Blue2),
                title = {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = heading,
                            fontSize = 30.sp,
                            color = White,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Transparent,
                    titleContentColor = White
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp)
            ) {
                content(innerPadding)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarForLazyColumns(
    heading: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(Blue2),
                title = {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = heading,
                            fontSize = 30.sp,
                            color = White,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Transparent,
                    titleContentColor = White
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarForLazyColumnsLogOut(
    heading: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = "Confirm Logout", fontSize = 20.sp)
            },
            text = {
                Text("Are you sure you want to log out?")
            },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseAuth.getInstance().signOut()
                    context.startActivity(Intent(context, PhoneLogin::class.java))
                    (context as? Activity)?.finish()
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(Blue2),
                title = {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = heading,
                            fontSize = 30.sp,
                            color = White,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        showDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Transparent,
                    titleContentColor = White
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                content()
            }
        }
    )
}

@Composable
fun TextField(
    label: String,
    textValue: String,
    onValueChange: (String) -> Unit,
    textType: String = stringResource(id = R.string.regular),
    isPasswordTextField: Boolean = false,
    enabled: Boolean = true
) {
    val keyboardType = when (textType) {
        stringResource(id = R.string.regular) -> KeyboardType.Text
        stringResource(id = R.string.email) -> KeyboardType.Email
        stringResource(id = R.string.phone) -> KeyboardType.Phone
        else -> KeyboardType.Text
    }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = textValue,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 10.dp),
        enabled = enabled,
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPasswordTextField) KeyboardType.Password else keyboardType),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        visualTransformation = if (isPasswordTextField) {
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isPasswordTextField) {
            {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        } else {
            null
        }
    )
}

@Composable
fun DividerLine(color1: Color, color2: Color, width: Float) {
    Column(
        modifier = Modifier
            .height(2.dp)
            .fillMaxWidth(width)
            .background(Brush.horizontalGradient(listOf(color1, color2)))
    ) {}
}

@Composable
fun WrapButtonWithBackground(toDoFunction: () -> Unit, label: String) {
    Button(
        onClick = { toDoFunction() },
        modifier = Modifier
            .padding(0.dp, 25.dp),
        border = BorderStroke(2.dp, Yellow),
        shape = RoundedCornerShape(5000.dp),
        colors = ButtonDefaults.buttonColors(Blue2),
        contentPadding = PaddingValues(32.dp, 16.dp)
    ) {
        Text(
            text = label, color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun VerticalOrLine() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .height(2.dp)
                .weight(5f)
                .background(Brush.horizontalGradient(listOf(Yellow_Deep, Yellow)))
        ) {}
        Text(
            text = stringResource(id = R.string.or),
            modifier = Modifier
                .weight(1f)
                .size(20.dp),
            textAlign = TextAlign.Center,
            color = Yellow,
        )
        Column(
            modifier = Modifier
                .height(2.dp)
                .weight(5f)
                .background(Brush.horizontalGradient(listOf(Yellow, Yellow_Deep)))
        ) {}
    }
}

@Composable
fun BorderButton(toDoFunction: () -> Unit, label: String) {
    Button(
        onClick = { toDoFunction() },
        modifier = Modifier.padding(0.dp, 25.dp),
        border = BorderStroke(2.dp, Yellow),
        shape = RoundedCornerShape(5000.dp),
        colors = ButtonDefaults.buttonColors(Transparent),
        contentPadding = PaddingValues(32.dp, 16.dp)
    ) {
        Text(
            text = label,
            color = Blue2,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun BorderRadioButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = if (isSelected) {
            ButtonDefaults.buttonColors(containerColor = Blue2)
        } else {
            ButtonDefaults.buttonColors(containerColor = Transparent)
        },
        border = BorderStroke(2.dp, Yellow),
        shape = RoundedCornerShape(5000.dp),
        contentPadding = PaddingValues(32.dp, 16.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Blue2,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun UnderlineButton(toDoFunction: () -> Unit, label: String) {
    Text(
        modifier = Modifier.clickable(enabled = true) {
            toDoFunction()
        },
        text = label,
        color = Blue2,
        fontStyle = FontStyle.Italic,
        fontSize = 24.sp,
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight(1000),
        letterSpacing = 1.sp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownTextField(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val menuWidth = screenWidth * 0.8f

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(menuWidth)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MenuItemList(items: List<MenuItem>) {
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable {
                        val intent = Intent(context, item.activityClass)
                        intent.putExtra(item.extra, item.extra)
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(96.dp)
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ItemsList(
    items: List<ItemData>,
    modifier: Modifier = Modifier,
    onClick: (ItemData) -> Unit,
    buttonActions: List<Pair<String, (ItemData) -> Unit>>
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            ItemRow(item = item, onClick = onClick, buttonActions = buttonActions)
        }
    }
}

@Composable
fun ItemRow(
    item: ItemData,
    onClick: (ItemData) -> Unit,
    buttonActions: List<Pair<String, (ItemData) -> Unit>>
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick(item) },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = item.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = item.subtitle, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttonActions.getOrNull(0)?.let { (label, action) ->
                        Button(
                            onClick = { action(item) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(2.dp, Yellow),
                            shape = RoundedCornerShape(5000.dp),
                            colors = ButtonDefaults.buttonColors(Blue2),
                            contentPadding = PaddingValues(32.dp, 16.dp)
                        ) {
                            Text(
                                text = label, color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    buttonActions.getOrNull(1)?.let { (label, action) ->
                        Button(
                            onClick = { action(item) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(2.dp, Yellow),
                            shape = RoundedCornerShape(5000.dp),
                            colors = ButtonDefaults.buttonColors(Blue2),
                            contentPadding = PaddingValues(32.dp, 16.dp)
                        ) {
                            Text(
                                text = label, color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    buttonActions.getOrNull(2)?.let { (label, action) ->
//                        Button(
//                            onClick = { action(item) },
//                            modifier = Modifier.weight(1f),
//                            border = BorderStroke(2.dp, Yellow),
//                            shape = RoundedCornerShape(5000.dp),
//                            colors = ButtonDefaults.buttonColors(Blue2),
//                            contentPadding = PaddingValues(32.dp, 16.dp)
//                        ) {
//                            Text(
//                                text = label, color = Color.White,
//                                fontWeight = FontWeight.Bold,
//                                style = MaterialTheme.typography.bodyLarge
//                            )
//                        }
//                    }
//                    buttonActions.getOrNull(3)?.let { (label, action) ->
//                        Button(
//                            onClick = { action(item) },
//                            modifier = Modifier.weight(1f),
//                            border = BorderStroke(2.dp, Yellow),
//                            shape = RoundedCornerShape(5000.dp),
//                            colors = ButtonDefaults.buttonColors(Blue2),
//                            contentPadding = PaddingValues(32.dp, 16.dp)
//                        ) {
//                            Text(
//                                text = label, color = Color.White,
//                                fontWeight = FontWeight.Bold,
//                                style = MaterialTheme.typography.bodyLarge
//                            )
//                        }
//                    }
//                }
            }
        }
    }
}



@Composable
fun ItemsList3(
    items: List<ItemData>,
    modifier: Modifier = Modifier,
    onClick: (ItemData) -> Unit,
    buttonActions: List<Pair<String, (ItemData) -> Unit>>
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            ItemRow3(item = item, onClick = onClick, buttonActions = buttonActions)
        }
    }
}

@Composable
fun ItemRow3(
    item: ItemData,
    onClick: (ItemData) -> Unit,
    buttonActions: List<Pair<String, (ItemData) -> Unit>>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = item.subtitle, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttonActions.getOrNull(0)?.let { (label, action) ->
                        Button(
                            onClick = { action(item) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(2.dp, Yellow),
                            shape = RoundedCornerShape(5000.dp),
                            colors = ButtonDefaults.buttonColors(Blue2),
                            contentPadding = PaddingValues(32.dp, 16.dp)
                        ) {
                            Text(
                                text = label, color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    buttonActions.getOrNull(1)?.let { (label, action) ->
                        Button(
                            onClick = { action(item) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(2.dp, Yellow),
                            shape = RoundedCornerShape(5000.dp),
                            colors = ButtonDefaults.buttonColors(Blue2),
                            contentPadding = PaddingValues(32.dp, 16.dp)
                        ) {
                            Text(
                                text = label, color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

//                Spacer(modifier = Modifier.height(8.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    buttonActions.getOrNull(2)?.let { (label, action) ->
//                        Button(
//                            onClick = { action(item) },
//                            modifier = Modifier.weight(1f),
//                            border = BorderStroke(2.dp, Yellow),
//                            shape = RoundedCornerShape(5000.dp),
//                            colors = ButtonDefaults.buttonColors(Blue2),
//                            contentPadding = PaddingValues(32.dp, 16.dp)
//                        ) {
//                            Text(
//                                text = label, color = Color.White,
//                                fontWeight = FontWeight.Bold,
//                                style = MaterialTheme.typography.bodyLarge
//                            )
//                        }
//                    }
//                    buttonActions.getOrNull(3)?.let { (label, action) ->
//                        Button(
//                            onClick = { action(item) },
//                            modifier = Modifier.weight(1f),
//                            border = BorderStroke(2.dp, Yellow),
//                            shape = RoundedCornerShape(5000.dp),
//                            colors = ButtonDefaults.buttonColors(Blue2),
//                            contentPadding = PaddingValues(32.dp, 16.dp)
//                        ) {
//                            Text(
//                                text = label, color = Color.White,
//                                fontWeight = FontWeight.Bold,
//                                style = MaterialTheme.typography.bodyLarge
//                            )
//                        }
//                    }
//                }
            }
        }
    }
}

data class MenuItem2(
    val icon: ImageVector,
    val title: String,
    val activityClass: Class<out Activity>,
    val extra: String
)

data class ItemData2(
    val imageUrl: String,
    val title: String,
    val subtitle: String,
    val id: String, 
    val uid: String, 
    val flatNumber: String,
    val wingNumber: String,
    val flatId: String
)


@Composable
fun ItemsList2(
    items: List<ItemData>,
    modifier: Modifier = Modifier,
    onClick: (ItemData) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            ItemRow2(item = item, onClick = { onClick(item) })
        }
    }
}

@Composable
fun ItemRow2(item: ItemData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = "Building Image",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            Text(text = item.subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun BannerAdView(platformFeeStatus: String) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { context ->
                AdView(context).apply {
                    this.adUnitId = "ca-app-pub-39402569942544/63009781"
                    setAdSize(AdSize.BANNER)
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
}

@Composable
fun BannerAdView() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                this.adUnitId = "ca-app-pub-30994959509182/35680634"
                setAdSize(AdSize.BANNER)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Preview
@Composable
fun Top() {
    TopBarForLazyColumns("asd") { }
}
