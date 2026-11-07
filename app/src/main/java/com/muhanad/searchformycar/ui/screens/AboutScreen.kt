package com.muhanad.searchformycar.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import androidx.core.net.toUri
import com.muhanad.searchformycar.R
import com.muhanad.searchformycar.ui.theme.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    language: String = "ar",
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isAr = language == "ar"

    val primarySupportPhone = "+249928024419"
    val whatsappSupportPhone = "+249928024419"
    val supportEmail = "support@searchformycar.com"

    val aboutContent = @Composable { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo Header with Shadow
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.image),
                    contentDescription = "App Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = AppStrings.get("app_version", language),
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // About Application Description Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = if (isAr) Alignment.End else Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (isAr) Arrangement.End else Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!isAr) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            AppStrings.get("about_desc_title", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isAr) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        AppStrings.get("about_desc", language),
                        textAlign = if (isAr) TextAlign.Right else TextAlign.Left,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Us & Technical Support Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = if (isAr) Alignment.End else Alignment.Start
                ) {
                    Text(
                        AppStrings.get("contact_us", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Direct Phone Call Button
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, "tel:$primarySupportPhone".toUri())
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, AppStrings.get("call_failed", language), Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isAr) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppStrings.get("call_help_center", language), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(primarySupportPhone, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            } else {
                                Text(primarySupportPhone, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(AppStrings.get("call_help_center", language), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // WhatsApp Support Chat Button
                    Button(
                        onClick = {
                            val message = if (isAr) "مرحباً، أستفسر بخصوص تطبيق فتش لي عربيتي" else "Hello, inquiring about Search For My Car app"
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://api.whatsapp.com/send?phone=$whatsappSupportPhone&text=${Uri.encode(message)}".toUri()
                            )
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, AppStrings.get("whatsapp_not_installed", language), Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp,
                            hoveredElevation = 8.dp
                        ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isAr) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppStrings.get("whatsapp_support", language), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                }
                                Text(whatsappSupportPhone, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            } else {
                                Text(whatsappSupportPhone, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(AppStrings.get("whatsapp_support", language), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email Support Button
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, "mailto:$supportEmail".toUri())
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, AppStrings.get("email_failed", language), Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isAr) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppStrings.get("support_email", language), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                                Text(supportEmail, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            } else {
                                Text(supportEmail, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(AppStrings.get("support_email", language), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Actions Card (Rating & Updates)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Rate App Button
                    Button(
                        onClick = {
                            val reviewManager = ReviewManagerFactory.create(context)
                            reviewManager.requestReviewFlow().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val reviewInfo = task.result
                                    if (context is Activity) {
                                        reviewManager.launchReviewFlow(context, reviewInfo)
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
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
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppStrings.get("rate_app", language), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // In-App Update Check Button
                    OutlinedButton(
                        onClick = {
                            val appUpdateManager = AppUpdateManagerFactory.create(context)
                            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                                ) {
                                    if (context is Activity) {
                                        appUpdateManager.startUpdateFlow(
                                            appUpdateInfo,
                                            context,
                                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                                        )
                                    }
                                } else {
                                    Toast.makeText(context, AppStrings.get("app_up_to_date", language), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppStrings.get("check_updates", language), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    if (onBack != null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    color = MaterialTheme.colorScheme.scrim,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                AppStrings.get("about_app", language),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Color.White,
                                textAlign = if (isAr) TextAlign.Right else TextAlign.Left,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = AppStrings.get("back", language),
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        ) { innerPadding ->
            aboutContent(innerPadding)
        }
    } else {
        aboutContent(PaddingValues(0.dp))
    }
}
