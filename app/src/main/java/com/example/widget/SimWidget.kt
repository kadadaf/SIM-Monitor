package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.color.ColorProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.SIMCard

import androidx.glance.action.actionStartActivity

class SimWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load data on background thread
        val db = AppDatabase.getDatabase(context)
        val dao = db.simCardDao()
        val simCards = dao.getAllSIMCards()

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFFF7F4FF))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "SIM Monitor", // You could localize
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(day = Color(0xFF4B39C2), night = Color(0xFF4B39C2))
                        ),
                        modifier = GlanceModifier.padding(bottom = 8.dp)
                    )

                    if (simCards.isEmpty()) {
                        Text(
                            text = "No SIM Cards",
                            style = TextStyle(fontSize = 14.sp, color = ColorProvider(day = Color.Gray, night = Color.Gray)),
                        )
                    } else {
                        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                            items(simCards) { sim ->
                                SimCardWidgetItem(sim)
                            }
                        }
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun SimCardWidgetItem(sim: SIMCard) {
        val statusColor = when (sim.status) {
            "HEALTHY" -> Color(0xFF4CAF50)
            "ATTENTION" -> Color(0xFFFF9800)
            "RISK", "EXPIRED" -> Color(0xFFF44336)
            else -> Color.Gray
        }
        val statusText = when (sim.status) {
            "HEALTHY" -> "Healthy"
            "ATTENTION" -> "Attention"
            "RISK" -> "Risk"
            "EXPIRED" -> "Expired"
            else -> "Unknown"
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(Color.White)
                .clickable(actionStartActivity<com.example.MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = GlanceModifier.width(4.dp).background(statusColor).size(40.dp))
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = sim.name,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorProvider(day = Color.Black, night = Color.White))
                )
                Text(
                    text = "${sim.carrier} · $statusText",
                    style = TextStyle(fontSize = 12.sp, color = ColorProvider(day = Color.Gray, night = Color.LightGray))
                )
            }
        }
    }
}
