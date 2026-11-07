package com.muhanad.searchformycar.ui.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.muhanad.searchformycar.ui.theme.AppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds

data class InstructionSlide(
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val icon: String
)

@Composable
fun HomeScreen(
    repository: CarRepository,
    isDarkMode: Boolean,
    language: String,
    onSelectTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var chassisQuery by rememberSaveable { mutableStateOf(repository.cachedSearchQuery) }
    var searchResultState by remember { 
        mutableStateOf<SearchResult?>(
            if (repository.cachedSearchQuery.isNotBlank()) SearchResult.Success(repository.cachedSearchResult) else null
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var stats by remember { mutableStateOf(repository.getCachedStatistics()) }
    val scope = rememberCoroutineScope()
    val isAr = language == "ar"

    val instructionSlides = remember {
        listOf(
            InstructionSlide(
                titleAr = "ابحث برقم الشاسي",
                titleEn = "Search by Chassis",
                descriptionAr = "أدخل رقم الشاسي فوراً للتحقق من حالة السيارة وتفاصيلها في قاعدة البيانات",
                descriptionEn = "Enter chassis number to instantly check car status in database",
                icon = "🔎"
            ),
            InstructionSlide(
                titleAr = "أضف بلاغ سيارة مفقودة",
                titleEn = "File a Missing Car Report",
                descriptionAr = "في حال عدم العثور عليها، قدم بلاغاً جديداً ليصل إلى فريق البحث فوراً",
                descriptionEn = "If not found, submit a report to alert search team instantly",
                icon = "📝"
            ),
            InstructionSlide(
                titleAr = "تواصل مباشر عبر واتساب",
                titleEn = "Direct WhatsApp Support",
                descriptionAr = "تواصل بسهولة وأمان مع مالك السيارة أو فريق الدعم بنقرة واحدة",
                descriptionEn = "Connect easily with car owner or support team in one click",
                icon = "💬"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { 3 })

    // Auto-sliding Carousel Banner (slides every 4 seconds)
    LaunchedEffect(pagerState) {
        while (true) {
            delay(4000.milliseconds)
            val nextPage = (pagerState.currentPage + 1) % instructionSlides.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    LaunchedEffect(Unit) {
        repository.setContext(context)
        stats = repository.getStatistics()
    }

    val performSearch: () -> Unit = {
        val query = chassisQuery.trim()
        if (query.isNotBlank()) {
            if (query.length < 3) {
                Toast.makeText(context, AppStrings.get("min_chassis_warning", language), Toast.LENGTH_SHORT).show()
            } else {
                keyboardController?.hide()
                if (!isNetworkAvailable(context)) {
                    searchResultState = SearchResult.NetworkError(AppStrings.get("no_internet", language))
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
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Auto-Sliding Instruction Header Carousel with 3 Dots ViewPager Indicator
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) { page ->
                val slide = instructionSlides[page]
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.image),
                            contentDescription = "Header Instruction Slide",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.80f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = if (isAr) Alignment.End else Alignment.Start
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = if (isAr) Arrangement.End else Arrangement.Start
                            ) {
                                if (!isAr) {
                                    Text(slide.icon, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = if (isAr) slide.titleAr else slide.titleEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = Color.White
                                )
                                if (isAr) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(slide.icon, fontSize = 22.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr) slide.descriptionAr else slide.descriptionEn,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.95f),
                                textAlign = if (isAr) TextAlign.Right else TextAlign.Left,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Dots ViewPager Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(instructionSlides.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        label = "indicator_width"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isAr) Arrangement.End else Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!isAr) {
                        Icon(
                            imageVector = Icons.Default.CarRepair,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = AppStrings.get("search_chassis", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isAr) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CarRepair,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = chassisQuery,
                    onValueChange = { chassisQuery = it },
                    placeholder = {
                        Text(
                            AppStrings.get("enter_chassis_placeholder", language),
                            textAlign = if (isAr) TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(textAlign = if (isAr) TextAlign.Right else TextAlign.Left)
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
                        Text(AppStrings.get("search", language), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                    val statusText = if (car.status == "مفقود") AppStrings.get("missing", language) else AppStrings.get("found", language)
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
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
                                        text = statusText,
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

                            // Details
                            StructuredDetailRow(
                                label = AppStrings.get("chassis_number", language),
                                value = car.chassisNumber,
                                icon = "🔢",
                                isAr = isAr
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            StructuredDetailRow(
                                label = AppStrings.get("license_plate", language),
                                value = car.licensePlate.ifEmpty { AppStrings.get("not_specified", language) },
                                icon = "🏷️",
                                isAr = isAr
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            StructuredDetailRow(
                                label = AppStrings.get("color", language),
                                value = car.color.ifEmpty { AppStrings.get("not_specified", language) },
                                icon = "🎨",
                                isAr = isAr
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            StructuredDetailRow(
                                label = AppStrings.get("phone", language),
                                value = car.phone.ifEmpty { AppStrings.get("not_available", language) },
                                icon = "📞",
                                isAr = isAr
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            StructuredDetailRow(
                                label = AppStrings.get("details", language),
                                value = car.description.ifEmpty { AppStrings.get("no_details", language) },
                                icon = "📝",
                                isAr = isAr
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // WhatsApp Button
                            if (car.phone.isNotBlank()) {
                                Button(
                                    onClick = {
                                        val phone = car.phone.trim()
                                        val message = if (isAr) {
                                            "مرحباً، أستفسر بخصوص سيارتك المسجلة برقم الشاسي: ${car.chassisNumber}"
                                        } else {
                                            "Hello, inquiring about your car registered with chassis number: ${car.chassisNumber}"
                                        }
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = "https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(message)}".toUri()
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, AppStrings.get("whatsapp_not_installed", language), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Text(AppStrings.get("chat_owner_whatsapp", language), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    // Car not found
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
                                text = AppStrings.get("car_not_found", language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFE53935),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = AppStrings.get("add_report_prompt", language),
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
                                Text(AppStrings.get("add_new_report_btn", language), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
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
                            Text(AppStrings.get("retry", language), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }
                }
            }
            null -> {}
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Statistics Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = AppStrings.get("total_reports", language),
                count = "${stats.total}",
                icon = "📊"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = AppStrings.get("missing_cars", language),
                count = "${stats.missing}",
                icon = "⚠️"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = AppStrings.get("found_cars", language),
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
fun StructuredDetailRow(label: String, value: String, icon: String, isAr: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isAr) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Text(
                text = value,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )
        } else {
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
            Text(title, fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}
