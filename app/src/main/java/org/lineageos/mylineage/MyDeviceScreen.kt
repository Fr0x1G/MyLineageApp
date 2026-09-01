package org.lineageos.mylineage

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class DeviceScreenState { MAIN, STORAGE, BATTERY, UPTIME }

@Composable
fun MyDeviceScreen() {
	var currentScreen by remember { mutableStateOf(DeviceScreenState.MAIN) }
	Crossfade(targetState = currentScreen, label = "ScreenTransition") { state ->
		when (state) {
			DeviceScreenState.STORAGE -> DeviceStorageScreen(onBack = { currentScreen = DeviceScreenState.MAIN })
			DeviceScreenState.BATTERY -> DeviceBatteryScreen(onBack = { currentScreen = DeviceScreenState.MAIN })
			DeviceScreenState.UPTIME -> DeviceUptimeScreen(onBack = { currentScreen = DeviceScreenState.MAIN })
			DeviceScreenState.MAIN -> MainDeviceContent(
				onStorageClick = { currentScreen = DeviceScreenState.STORAGE },
				onBatteryClick = { currentScreen = DeviceScreenState.BATTERY },
				onUptimeClick = { currentScreen = DeviceScreenState.UPTIME }
			)
		}
	}
}

@Composable
fun MainDeviceContent(onStorageClick: () -> Unit, onBatteryClick: () -> Unit, onUptimeClick: () -> Unit) {
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
	var lineageVersion by remember { mutableStateOf("Fetching...") }
	LaunchedEffect(Unit) {
		kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
			val propsToTry = listOf(
				"ro.lineage.display.version",
				"ro.lineage.build.version",
				"ro.modversion",
				"ro.build.display.id"
			)
			var foundVersion = "LineageOS (Unknown)"
			for (prop in propsToTry) {
				try {
					val process = Runtime.getRuntime().exec(arrayOf("getprop", prop))
					val out = process.inputStream.bufferedReader().readLine()?.trim() ?: ""
					if (out.isNotEmpty()) {
						foundVersion = out
						break
					}
				} catch (e: Exception) {
				}
			}

			foundVersion = foundVersion.replace(" dev-keys", "")
				.replace(" release-keys", "")
				.replace(" test-keys", "")
			if (!foundVersion.contains("lineage", ignoreCase = true) && foundVersion != "LineageOS (Unknown)") {
				foundVersion = "LineageOS ($foundVersion)"
			}

			lineageVersion = foundVersion
		}
	}

	val processor = Build.HARDWARE.uppercase()
	Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
		Text("My Device", fontSize = 28.sp, fontWeight = FontWeight.Bold)
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
		Spacer(modifier = Modifier.height(16.dp))
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			Card(modifier = Modifier.weight(1f).clickable { onStorageClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
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
					LinearProgressIndicator(progress = { storageProgress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = lineageColor, trackColor = lineageColor.copy(alpha = 0.2f), strokeCap = StrokeCap.Round)
				}
			}

			Card(modifier = Modifier.weight(1f).clickable { onBatteryClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
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
					LinearProgressIndicator(progress = { batteryLevel / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = lineageColor, trackColor = lineageColor.copy(alpha = 0.2f), strokeCap = StrokeCap.Round)
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

			Card(modifier = Modifier.weight(1f).clickable { onUptimeClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
				Column(modifier = Modifier.padding(16.dp)) {
					Row(verticalAlignment = Alignment.CenterVertically) {
						Icon(Icons.Filled.Schedule, contentDescription = null, tint = lineageColor, modifier = Modifier.size(16.dp))
						Spacer(modifier = Modifier.width(8.dp))
						Text("Uptime", fontSize = 14.sp, fontWeight = FontWeight.Medium)
					}
					Spacer(modifier = Modifier.height(8.dp))
					Text("${hours}h ${minutes}m", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = lineageColor)
				}
			}
		}

		Spacer(modifier = Modifier.height(24.dp))
		Text("System", fontSize = 18.sp, fontWeight = FontWeight.Bold)
		Text("Software and hardware details", fontSize = 14.sp, color = Color.Gray)
		Spacer(modifier = Modifier.height(16.dp))
		Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardBgColor)) {
			Column(modifier = Modifier.padding(16.dp)) {
				SystemListItem(icon = Icons.Filled.Star, title = lineageVersion, subtitle = "Firmware", color = lineageColor)
				HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp), color = Color.Gray.copy(alpha = 0.2f))
				SystemListItem(icon = Icons.Filled.Android, title = "Android ${Build.VERSION.RELEASE}", subtitle = "Android Version", color = Color(0xFF3DDC84))
				HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp), color = Color.Gray.copy(alpha = 0.2f))
				SystemListItem(icon = Icons.Filled.Memory, title = processor, subtitle = "Processor", color = lineageColor)
			}
		}

		Spacer(modifier = Modifier.height(80.dp))
	}
}

@Composable
fun DeviceStorageScreen(onBack: () -> Unit) {
	val context = LocalContext.current
	val lineageColor = Color(0xFF167C80)
	val cardBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
	val stat = StatFs(Environment.getDataDirectory().path)
	val totalGb = stat.totalBytes / (1024f * 1024f * 1024f)
	val usedGb = (stat.totalBytes - stat.availableBytes) / (1024f * 1024f * 1024f)
	val storagePercent = if (stat.totalBytes > 0) ((stat.totalBytes - stat.availableBytes).toFloat() / stat.totalBytes.toFloat()) * 100 else 0f
	val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
	val mi = ActivityManager.MemoryInfo()
	am.getMemoryInfo(mi)
	val totalRamGb = mi.totalMem / (1024f * 1024f * 1024f)
	val usedRamGb = (mi.totalMem - mi.availMem) / (1024f * 1024f * 1024f)
	val ramPercent = if (mi.totalMem > 0) ((mi.totalMem - mi.availMem).toFloat() / mi.totalMem.toFloat()) * 100 else 0f
	Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
		ScreenHeader(title = "Storage & Memory", onBack = onBack)
		Spacer(modifier = Modifier.height(24.dp))
		HealthSectorCard("Internal Storage", Icons.Filled.Storage, storagePercent, String.format("%.1f GB used", usedGb), lineageColor, cardBg)
		Spacer(modifier = Modifier.height(16.dp))
		HealthSectorCard("Memory (RAM)", Icons.Filled.Memory, ramPercent, String.format("%.1f GB used", usedRamGb), lineageColor, cardBg)
		Spacer(modifier = Modifier.height(32.dp))
		Text("Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold)
		Spacer(modifier = Modifier.height(16.dp))
		Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
			Column(modifier = Modifier.padding(16.dp)) {
				StorageCategoryItem(Icons.Filled.Apps, "Coming soon", "??,?? GB", 0.1f, lineageColor)
				StorageCategoryItem(Icons.Outlined.Delete, "Coming soon", "??,?? GB", 0.10f, lineageColor)
				StorageCategoryItem(Icons.Outlined.Folder, "Coming soon", "??,?? GB", 0.20f, lineageColor)
				StorageCategoryItem(Icons.Outlined.VideoLibrary, "Coming soon", "??,?? GB", 0.30f, lineageColor)
				StorageCategoryItem(Icons.Outlined.Image, "Coming soon", "??,?? GB", 0.40f, lineageColor)
				StorageCategoryItem(Icons.Outlined.Audiotrack, "Coming soon", "??,?? MB", 0.50f, lineageColor)
				Spacer(modifier = Modifier.height(16.dp))
				HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
				Spacer(modifier = Modifier.height(16.dp))
				Text("System", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
				Spacer(modifier = Modifier.height(8.dp))
				StorageCategoryItem(Icons.Filled.Android, "Android ${Build.VERSION.RELEASE}", "??,?? GB", 0.8f, Color(0xFF3DDC84))
			}
		}
		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
fun DeviceBatteryScreen(onBack: () -> Unit) {
	val context = LocalContext.current
	val lineageColor = Color(0xFF167C80)
	val statusGreen = Color(0xFF3DDC84)
	val cardBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
	val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
	val batteryLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
	val temp = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
	val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
	val voltage = if (voltageMv > 1000) voltageMv / 1000f else voltageMv.toFloat()
	val statusText = when (batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1) {
		BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
		BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
		BatteryManager.BATTERY_STATUS_FULL -> "Full"
		else -> "Not Charging"
	}

	Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
		ScreenHeader(title = "Battery Health", onBack = onBack)
		Spacer(modifier = Modifier.height(32.dp))
		Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
			CircularProgressIndicator(progress = { batteryLevel / 100f }, modifier = Modifier.size(200.dp), strokeWidth = 12.dp, color = lineageColor, trackColor = lineageColor.copy(alpha = 0.15f), strokeCap = StrokeCap.Round)
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Text("$batteryLevel%", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = lineageColor)
				Text("Battery Level", fontSize = 14.sp, color = Color.Gray)
				Text(statusText, fontSize = 12.sp, color = Color.Gray)
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
			Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
				Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
					Text("$temp °C", fontSize = 22.sp, fontWeight = FontWeight.Bold)
					Text("Temperature", fontSize = 12.sp, color = Color.Gray)
					Spacer(modifier = Modifier.height(8.dp))
					Text(if (temp < 40) "Normal" else "Warm", fontSize = 12.sp, color = if (temp < 40) statusGreen else Color(0xFFE57373), fontWeight = FontWeight.Bold)
				}
			}

			Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
				Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
					Text(String.format("%.1f V", voltage), fontSize = 22.sp, fontWeight = FontWeight.Bold)
					Text("Voltage", fontSize = 12.sp, color = Color.Gray)
					Spacer(modifier = Modifier.height(8.dp))
					Text("Good", fontSize = 12.sp, color = statusGreen, fontWeight = FontWeight.Bold)
				}
			}
		}

		Spacer(modifier = Modifier.height(16.dp))
		Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
			Column(modifier = Modifier.padding(20.dp)) {
				Text("Usage past 24h", fontSize = 14.sp, fontWeight = FontWeight.Bold)
				Spacer(modifier = Modifier.height(16.dp))
				androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
					val path = Path().apply {
						moveTo(0f, size.height * 0.2f)
						lineTo(size.width * 0.3f, size.height * 0.5f)
						lineTo(size.width * 0.6f, size.height * 0.9f)
						lineTo(size.width * 0.8f, size.height * 0.1f)
						lineTo(size.width, size.height * 0.3f)
					}
					drawPath(path, color = lineageColor, style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
				}
			}
		}
		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
fun DeviceUptimeScreen(onBack: () -> Unit) {
	val lineageColor = Color(0xFF167C80)
	val cardBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
	val uptimeMillis = SystemClock.elapsedRealtime()
	val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(uptimeMillis)
	val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
	Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
		ScreenHeader(title = "System Uptime", onBack = onBack)
		Spacer(modifier = Modifier.height(24.dp))
		Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardBg)) {
			Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
				Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
					CircularProgressIndicator(progress = { 0.8f }, modifier = Modifier.size(160.dp), strokeWidth = 10.dp, color = lineageColor, trackColor = lineageColor.copy(alpha = 0.15f), strokeCap = StrokeCap.Round)
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Text("$hours", fontSize = 48.sp, fontWeight = FontWeight.Bold)
						Text("${minutes}m", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = lineageColor)
						Text("Total Uptime", fontSize = 12.sp, color = Color.Gray)
					}
				}

				Spacer(modifier = Modifier.height(32.dp))
				Text("Stability Trend", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
				Spacer(modifier = Modifier.height(12.dp))
				androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
					val path = Path().apply {
						moveTo(0f, size.height)
						cubicTo(size.width * 0.25f, size.height * 0.8f, size.width * 0.5f, size.height * 0.5f, size.width, size.height * 0.2f)
					}
					val fillPath = Path().apply {
						addPath(path)
						lineTo(size.width, size.height)
						lineTo(0f, size.height)
						close()
					}
					drawPath(path = fillPath, brush = Brush.verticalGradient(listOf(lineageColor.copy(alpha = 0.4f), Color.Transparent)))
					drawPath(path, color = lineageColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
					drawCircle(color = lineageColor, radius = 8f, center = Offset(size.width, size.height * 0.2f))
					drawCircle(color = Color.White, radius = 4f, center = Offset(size.width, size.height * 0.2f))
				}

				Spacer(modifier = Modifier.height(24.dp))
				HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
				Spacer(modifier = Modifier.height(16.dp))
				Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
					Column {
						Text("Status", fontSize = 11.sp, color = Color.Gray)
						Text("Stable", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3DDC84))
					}

					Column {
						Text("Avg per day", fontSize = 11.sp, color = Color.Gray)
						Text("23h 44m", fontSize = 14.sp, fontWeight = FontWeight.Bold)
					}

					Column {
						Text("Record Uptime", fontSize = 11.sp, color = Color.Gray)
						Text("${hours + 42}h 10m", fontSize = 14.sp, fontWeight = FontWeight.Bold)
					}
				}
			}
		}
	}
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
	Row(verticalAlignment = Alignment.CenterVertically) {
		IconButton(onClick = onBack) {
			Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
		}
		Spacer(modifier = Modifier.width(8.dp))
		Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
	}
}

@Composable
fun StorageCategoryItem(
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	title: String,
	sizeText: String,
	progress: Float,
	color: Color
) {
	Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
			Spacer(modifier = Modifier.width(12.dp))
			Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
			Spacer(modifier = Modifier.weight(1f))
			Text(sizeText, fontSize = 14.sp, color = Color.Gray)
		}

		Spacer(modifier = Modifier.height(8.dp))
		LinearProgressIndicator(
			progress = { progress },
			modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
			color = color,
			trackColor = color.copy(alpha = 0.1f),
			strokeCap = StrokeCap.Round
		)
	}
}

@Composable
fun HealthSectorCard(
	title: String,
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	percent: Float,
	usedText: String,
	lineageColor: Color,
	cardBg: Color
) {
	var expanded by remember { mutableStateOf(false) }
	val blocks = remember(percent) {
		val totalBlocks = 7 * 22
		val usedCount = ((percent / 100f) * totalBlocks).toInt()
		val list = MutableList(totalBlocks) { index ->
			if (index < usedCount) (1..3).random() else 0
		}
		list.shuffle()
		list
	}

	Card(
		modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
		shape = RoundedCornerShape(20.dp),
		colors = CardDefaults.cardColors(containerColor = cardBg)
	) {
		Column(modifier = Modifier.padding(20.dp)) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(icon, contentDescription = null, tint = lineageColor, modifier = Modifier.size(20.dp))
				Spacer(modifier = Modifier.width(12.dp))
				Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
				Spacer(modifier = Modifier.weight(1f))
				Text("${percent.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = lineageColor)
			}

			Spacer(modifier = Modifier.height(8.dp))
			Text(usedText, fontSize = 12.sp, color = Color.Gray)
			Spacer(modifier = Modifier.height(12.dp))
			androidx.compose.animation.AnimatedVisibility(visible = !expanded) {
				LinearProgressIndicator(
					progress = { percent / 100f },
					modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
					color = lineageColor,
					trackColor = lineageColor.copy(alpha = 0.15f),
					strokeCap = StrokeCap.Round
				)
			}

			androidx.compose.animation.AnimatedVisibility(visible = expanded) {
				Column {
					Text("Sector Allocation Map", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
					Spacer(modifier = Modifier.height(12.dp))
					Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
						for (col in 0 until 22) {
							Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
								for (row in 0 until 7) {
									val index = col * 7 + row
									val state = blocks[index]
									val color = if (state == 0) Color.Gray.copy(alpha = 0.15f)
									else lineageColor.copy(alpha = when (state) { 1 -> 0.4f; 2 -> 0.7f; else -> 1.0f })
									Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
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
fun SystemListItem(
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	title: String,
	subtitle: String,
	color: Color
) {
	Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
		Box(
			modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape),
			contentAlignment = Alignment.Center
		) {
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
