package pl.lipov.malin.common.utils.mapBottomSheet

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun FeatureSheetContent(
    feature: MapFeatureInfo
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            feature.title,
            style = MaterialTheme.typography.headlineSmall
        )

        feature.subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            feature.tags.forEach { tag ->
                AssistChip(
                    onClick = {},
                    label = { Text(tag) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            feature.centroid?.let { featureCentroid ->
                Button(onClick = {
                    navigate(context, featureCentroid.latitude, featureCentroid.longitude)
                }) { Text("Nawiguj") }
            }

            Button(onClick = {
                shareFeature(context, feature)
            }) { Text("Udostępnij") }
        }
    }
}

fun shareFeature(
    context: Context,
    feature: MapFeatureInfo
) {
    val text = buildString {
        appendLine(feature.title)
        feature.subtitle?.let { appendLine(it) }
        feature.tags.forEach { appendLine("- $it") }
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    context.startActivity(Intent.createChooser(intent, "Udostępnij pomieszczenie"))
}

private fun navigate(
    context: Context,
    latitude: Double,
    longitude: Double
) {
    val uri = "geo:$latitude,$longitude?q=$latitude,$longitude".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "Brak aplikacji do nawigacji", Toast.LENGTH_LONG).show()
    }
}
