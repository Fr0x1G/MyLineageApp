package org.lineageos.mylineage

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NewsItem(val title: String, val date: String, val link: String)
suspend fun fetchLineageNews(): List<NewsItem> = withContext(Dispatchers.IO) {
	val list = mutableListOf<NewsItem>()
	try {
		val url = java.net.URL("https://lineageos.org/feed.xml")
		val connection = url.openConnection() as java.net.HttpURLConnection
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
		connection.connectTimeout = 5000
		connection.readTimeout = 5000
		val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
		val doc = factory.newDocumentBuilder().parse(connection.inputStream)
		var nodes = doc.getElementsByTagName("entry")
		if (nodes.length == 0) nodes = doc.getElementsByTagName("item")
		for (i in 0 until nodes.length) {
			val element = nodes.item(i) as org.w3c.dom.Element
			var title = "LineageOS Update"
			val titleNodes = element.getElementsByTagName("title")
			if (titleNodes.length > 0) title = titleNodes.item(0).textContent.replace("<![CDATA[", "").replace("]]>", "").trim()
			var link = "https://lineageos.org"
			val linkNodes = element.getElementsByTagName("link")
			if (linkNodes.length > 0) {
				val linkElement = linkNodes.item(0) as org.w3c.dom.Element
				link = linkElement.getAttribute("href")
				if (link.isEmpty()) link = linkElement.textContent.trim()
			}
			var date = ""
			var dateNodes = element.getElementsByTagName("published")
			if (dateNodes.length == 0) dateNodes = element.getElementsByTagName("updated")
			if (dateNodes.length == 0) dateNodes = element.getElementsByTagName("pubDate")
			if (dateNodes.length > 0) {
				val dateRaw = dateNodes.item(0).textContent.trim()
				date = if (dateRaw.length >= 10) dateRaw.substring(0, 10) else dateRaw
			}
			list.add(NewsItem(title, date, link))
			if (list.size >= 30) break
		}
	} catch (e: Exception) {
		android.util.Log.e("NewsParser", "Ошибка: ${e.message}", e)
	}
	return@withContext list
}

@Composable
fun NewsScreen() {
	val context = LocalContext.current
	val lineageColor = Color(0xFF167C80)
	var newsList by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
	var isLoading by remember { mutableStateOf(true) }
	val categories = listOf("All", "Updates", "Changelogs", "Bookmarks")
	var selectedCategory by remember { mutableIntStateOf(0) }
	var bookmarkedLinks by remember { mutableStateOf(setOf<String>()) }
	val filteredNews = remember(newsList, selectedCategory, bookmarkedLinks) {
		newsList.filter { news ->
			when (selectedCategory) {
				0 -> true // All
				1 -> !news.title.contains("Changelog", ignoreCase = true)
				2 -> news.title.contains("Changelog", ignoreCase = true)
				3 -> bookmarkedLinks.contains(news.link)
				else -> true
			}
		}
	}

	LaunchedEffect(Unit) {
		isLoading = true
		newsList = fetchLineageNews()
		isLoading = false
	}

	LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
		item {
			Spacer(modifier = Modifier.height(16.dp))
			LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
				items(categories.size) { index ->
					val isSelected = selectedCategory == index
					Box(
						modifier = Modifier
							.clip(RoundedCornerShape(20.dp))
							.background(if (isSelected) lineageColor else Color.Transparent)
							.clickable { selectedCategory = index }
							.padding(horizontal = 16.dp, vertical = 8.dp)
					) {
						Text(
							text = categories[index],
							color = if (isSelected) Color.White else lineageColor,
							fontWeight = FontWeight.Bold,
							fontSize = 14.sp
						)
					}
				}
			}
			Spacer(modifier = Modifier.height(24.dp))
		}

		if (isLoading) {
			item {
				Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
					CircularProgressIndicator(color = lineageColor, modifier = Modifier.padding(32.dp))
				}
			}
		} else if (newsList.isEmpty()) {
			item {
				Text("Could not load news.", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center)
			}
		} else if (filteredNews.isEmpty()) {
			item {
				Text("No news found in this category.", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center)
			}
		} else {
			item {
				val featuredNews = filteredNews.first()
				Card(
					modifier = Modifier.fillMaxWidth().height(200.dp).clickable {
						context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(featuredNews.link)))
					},
					shape = RoundedCornerShape(24.dp),
					colors = CardDefaults.cardColors(containerColor = lineageColor)
				) {
					Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
						Column {
							Box(modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
								Text(categories[selectedCategory], color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
							}
							Spacer(modifier = Modifier.height(12.dp))
							Text(featuredNews.title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
							Spacer(modifier = Modifier.weight(1f))
							Text(featuredNews.date, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
						}

						Box(
							modifier = Modifier.align(Alignment.BottomEnd).size(40.dp).background(Color.White, CircleShape),
							contentAlignment = Alignment.Center
						) {
							Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Read", tint = lineageColor)
						}
					}
				}

				Spacer(modifier = Modifier.height(32.dp))
				Text("Latest News", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
				Spacer(modifier = Modifier.height(16.dp))
			}

			val remainingNews = filteredNews.drop(1)
			items(remainingNews.size) { index ->
				val news = remainingNews[index]

				val isChangelog = news.title.contains("Changelog", ignoreCase = true)
				val changelogNumber = if (isChangelog) Regex("\\d+").find(news.title)?.value ?: "" else ""
				val isBookmarked = bookmarkedLinks.contains(news.link)

				Card(
					modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable {
						context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(news.link)))
					},
					shape = RoundedCornerShape(16.dp),
					colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
				) {
					Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {

						Box(
							modifier = Modifier.size(64.dp).background(lineageColor.copy(alpha = if (isChangelog) 1f else 0.1f), RoundedCornerShape(12.dp)),
							contentAlignment = Alignment.Center
						) {
							if (isChangelog && changelogNumber.isNotEmpty()) {
								Text(changelogNumber, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
							} else {
								Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = lineageColor, modifier = Modifier.size(32.dp))
							}
						}

						Spacer(modifier = Modifier.width(16.dp))
						Column(modifier = Modifier.weight(1f)) {
							Text(news.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
							Spacer(modifier = Modifier.height(4.dp))
							Text(news.date, color = lineageColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
						}

						Spacer(modifier = Modifier.width(8.dp))
						IconButton(onClick = {
							bookmarkedLinks = if (isBookmarked) {
								bookmarkedLinks - news.link
							} else {
								bookmarkedLinks + news.link
							}
						}) {
							Icon(
								imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
								contentDescription = "Bookmark",
								tint = if (isBookmarked) lineageColor else Color.Gray
							)
						}
					}
				}
			}
			item { Spacer(modifier = Modifier.height(24.dp)) }
		}
	}
}