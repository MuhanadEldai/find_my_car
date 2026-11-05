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
import androidx.compose.material.icons.filled.Send
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

@Composable
fun AddReportScreen(
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    var chassisNumber by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
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
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "بيانات البلاغ والسيارة",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = chassisNumber,
                    onValueChange = { chassisNumber = it },
                    label = { Text("رقم الشاسي *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = carModel,
                    onValueChange = { carModel = it },
                    label = { Text("نوع وموديل السيارة (مثال: تويوتا كامري 2023) *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it },
                    label = { Text("رقم اللوحة") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("اللون") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("اسم المالك") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف للتواصل *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("تفاصيل ومواصفات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit via WhatsApp Button directly to +249995333234 with elevation
                Button(
                    onClick = {
                        if (chassisNumber.isBlank() || carModel.isBlank() || phone.isBlank()) {
                            Toast.makeText(context, "يرجى تعبئة الحقول المطلوبة (*)", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val formattedMessage = """
                            🚨 *بلاغ سيارة جديدة للمحققين* 🚨

                            🔢 *رقم الشاسي:* $chassisNumber
                            🚘 *نوع السيارة:* $carModel
                            🏷️ *رقم اللوحة:* ${licensePlate.ifEmpty { "غير محدد" }}
                            🎨 *اللون:* ${color.ifEmpty { "غير محدد" }}
                            👤 *اسم المالك:* ${ownerName.ifEmpty { "غير محدد" }}
                            📞 *رقم التواصل:* $phone
                            📝 *التفاصيل:* ${description.ifEmpty { "لا توجد تفاصيل إضافية" }}

                  
                        """.trimIndent()

                        val whatsappUri = "https://api.whatsapp.com/send?phone=+$targetPhone&text=${
                            Uri.encode(
                                formattedMessage
                            )
                        }".toUri()
                        val intent = Intent(Intent.ACTION_VIEW, whatsappUri)

                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "تطبيق واتساب غير مثبت على الجهاز", Toast.LENGTH_LONG).show()
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
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إرسال البلاغ مباشرة عبر واتساب", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
