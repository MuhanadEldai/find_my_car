package com.muhanad.searchformycar.ui.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhanad.searchformycar.R
import com.muhanad.searchformycar.data.CarRepository
import com.muhanad.searchformycar.data.SearchResult
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@Composable
fun HomeScreen(
    repository: CarRepository,
    onSelectTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDark = isSystemInDarkTheme() || MaterialTheme.colorScheme.surface.luminance() < 0.5f
    var chassisQuery by rememberSaveable { mutableStateOf(repository.cachedSearchQuery) }
    var searchResultState by remember { 
        mutableStateOf<SearchResult?>(
            if (repository.cachedSearchQuery.isNotBlank()) SearchResult.Success(repository.cachedSearchResult) else null
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var stats by remember { mutableStateOf(repository.getCachedStatistics()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        stats = repository.getStatistics()
    }

    val performSearch: () -> Unit = {
        if (chassisQuery.isNotBlank()) {
            keyboardController?.hide()
            if (!isNetworkAvailable(context)) {
                searchResultState = SearchResult.NetworkError("لا يوجد اتصال بالإنترنت. يرجى التحقق من الشبكة.")
            } else {
                scope.launch {
                    isLoading = true
                    val result = repository.searchCar(chassisQuery)
                    searchResultState = result
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Image Card
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.image),
                    contentDescription = "Header Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Card with elevated shadow
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "رقم الشاسي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = chassisQuery,
                    onValueChange = { chassisQuery = it },
                    placeholder = {
                        Text(
                            "أدخل رقم الشاسي هنا...",
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = performSearch,
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp,
                        hoveredElevation = 8.dp
                    ),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onTertiaryFixed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("بحث", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Results handling
        when (val state = searchResultState) {
            is SearchResult.Success -> {
                if (state.car != null) {
                    val car = state.car
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Card Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge(
                                    containerColor = if (car.status == "مفقود") Color(0xFFE53935) else Color(0xFF2E7D32)
                                ) {
                                    Text(
                                        text = car.status,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = car.carModel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.CarRepair,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Structured Info Grid / Rows
                            StructuredDetailRow(label = "رقم الشاسي", value = car.chassisNumber, icon = "🔢")
                            Spacer(modifier = Modifier.height(10.dp))
                            StructuredDetailRow(label = "رقم اللوحة", value = car.licensePlate.ifEmpty { "غير محدد" }, icon = "🏷️")
                            Spacer(modifier = Modifier.height(10.dp))
                            StructuredDetailRow(label = "لون السيارة", value = car.color.ifEmpty { "غير محدد" }, icon = "🎨")
                            Spacer(modifier = Modifier.height(10.dp))
                            StructuredDetailRow(label = "اسم المالك", value = car.ownerName.ifEmpty { "غير معروف" }, icon = "👤")
                            Spacer(modifier = Modifier.height(10.dp))
                            StructuredDetailRow(label = "رقم الهاتف", value = car.phone.ifEmpty { "غير متوفر" }, icon = "📞")
                            Spacer(modifier = Modifier.height(10.dp))
                            StructuredDetailRow(label = "تفاصيل البلاغ", value = car.description.ifEmpty { "لا توجد تفاصيل إضافية" }, icon = "📝")

                            Spacer(modifier = Modifier.height(20.dp))

                            // WhatsApp Button
                            if (car.phone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val phone = car.phone.trim()
                                        val message = "مرحباً، أستفسر بخصوص سيارتك المسجلة برقم الشاسي: ${car.chassisNumber}"
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data =
                                                "https://api.whatsapp.com/send?phone=$phone&text=${
                                                    Uri.encode(message)
                                                }".toUri()
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "تطبيق واتساب غير مثبت", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Text("مراسلة المالك عبر واتساب", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    // Car not found in database -> ask to make report
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "لم يتم العثور على سيارة بهذا رقم الشاسي في قاعدة البيانات",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFE53935),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "هل تريد إضافة بلاغ عنها؟",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onSelectTab(1) },
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onTertiaryFixed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("إضافة بلاغ جديد", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
            is SearchResult.NetworkError -> {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFE53935),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = performSearch,
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعادة المحاولة", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                }
            }
            null -> {}
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Statistics Cards Row (Database counts)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "إجمالي البلاغات",
                count = "${stats.total}",
                icon = "📊"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "سيارات مفقودة",
                count = "${stats.missing}",
                icon = "⚠️"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "تم العثور عليها",
                count = "${stats.found}",
                icon = "✅"
            )
        }
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@Composable
fun StructuredDetailRow(label: String, value: String, icon: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = icon, fontSize = 16.sp)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, count: String, icon: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(count, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 15.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}
