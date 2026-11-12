package com.muhanad.searchformycar.ui.screens

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.History
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
import androidx.core.content.edit
import com.muhanad.searchformycar.R
import com.muhanad.searchformycar.data.CarRepository
import com.muhanad.searchformycar.data.SearchResult
import com.muhanad.searchformycar.data.convertArabicDigitsToEnglish
import com.muhanad.searchformycar.ui.theme.AppStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds

data class InstructionSlide(
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String,
    val descriptionEn: String
)

fun getRecentSearches(context: Context): List<String> {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val raw = prefs.getString("recent_vin_searches", "") ?: ""
    return if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
}

fun addRecentSearch(context: Context, query: String) {
    if (query.trim().length < 3) return
    val clean = query.convertArabicDigitsToEnglish().trim()
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val current = getRecentSearches(context).toMutableList()
    current.remove(clean)
    current.add(0, clean)
    val updated = current.take(5).joinToString(",")
    prefs.edit { putString("recent_vin_searches", updated) }
}

@Composable
fun HomeScreen(
    repository: CarRepository,
    language: String
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var chassisQuery by rememberSaveable { mutableStateOf(repository.cachedSearchQuery) }
    var recentSearches by remember { mutableStateOf(getRecentSearches(context)) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var animateStats by remember { mutableStateOf(false) }

    var searchResultState by remember { 
        mutableStateOf<SearchResult?>(
            if (repository.cachedSearchQuery.isNotBlank()) SearchResult.Success(listOfNotNull(repository.cachedSearchResult)) else null
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var stats by remember { mutableStateOf(repository.getCachedStatistics()) }
    val scope = rememberCoroutineScope()
    val isAr = language == "ar"

    val instructionSlides = remember {
        listOf(
            InstructionSlide(
                titleAr = "",
                titleEn = "",
                descriptionAr = " ",
                descriptionEn = ""
            ),
            InstructionSlide(
                titleAr = "",
                titleEn = "File a Missing Car Report",
                descriptionAr = "اكثر من 10000 بلاغ عن سيارة مفقودة ",
                descriptionEn = "If not found, submit a report to alert search team instantly"
            ),
            InstructionSlide(
                titleAr = "",
                titleEn = "Direct WhatsApp Support",
                descriptionAr = "اكثر من 5000 سيارة تم العثور عليها",
                descriptionEn = "Connect easily with car owner or support team in one click"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { 3 })

    // Auto-sliding Carousel Banner (slides 1 -> 2 -> 3 -> 2 -> 1, handles user touch interrupts)
    LaunchedEffect(pagerState) {
        var isForward = true
        while (true) {
            delay(5000.milliseconds)
            if (!pagerState.isScrollInProgress) {
                val current = pagerState.currentPage
                if (isForward) {
                    if (current < instructionSlides.size - 1) {
                        pagerState.animateScrollToPage(
                            page = current + 1,
                            animationSpec = tween(durationMillis = 800)
                        )
                    } else {
                        isForward = false
                        pagerState.animateScrollToPage(
                            page = current - 1,
                            animationSpec = tween(durationMillis = 800)
                        )
                    }
                } else {
                    if (current > 0) {
                        pagerState.animateScrollToPage(
                            page = current - 1,
                            animationSpec = tween(durationMillis = 800)
                        )
                    } else {
                        isForward = true
                        pagerState.animateScrollToPage(
                            page = current + 1,
                            animationSpec = tween(durationMillis = 800)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        repository.setContext(context)
        animateStats = true
        stats = repository.getStatistics(forceRefresh = false)
    }

    val performSearch: () -> Unit = {
        val query = chassisQuery.convertArabicDigitsToEnglish().trim()
        if (query.isNotBlank()) {
            if (query.length < 3) {
                Toast.makeText(context, AppStrings.get("min_chassis_warning", language), Toast.LENGTH_SHORT).show()
            } else {
                keyboardController?.hide()
                addRecentSearch(context, query)
                recentSearches = getRecentSearches(context)
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
            .padding(vertical = 16.dp)
    ) {
        // Auto-Sliding Instruction Header Carousel with 3 Dots ViewPager Indicator (Edge-to-Edge Container)
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) { page ->
                val slide = instructionSlides[page]
                val imageRes = when (page) {
                    0 -> R.drawable.image
                    1 -> R.drawable.header2
                    2 -> R.drawable.header3
                    else -> R.drawable.image
                }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = "Header Instruction Slide $page",
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
                                            Color.Black.copy(alpha = 0.65f)
                                        )
                                    )
                                )
                        )
                        if (page == 0) {
                            val descText = if (isAr) slide.descriptionAr else slide.descriptionEn
                            if (descText.isNotBlank()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.Bottom,
                                    horizontalAlignment = if (isAr) Alignment.CenterHorizontally else Alignment.Start
                                ) {
                                    Text(
                                        text = descText,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = if (isAr) TextAlign.Center else TextAlign.Left,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
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

        // Main Screen Content Section with 16.dp horizontal padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
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

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = chassisQuery,
                            onValueChange = { chassisQuery = it.convertArabicDigitsToEnglish() },
                            placeholder = {
                                Text(
                                    AppStrings.get("enter_chassis_placeholder", language),
                                    textAlign = if (isAr) TextAlign.Right else TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            trailingIcon = {
                                if (recentSearches.isNotEmpty()) {
                                    IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "Recent Searches",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(textAlign = if (isAr) TextAlign.Right else TextAlign.Left)
                        )

                        DropdownMenu(
                            expanded = isDropdownExpanded && recentSearches.isNotEmpty(),
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            recentSearches.forEach { recent ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = recent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = if (isAr) "بحث سابق" else "Recent",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    onClick = {
                                        chassisQuery = recent
                                        isDropdownExpanded = false
                                        performSearch()
                                    }
                                )
                            }
                        }
                    }

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
                    if (state.cars.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (car in state.cars) {
                                val (statusText, badgeColor, statusDesc) = when (car.status) {
                                    "مفقود" -> Triple(AppStrings.get("missing", language), Color(0xFFE53935), AppStrings.get("status_desc_missing", language))
                                    "تم العثور عليها" -> Triple(AppStrings.get("found", language), Color(0xFF2E7D32), AppStrings.get("status_desc_found", language))
                                    "تم تسليمها" -> Triple(AppStrings.get("delivered", language), Color(0xFF1976D2), AppStrings.get("status_desc_delivered", language))
                                    else -> Triple(AppStrings.get("missing", language), Color(0xFFE53935), AppStrings.get("status_desc_missing", language))
                                }

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
                                                containerColor = badgeColor
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

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = statusDesc,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            textAlign = if (isAr) TextAlign.Right else TextAlign.Left,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Details
                                        StructuredDetailRow(
                                            label = AppStrings.get("chassis_number", language),
                                            value = car.chassisNumber,
                                            icon = "",
                                            isAr = isAr
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        StructuredDetailRow(
                                            label = AppStrings.get("license_plate", language),
                                            value = car.licensePlate.ifEmpty { AppStrings.get("not_specified", language) },
                                            icon = "",
                                            isAr = isAr
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        StructuredDetailRow(
                                            label = AppStrings.get("color", language),
                                            value = car.color.ifEmpty { AppStrings.get("not_specified", language) },
                                            icon = "",
                                            isAr = isAr
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        StructuredDetailRow(
                                            label = AppStrings.get("phone", language),
                                            value = car.phone.ifEmpty { AppStrings.get("not_available", language) },
                                            icon = "",
                                            isAr = isAr
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        StructuredDetailRow(
                                            label = AppStrings.get("details", language),
                                            value = car.description.ifEmpty { AppStrings.get("no_details", language) },
                                            icon = "",
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
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp)
                                            ) {
                                                Text(AppStrings.get("chat_owner_whatsapp", language), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Car not found -> Direct WhatsApp Support Card
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
                                    text = if (isAr) "للاستفسار أو الإبلاغ الفوري، تواصل معنا مباشرة عبر واتساب" else "For inquiry or immediate reporting, contact us via WhatsApp",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        val targetPhone = "+249969904987"
                                        val message = if (isAr) {
                                            "مرحباً، أبحث عن سيارة برقم الشاسي: $chassisQuery ولم أجدها في قاعدة البيانات وأريد الإبلاغ عنها."
                                        } else {
                                            "Hello, I searched for car with chassis: $chassisQuery, it was not found in database and I want to report it."
                                        }
                                        val uri = "https://api.whatsapp.com/send?phone=$targetPhone&text=${Uri.encode(message)}".toUri()
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, AppStrings.get("whatsapp_not_installed", language), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Text(
                                        text = if (isAr) "إبلاغ والاستفسار عبر واتساب" else "Inquire & Report via WhatsApp",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
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

            // Statistics Cards (2x2 Grid for 4 States)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.get("total_reports", language),
                        count = stats.total,
                        startAnimation = animateStats,
                        description = AppStrings.get("stat_desc_total", language)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.get("missing_cars", language),
                        count = stats.missing,
                        startAnimation = animateStats,
                        description = AppStrings.get("stat_desc_missing", language)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.get("found_cars", language),
                        count = stats.found,
                        startAnimation = animateStats,
                        description = AppStrings.get("stat_desc_found", language)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = AppStrings.get("delivered_cars", language),
                        count = stats.delivered,
                        startAnimation = animateStats,
                        description = AppStrings.get("stat_desc_delivered", language)
                    )
                }
            }
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
fun StatCard(modifier: Modifier = Modifier, title: String, count: Int, startAnimation: Boolean, description: String) {
    val animatedCount by animateIntAsState(
        targetValue = if (startAnimation) count else 0,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "count_animation"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(115.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("$animatedCount", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}
