package org.lineageos.mylineage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch
import org.lineageos.mylineage.ui.theme.MyLineageTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			MyLineageTheme {
				Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
					AppController()
				}
			}
		}
	}
}

@Composable
fun AppController() {
	var showLogcat by remember { mutableStateOf(false) }
	Crossfade(targetState = showLogcat, label = "LogcatTransition") { isLogcat ->
		if (isLogcat) {
			LogcatScreen(onBack = { showLogcat = false })
		} else {
			LineageMainScreen(onOpenLogcat = { showLogcat = true })
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineageMainScreen(onOpenLogcat: () -> Unit) {
	val lineageColor = Color(0xFF167C80)
	val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
	val coroutineScope = rememberCoroutineScope()
	val items = listOf("Supported", "News", "My Device")
	val icons = listOf(Icons.Filled.Star, Icons.AutoMirrored.Filled.List, Icons.Filled.Phone)
	Scaffold(
		topBar = {
			CenterAlignedTopAppBar(
				title = {
					Text(
						text = "LINEAGEOS",
						fontWeight = FontWeight.ExtraBold,
						letterSpacing = 2.sp,
						color = lineageColor
					)
				},
				colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
			)
		},
		bottomBar = {
			NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
				items.forEachIndexed { index, item ->
					NavigationBarItem(
						icon = { Icon(icons[index], contentDescription = item) },
						label = { Text(item) },
						selected = pagerState.currentPage == index,
						onClick = {
							coroutineScope.launch {
								pagerState.animateScrollToPage(index)
							}
						},
						colors = NavigationBarItemDefaults.colors(
							selectedIconColor = lineageColor,
							selectedTextColor = lineageColor,
							indicatorColor = lineageColor.copy(alpha = 0.2f)
						)
					)
				}
			}
		},

		floatingActionButton = {
			FloatingActionButton(
				onClick = onOpenLogcat,
				containerColor = lineageColor,
				contentColor = Color.White,
				shape = RoundedCornerShape(16.dp)
			) {
				Icon(Icons.Filled.BugReport, contentDescription = "Open Logcat")
			}
		}
	) { innerPadding ->
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.padding(innerPadding).fillMaxSize()
		) { page ->
			when (page) {
				0 -> SupportedScreen()
				1 -> NewsScreen()
				2 -> MyDeviceScreen()
			}
		}
	}
}