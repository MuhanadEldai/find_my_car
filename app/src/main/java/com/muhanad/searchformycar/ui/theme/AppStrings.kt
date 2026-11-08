package com.muhanad.searchformycar.ui.theme

object AppStrings {
    fun get(key: String, lang: String): String {
        val isAr = lang == "ar"
        return when (key) {
            // App & Navigation
            "app_name" -> if (isAr) "فتش لي عربيتي" else "Search For My Car"
            "home" -> if (isAr) "الرئيسية" else "Home"
            "add_report" -> if (isAr) "إضافة بلاغ" else "Add Report"
            "add_report_title" -> if (isAr) "إضافة بلاغ سيارة جديدة" else "Add New Car Report"
            "about_app" -> if (isAr) "حول التطبيق" else "About App"
            "settings" -> if (isAr) "الإعدادات" else "Settings"
            "back" -> if (isAr) "رجوع" else "Back"

            // Home Screen
            "search_chassis" -> if (isAr) "رقم الشاسي" else "Chassis Number"
            "enter_chassis_placeholder" -> if (isAr) "أدخل رقم الشاسي هنا..." else "Enter chassis number here..."
            "search" -> if (isAr) "بحث" else "Search"
            "total_reports" -> if (isAr) "إجمالي البلاغات" else "Total Reports"
            "missing_cars" -> if (isAr) "سيارات مفقودة" else "Missing Cars"
            "found_cars" -> if (isAr) "تم العثور عليها" else "Found Cars"
            "delivered_cars" -> if (isAr) "تم تسليمها" else "Delivered Cars"
            "missing" -> if (isAr) "مفقود" else "Missing"
            "found" -> if (isAr) "تم العثور عليها" else "Found"
            "delivered" -> if (isAr) "تم تسليمها" else "Delivered"

            "stat_desc_total" -> if (isAr) "إجمالي البلاغات المسجلة" else "Total registered reports"
            "stat_desc_missing" -> if (isAr) "سيارات ما زالت مفقودة" else "Cars still missing"
            "stat_desc_found" -> if (isAr) "تم تحديد مكانها بنجاح" else "Located successfully"
            "stat_desc_delivered" -> if (isAr) "تم تسليمها للمالك بنجاح" else "Handed over to owner"

            "status_desc_missing" -> if (isAr) "سيارة مفقودة - يرجى المساعدة في البحث" else "Missing car - please help search"
            "status_desc_found" -> if (isAr) "تم العثور على السيارة وجاري البحث عن صاحبها" else "Car found, coordinating handover"
            "status_desc_delivered" -> if (isAr) "تم تسليم السيارة لصاحبها بنجاح" else "Car successfully delivered to owner"

            "car_model" -> if (isAr) "نوع وموديل السيارة" else "Car Model"
            "license_plate" -> if (isAr) "رقم اللوحة" else "License Plate"
            "color" -> if (isAr) "لون السيارة" else "Car Color"
            "owner_name" -> if (isAr) "اسم المالك" else "Owner Name"
            "phone" -> if (isAr) "رقم الهاتف" else "Phone Number"
            "details" -> if (isAr) "تفاصيل البلاغ" else "Report Details"
            "status" -> if (isAr) "الحالة" else "Status"
            "not_specified" -> if (isAr) "غير محدد" else "Not specified"
            "not_available" -> if (isAr) "غير متوفر" else "Not available"
            "no_details" -> if (isAr) "لا توجد تفاصيل إضافية" else "No additional details"
            "chat_owner_whatsapp" -> if (isAr) "مراسلة المالك عبر واتساب" else "Message Owner via WhatsApp"
            "car_not_found" -> if (isAr) "لم يتم العثور على سيارة بهذا رقم الشاسي في قاعدة البيانات" else "No car found with this chassis number in database"
            "add_report_prompt" -> if (isAr) "هل تريد إضافة بلاغ عنها؟" else "Would you like to file a report for it?"
            "add_new_report_btn" -> if (isAr) "إضافة بلاغ جديد" else "Add New Report"
            "no_internet" -> if (isAr) "لا يوجد اتصال بالإنترنت. يرجى التحقق من الشبكة." else "No internet connection. Please check your network."
            "retry" -> if (isAr) "إعادة المحاولة" else "Retry"
            "min_chassis_warning" -> if (isAr) "يرجى إدخال 3 أحرف أو أرقام على الأقل للبحث" else "Please enter at least 3 characters/digits to search"

            // Add Report Screen
            "report_card_header" -> if (isAr) "بيانات البلاغ والسيارة" else "Car & Report Details"
            "chassis_req" -> if (isAr) "رقم الشاسي *" else "Chassis Number *"
            "model_req" -> if (isAr) "نوع وموديل السيارة (مثال: تويوتا كامري 2023) *" else "Car Model & Year (e.g. Toyota Camry 2023) *"
            "plate_label" -> if (isAr) "رقم اللوحة" else "License Plate Number"
            "color_label" -> if (isAr) "اللون" else "Color"
            "phone_req" -> if (isAr) "رقم الهاتف للتواصل *" else "Contact Phone Number *"
            "extra_details_label" -> if (isAr) "تفاصيل ومواصفات إضافية" else "Additional Details & Description"
            "send_whatsapp_report" -> if (isAr) "إرسال البلاغ مباشرة عبر واتساب" else "Send Report via WhatsApp"
            "fill_required_fields" -> if (isAr) "يرجى تعبئة الحقول المطلوبة (*)" else "Please fill in all required fields (*)"
            "whatsapp_not_installed" -> if (isAr) "تطبيق واتساب غير مثبت على الجهاز" else "WhatsApp is not installed on this device"

            // Settings Screen
            "dark_mode" -> if (isAr) "الوضع الليلي" else "Dark Mode"
            "dark_mode_desc" -> if (isAr) "تفعيل المظهر الداكن للتطبيق" else "Enable app dark theme"
            "push_notifications" -> if (isAr) "الإشعارات الفورية" else "Push Notifications"
            "push_notifications_desc" -> if (isAr) "تلقي إشعارات الحالات والبلاغات الجديدة" else "Receive instant alerts and report updates"
            "app_language" -> if (isAr) "لغة التطبيق" else "App Language"
            "notifications_enabled_msg" -> if (isAr) "تم تفعيل الإشعارات بنجاح" else "Notifications enabled successfully"
            "notifications_disabled_msg" -> if (isAr) "تم إيقاف الإشعارات" else "Notifications disabled"
            "notifications_denied_msg" -> if (isAr) "تم رفض إذن الإشعارات" else "Notification permission denied"

            // About Screen
            "about_desc_title" -> if (isAr) "عن التطبيق" else "About App"
            "about_desc" -> if (isAr) "المنصّة الأولى في السودان لاسترجاع السيارات المفقودة\n" +
                    "نؤمن أن الصدق، الأمانة، والسرعة في العمل هي أساس ثقة الناس فينا  💪\n" +
                    "هدفنا نرجّع كل عربية لأصحابها، ونكون اليد الواحدة البتساعد كل سوداني يلقى عربيتو "
            else "Official app to search for and report missing cars in Sudan easily and securely with real-time database connection."
            "contact_us" -> if (isAr) "📞 أرقام التواصل والدعم الفني" else "📞 Contact Us & Support"
            "call_help_center" -> if (isAr) "اتصال بمركز المساعدة" else "Call Help Center"
            "whatsapp_support" -> if (isAr) "واتساب الدعم الفني" else "WhatsApp"
            "support_facebook" -> if (isAr) "صفحة فتش لي عربيتي في الفيسبوك" else "Official Facebook Page"
            "app_version" -> if (isAr) "الإصدار 3.0.0 • FOUND 2024" else "Version 3.0.0 • FOUND 2024"
            "rate_app" -> if (isAr) "تقييم التطبيق على متجر بلاي" else "Rate App on Play Store"
            "check_updates" -> if (isAr) "التحقق من وجود تحديثات جديدة" else "Check for New Updates"
            "app_up_to_date" -> if (isAr) "تطبيقك محدّث لأحدث إصدار" else "Your app is up to date"
            "call_failed" -> if (isAr) "تعذر إجراء الاتصال" else "Unable to make phone call"
            "facebook_failed" -> if (isAr) "تعذر فتح صفحة الفيسبوك" else "Unable to open Facebook page"

            else -> key
        }
    }
}
