package org.lineageos.mylineage

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyDeviceScreen() {
	val context = LocalContext.current
	val lineageColor = Color(0xFF167C80)
	val cardBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
	val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
	val model = Build.MODEL
	val stat = StatFs(Environment.getDataDirectory().path)
	val totalBytes = stat.totalBytes
	val usedBytes = totalBytes - stat.availableBytes
	val usedGb = usedBytes / (1024f * 1024f * 1024f)
	val totalGb = totalBytes / (1024f * 1024f * 1024f)
	val storageProgress = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f
	val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
	val batteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
	val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
	val tempC = temp / 10f
	val uptimeMillis = SystemClock.elapsedRealtime()
	val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(uptimeMillis)
	val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
	val uptimeString = "${hours}h ${minutes}m"
	val lineageVersion = remember {
    	try {
        	val systemProperties = Class.forName("android.os.SystemProperties")
        	val getMethod = systemProperties.getMethod("get", String::class.java)
        	val display = getMethod.invoke(null, "ro.lineage.display.version") as String
        	display.ifEmpty { "LineageOS (Unknown)" }
    	} catch (e: Exception) {
        	"LineageOS 21"
    	}
	}

	Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
    	Text("My Device", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    	Text("Everything about your device", fontSize = 14.sp, color = Color.Gray)
    	Spacer(modifier = Modifier.height(24.dp))
    	Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
        	Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            	Box(modifier = Modifier.size(64.dp, 100.dp).background(lineageColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                	Box(modifier = Modifier.size(56.dp, 92.dp).background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp)))
            	}
            	Spacer(modifier = Modifier.width(24.dp))
            	Column {
                	Text(manufacturer, fontSize = 16.sp, color = Color.Gray)
                	Text(model, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                	Spacer(modifier = Modifier.height(8.dp))
                	Box(modifier = Modifier.background(lineageColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    	Text("Active", color = lineageColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                	}
            	}
        	}
    	}

    	Spacer(modifier = Modifier.height(24.dp))
    	Text("Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    	Text("All key info at a glance", fontSize = 14.sp, color = Color.Gray)
    	Spacer(modifier = Modifier.height(16.dp))

    	Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        	Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
            	Column(modifier = Modifier.padding(16.dp)) {
                	Row(verticalAlignment = Alignment.CenterVertically) {
                    	Icon(Icons.Filled.Storage, contentDescription = null, tint = lineageColor, modifier = Modifier.size(16.dp))
                    	Spacer(modifier = Modifier.width(8.dp))
                    	Text("Storage", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                	}
                	Spacer(modifier = Modifier.height(12.dp))
                	Text(String.format("%.1f GB", usedGb), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                	Text(String.format("of %.1f GB", totalGb), fontSize = 12.sp, color = Color.Gray)
                	Spacer(modifier = Modifier.height(8.dp))
                	LinearProgressIndicator(progress = storageProgress, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = lineageColor, trackColor = lineageColor.copy(alpha = 0.2f), strokeCap = StrokeCap.Round)
            	}
        	}
        	Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
            	Column(modifier = Modifier.padding(16.dp)) {
                	Row(verticalAlignment = Alignment.CenterVertically) {
                    	Icon(Icons.Filled.BatteryChargingFull, contentDescription = null, tint = lineageColor, modifier = Modifier.size(16.dp))
                    	Spacer(modifier = Modifier.width(8.dp))
                    	Text("Battery", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                	}
                	Spacer(modifier = Modifier.height(12.dp))
                	Text("$batteryLevel%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = lineageColor)
                	Text("Available", fontSize = 12.sp, color = Color.Gray)
                	Spacer(modifier = Modifier.height(8.dp))
                	LinearProgressIndicator(progress = batteryLevel / 100f, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = lineageColor, trackColor = lineageColor.copy(alpha = 0.2f), strokeCap = StrokeCap.Round)
            	}
        	}
    	}
    	Spacer(modifier = Modifier.height(12.dp))

    	Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        	Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
            	Column(modifier = Modifier.padding(16.dp)) {
                	Row(verticalAlignment = Alignment.CenterVertically) {
                    	Icon(Icons.Filled.Thermostat, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                    	Spacer(modifier = Modifier.width(8.dp))
                    	Text("Temperature", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                	}
                	Spacer(modifier = Modifier.height(8.dp))
                	Text("$tempC °C", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
            	}
        	}
        	Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
            	Column(modifier = Modifier.padding(16.dp)) {
                	Row(verticalAlignment = Alignment.CenterVertically) {
                    	Icon(Icons.Filled.Schedule, contentDescription = null, tint = lineageColor, modifier = Modifier.size(16.dp))
                    	Spacer(modifier = Modifier.width(8.dp))
                    	Text("Uptime", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                	}
                	Spacer(modifier = Modifier.height(8.dp))
                	Text(uptimeString, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = lineageColor)
            	}
        	}
    	}

    	Spacer(modifier = Modifier.height(24.dp))
    	Text("System", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    	Text("Software and platform details", fontSize = 14.sp, color = Color.Gray)
    	Spacer(modifier = Modifier.height(16.dp))

    	Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
        	Column(modifier = Modifier.padding(16.dp)) {
            	SystemListItem(icon = Icons.Filled.Star, title = lineageVersion, subtitle = "Firmware", color = lineageColor)
            	Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp), color = Color.Gray.copy(alpha = 0.2f))
            	SystemListItem(icon = Icons.Filled.Android, title = "Android ${Build.VERSION.RELEASE}", subtitle = "Android Version", color = Color(0xFF3DDC84))
            	Divider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp), color = Color.Gray.copy(alpha = 0.2f))
            	SystemListItem(icon = Icons.Filled.Code, title = "API Level ${Build.VERSION.SDK_INT}", subtitle = "Platform", color = lineageColor)
        	}
    	}
    	Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
fun SystemListItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, color: Color) {
	Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
    	Box(modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
        	Icon(icon, contentDescription = null, tint = color)
    	}
    	Spacer(modifier = Modifier.width(16.dp))
    	Column(modifier = Modifier.weight(1f)) {
        	Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        	Text(subtitle, fontSize = 12.sp, color = Color.Gray)
    	}
    	Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
	}
}