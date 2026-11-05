package com.muhanad.searchformycar.ui.theme

object AppStrings {
    fun get(key: String, lang: String): String {
        val isAr = lang == "ar"
        return when (key) {
            "app_name" -> if (isAr) "فتش لي عربيتي" else "Search For My Car"
            "home" -> if (isAr) "الرئيسية" else "Home"
            "add_report" -> if (isAr) "إضافة بلاغ" else "Add Report"
            "add_report_title" -> if (isAr) "إضافة بلاغ سيارة جديدة" else "Add New Car Report"
            "about_app" -> if (isAr) "حول التطبيق" else "About App"
            "settings" -> if (isAr) "الإعدادات" else "Settings"
            "search_chassis" -> if (isAr) "رقم الشاسي" else "Chassis Number"
            "enter_chassis_placeholder" -> if (isAr) "أدخل رقم الشاسي هنا..." else "Enter chassis number here..."
            "search" -> if (isAr) "بحث" else "Search"
            "total_reports" -> if (isAr) "إجمالي البلاغات" else "Total Reports"
            "missing_cars" -> if (isAr) "سيارات مفقودة" else "Missing Cars"
            "found_cars" -> if (isAr) "تم العثور عليها" else "Found Cars"
            "car_model" -> if (isAr) "نوع وموديل السيارة" else "Car Model"
            "license_plate" -> if (isAr) "رقم اللوحة" else "License Plate"
            "color" -> if (isAr) "اللون" else "Color"
            "owner_name" -> if (isAr) "اسم المالك" else "Owner Name"
            "phone" -> if (isAr) "رقم الهاتف للتواصل" else "Phone Number"
            "details" -> if (isAr) "تفاصيل ومواصفات إضافية" else "Additional Details"
            "send_whatsapp" -> if (isAr) "إرسال البلاغ مباشرة عبر واتساب" else "Send Report via WhatsApp"
            "dark_mode" -> if (isAr) "الوضع الليلي (Dark Mode)" else "Dark Mode"
            "push_notifications" -> if (isAr) "الإشعارات الفورية (Push Notifications)" else "Push Notifications"
            "app_language" -> if (isAr) "لغة التطبيق (App Language)" else "App Language"
            "contact_us" -> if (isAr) "📞 أرقام التواصل والدعم الفني" else "📞 Contact Us & Support"
            "back" -> if (isAr) "رجوع" else "Back"
            else -> key
        }
    }
}
