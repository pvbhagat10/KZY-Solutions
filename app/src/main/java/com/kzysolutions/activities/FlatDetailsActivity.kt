package com.kzysolutions.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kzysolutions.activities.ui.theme.KZYSolutionsTheme
import com.kzysolutions.utils.PlatformFee

class FlatDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var platformFeeStatus = "unpaid"
        val flatNumber = intent.getStringExtra("flatNumber") ?: ""
        val wingNumber = intent.getStringExtra("wingNumber") ?: ""
        val buildingName = intent.getStringExtra("buildingName") ?: ""
        val buildingAddress = intent.getStringExtra("buildingAddress") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""

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
                Surface(modifier = Modifier.fillMaxSize()) {
                    TopBar(heading = "Flat Details") {

                        if (platformFeeStatus == "unpaid"){
                            BannerAdView()
                        }

                        FlatDetailsScreen(
                            flatNumber = flatNumber,
                            wingNumber = wingNumber,
                            buildingName = buildingName,
                            buildingAddress = buildingAddress,
                            imageUrl = imageUrl,
                            platformFeeStatus
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlatDetailsScreen(
    flatNumber: String,
    wingNumber: String,
    buildingName: String,
    buildingAddress: String,
    imageUrl: String,
    platformFeeStatus: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Building Image",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(20.dp))

        DetailItem(label = "Flat Number", value = flatNumber)
        DetailItem(label = "Wing Number", value = wingNumber)
        DetailItem(label = "Building Name", value = buildingName)
        DetailItem(label = "Address", value = buildingAddress)
    }

    if (platformFeeStatus == "unpaid"){
        BannerAdView()
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
