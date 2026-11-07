package com.muhanad.searchformycar.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.muhanad.searchformycar.ui.theme.AppStrings

@Composable
fun AddReportScreen(
    language: String = "ar"
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isAr = language == "ar"
    
    var chassisNumber by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val targetPhone = "249995333234"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Report Details Input Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = if (isAr) Alignment.End else Alignment.Start
            ) {
                Text(
                    AppStrings.get("report_card_header", language),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = chassisNumber,
                    onValueChange = { chassisNumber = it },
                    label = { Text(AppStrings.get("chassis_req", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = if (isAr) TextAlign.Right else TextAlign.Left)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = carModel,
                    onValueChange = { carModel = it },
                    label = { Text(AppStrings.get("model_req", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = if (isAr) TextAlign.Right else TextAlign.Left)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it },
                    label = { Text(AppStrings.get("plate_label", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = if (isAr) TextAlign.Right else TextAlign.Left)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text(AppStrings.get("color_label", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = if (isAr) TextAlign.Right else TextAlign.Left)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(AppStrings.get("phone_req", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = if (isAr) TextAlign.Right else TextAlign.Left)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(AppStrings.get("extra_details_label", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    textStyle = LocalTextStyle.current.copy(textAlign = if (isAr) TextAlign.Right else TextAlign.Left)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (chassisNumber.isBlank() || carModel.isBlank() || phone.isBlank()) {
                            Toast.makeText(context, AppStrings.get("fill_required_fields", language), Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val formattedMessage = if (isAr) {
                            """
                                🚨 *بلاغ سيارة جديدة للمحققين* 🚨

                                🔢 *رقم الشاسي:* $chassisNumber
                                🚘 *نوع السيارة:* $carModel
                                🏷️ *رقم اللوحة:* ${licensePlate.ifEmpty { "غير محدد" }}
                                🎨 *اللون:* ${color.ifEmpty { "غير محدد" }}
                                📞 *رقم التواصل:* $phone
                                📝 *التفاصيل:* ${description.ifEmpty { "لا توجد تفاصيل إضافية" }}
                            """.trimIndent()
                        } else {
                            """
                                🚨 *New Missing Car Report* 🚨

                                🔢 *Chassis Number:* $chassisNumber
                                🚘 *Car Model:* $carModel
                                🏷️ *License Plate:* ${licensePlate.ifEmpty { "Not specified" }}
                                🎨 *Color:* ${color.ifEmpty { "Not specified" }}
                                📞 *Contact Phone:* $phone
                                📝 *Details:* ${description.ifEmpty { "No additional details" }}
                            """.trimIndent()
                        }

                        val whatsappUri = "https://api.whatsapp.com/send?phone=+$targetPhone&text=${Uri.encode(formattedMessage)}".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, whatsappUri)

                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, AppStrings.get("whatsapp_not_installed", language), Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp,
                        hoveredElevation = 10.dp
                    ),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppStrings.get("send_whatsapp_report", language), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
