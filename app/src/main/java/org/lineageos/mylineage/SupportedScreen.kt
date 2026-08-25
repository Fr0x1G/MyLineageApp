package org.lineageos.mylineage

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DeviceInfo(val model: String, val name: String, val brand: String, val type: String, val isOfficial: Boolean)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportedScreen() {
    val context = LocalContext.current
    val lineageColor = Color(0xFF167C80)
    val officialColor = Color(0xFF3DDC84)
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Phone", "Tablet", "TV", "WearOS")
    var selectedCategory by remember { mutableIntStateOf(0) }
    var allDevices by remember { mutableStateOf<List<DeviceInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filterExpanded by remember { mutableStateOf(false) }
    var sortAZ by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        isLoading = true
        val fetchedList = withContext(Dispatchers.IO) {
            val list = mutableListOf<DeviceInfo>()
            try {
                val url = java.net.URL("https://raw.githubusercontent.com/LineageOS/hudson/master/updater/devices.json")
                val conn = url.openConnection() as java.net.HttpURLConnection
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                val arr = org.json.JSONArray(text)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val model = obj.optString("model", "")
                    val brand = obj.optString("oem", "Unknown")
                    val name = obj.optString("name", "Unknown Device")
                    val type = when {
                        name.contains("Tab", true) || name.contains("Pad", true) -> "Tablet"
                        name.contains("TV", true) || name.contains("Shield", true) -> "TV"
                        name.contains("Watch", true) -> "WearOS"
                        else -> "Phone"
                    }
                    list.add(DeviceInfo(model, name, brand, type, true))
                }
            } catch (e: Exception) {
                android.util.Log.e("DevicesParser", "Ошибка: ${e.message}")
            }
            list
        }
        allDevices = fetchedList
        isLoading = false
    }

    val filteredDevices = remember(allDevices, selectedCategory, searchQuery, sortAZ) {
        var res = allDevices.filter { device ->
            val matchesCategory = selectedCategory == 0 || device.type == categories[selectedCategory]
            val matchesSearch = device.name.contains(searchQuery, ignoreCase = true) || device.brand.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        res = if (sortAZ) res.sortedBy { it.brand + it.name } else res.sortedByDescending { it.brand + it.name }
        res
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("Supported Devices", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("These devices officially support LineageOS.", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search devices...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = lineageColor,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                )
            )

            Box {
                OutlinedButton(
                    onClick = { filterExpanded = true },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = lineageColor)
                ) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Filter")
                }

                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (sortAZ) "✓ Sort A-Z" else "Sort A-Z") },
                        onClick = { sortAZ = true; filterExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text(if (!sortAZ) "✓ Sort Z-A" else "Sort Z-A") },
                        onClick = { sortAZ = false; filterExpanded = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories.size) { index ->
                val isSelected = selectedCategory == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) lineageColor else Color.Transparent)
                        .clickable { selectedCategory = index }
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = categories[index],
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (isLoading) {
                Text("Loading...", fontSize = 12.sp, color = lineageColor)
            } else {
                Text("${filteredDevices.size} devices", fontSize = 12.sp, color = Color.Gray)
            }
            Text("Synced with GitHub", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                CircularProgressIndicator(color = lineageColor, modifier = Modifier.padding(32.dp))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredDevices) { device ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wiki.lineageos.org/devices/${device.model}"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp, 72.dp)
                                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when(device.type) {
                                    "Tablet" -> Icons.Filled.TabletMac
                                    "TV" -> Icons.Filled.Tv
                                    "WearOS" -> Icons.Filled.Watch
                                    else -> Icons.Filled.Smartphone
                                }
                                Icon(icon, contentDescription = null, tint = lineageColor)
                            }

                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(device.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${device.brand} (${device.model})", color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .background(officialColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Official", color = officialColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}