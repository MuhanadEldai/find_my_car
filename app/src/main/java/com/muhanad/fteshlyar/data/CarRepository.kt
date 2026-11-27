package com.muhanad.fteshlyar.data

import android.content.Context
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

sealed class SearchResult {
    data class Success(val cars: List<CarModel>) : SearchResult()
    data class NetworkError(val message: String) : SearchResult()
}

data class CarStats(val total: Int, val missing: Int, val found: Int, val delivered: Int)

class CarRepository(context: Context? = null) {
    private var appContext: Context? = context?.applicationContext

    fun setContext(context: Context) {
        appContext = context.applicationContext
        loadStatsFromPrefs()
    }

    var cachedSearchQuery: String = ""
    var cachedSearchResult: CarModel? = null

    private var cachedStats: CarStats? = null
    private var lastStatsFetchTime: Long = 0
    private val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours TTL (1 time per day)

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    private val mockCars = mutableListOf(
        CarModel(
            id = "1",
            chassisNumber = "JH4KA3760MC123456",
            carModel = "تويوتا كامري 2023",
            licensePlate = "خرطوم 12345",
            color = "أبيض",
            phone = "+249912345678",
            description = "سيارة مسروقة من منطقة الرياض",
            status = "مفقود"
        ),
        CarModel(
            id = "2",
            chassisNumber = "KMHDH4AE8FU987654",
            carModel = "هيونداي النترا 2021",
            licensePlate = "أمدرمان 67890",
            color = "فضي",
            phone = "+249987654321",
            description = "فقدت بالقرب من شارع الهجرة",
            status = "تم تسليمها"
        )
    )

    init {
        loadStatsFromPrefs()
    }

    private fun saveStatsToPrefs(stats: CarStats) {
        try {
            appContext?.getSharedPreferences("offline_car_stats", Context.MODE_PRIVATE)?.edit()?.apply {
                putInt("total", stats.total)
                putInt("missing", stats.missing)
                putInt("found", stats.found)
                putInt("delivered", stats.delivered)
                putLong("fetch_time", System.currentTimeMillis())
                apply()
            }
        } catch (_: Exception) {}
    }

    private fun loadStatsFromPrefs(): CarStats? {
        try {
            val prefs = appContext?.getSharedPreferences("offline_car_stats", Context.MODE_PRIVATE) ?: return null
            if (prefs.contains("total")) {
                val total = prefs.getInt("total", 0)
                val missing = prefs.getInt("missing", 0)
                val found = prefs.getInt("found", 0)
                val delivered = prefs.getInt("delivered", 0)
                val fetchTime = prefs.getLong("fetch_time", 0L)
                val stats = CarStats(total = total, missing = missing, found = found, delivered = delivered)
                cachedStats = stats
                if (fetchTime > 0) {
                    lastStatsFetchTime = fetchTime
                }
                return stats
            }
        } catch (_: Exception) {}
        return null
    }

    fun getCachedStatistics(): CarStats {
        return cachedStats ?: loadStatsFromPrefs() ?: CarStats(120, 70, 30, 20)
    }

    /**
     * Efficiently fetches statistics using Firestore native aggregation `count()` queries.
     * Caches result for 24 hours to drastically reduce data usage and network reads.
     * Persists results locally in SharedPreferences for offline usage.
     */
    suspend fun getStatistics(forceRefresh: Boolean = false): CarStats {
        val loaded = cachedStats ?: loadStatsFromPrefs()
        val currentTime = System.currentTimeMillis()

        // Return cached statistics immediately if within cache TTL (0 Firestore reads)
        if (!forceRefresh && loaded != null && lastStatsFetchTime > 0 && (currentTime - lastStatsFetchTime) < CACHE_DURATION_MS) {
            cachedStats = loaded
            return loaded
        }

        try {
            firestore?.let { db ->
                // Native Firestore count() aggregation transfers only bytes, no full documents
                val totalSnapshot = withTimeoutOrNull(6000.milliseconds) {
                    db.collection("cars").count().get(AggregateSource.SERVER).await()
                } ?: return@let

                val total = totalSnapshot.count.toInt()

                val missingSnapshot = db.collection("cars")
                    .whereEqualTo("sts", "مفقود")
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                val missing = missingSnapshot.count.toInt()

                val foundSnapshot = db.collection("cars")
                    .whereEqualTo("sts", "تم العثور عليها")
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                val found = foundSnapshot.count.toInt()

                val deliveredSnapshot = db.collection("cars")
                    .whereEqualTo("sts", "تم تسليمها")
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                val delivered = deliveredSnapshot.count.toInt()

                val stats = CarStats(total = total, missing = missing, found = found, delivered = delivered)
                cachedStats = stats
                lastStatsFetchTime = currentTime
                saveStatsToPrefs(stats)
                return stats
            }
        } catch (_: Exception) {
            // fallback if offline/error
        }

        if (loaded != null) {
            cachedStats = loaded
            return loaded
        }

        val totalMock = mockCars.size
        val missingMock = mockCars.count { it.status == "مفقود" }
        val foundMock = mockCars.count { it.status == "تم العثور عليها" }
        val deliveredMock = mockCars.count { it.status == "تم تسليمها" }
        val fallback = CarStats(total = totalMock + 120, missing = missingMock + 70, found = foundMock + 30, delivered = deliveredMock + 20)
        cachedStats = fallback
        return fallback
    }

    suspend fun searchCar(chassis: String): SearchResult {
        val query = chassis.convertArabicDigitsToEnglish().trim()
        cachedSearchQuery = query
        if (query.length < 3) {
            cachedSearchResult = null
            return SearchResult.Success(emptyList())
        }

        val results = mutableListOf<CarModel>()
        val queryDigits = query.filter { it.isDigit() }

        try {
            val db = firestore
            if (db == null) {
                val mockMatches = mockCars.filter { car ->
                    car.chassisNumber.equals(query, ignoreCase = true) ||
                    car.id.equals(query, ignoreCase = true) ||
                    (queryDigits.length >= 3 && car.licensePlate.filter { it.isDigit() }.contains(queryDigits))
                }
                return SearchResult.Success(mockMatches)
            }

            // 1. Targeted query on "vin" field
            val vinSnapshot = withTimeoutOrNull(6000.milliseconds) {
                db.collection("cars")
                    .whereEqualTo("vin", query)
                    .get()
                    .await()
            }

            if (vinSnapshot == null) {
                return SearchResult.NetworkError("لا يوجد اتصال بالإنترنت. يرجى التحقق من الشبكة.")
            }

            for (doc in vinSnapshot.documents) {
                val car = parseCarDoc(doc, query)
                if (results.none { it.id == car.id }) {
                    results.add(car)
                }
            }

            // 2. If not found in "vin", check legacy "chassisNumber" field
            if (results.isEmpty()) {
                val chassisSnapshot = withTimeoutOrNull(6000.milliseconds) {
                    db.collection("cars")
                        .whereEqualTo("chassisNumber", query)
                        .get()
                        .await()
                }
                chassisSnapshot?.documents?.forEach { doc ->
                    val car = parseCarDoc(doc, query)
                    if (results.none { it.id == car.id }) {
                        results.add(car)
                    }
                }
            }

            // 3. If not found, check document ID directly
            if (results.isEmpty()) {
                val docRef = withTimeoutOrNull(6000.milliseconds) {
                    db.collection("cars").document(query).get().await()
                }
                if (docRef != null && docRef.exists()) {
                    val car = parseCarDoc(docRef, query)
                    if (results.none { it.id == car.id }) {
                        results.add(car)
                    }
                }
            }

            // 4. If VIN doesn't match and query has digits, fallback to searching by license plate digits (without letters)
            if (results.isEmpty() && queryDigits.length >= 3) {
                val allSnapshot = withTimeoutOrNull(6000.milliseconds) {
                    db.collection("cars").get().await()
                }
                allSnapshot?.documents?.forEach { doc ->
                    val plate = doc.getString("plt") ?: doc.getString("licensePlate") ?: ""
                    val plateDigits = plate.filter { it.isDigit() }
                    if (plateDigits.isNotEmpty() && plateDigits.contains(queryDigits)) {
                        val car = parseCarDoc(doc, query)
                        if (results.none { it.id == car.id }) {
                            results.add(car)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return SearchResult.NetworkError(e.localizedMessage ?: "حدث خطأ في الاتصال بالشبكة")
        }

        // Also search in mock list with VIN or license plate digits fallback
        val mockMatches = mockCars.filter { car ->
            car.chassisNumber.equals(query, ignoreCase = true) ||
            car.id.equals(query, ignoreCase = true) ||
            (queryDigits.length >= 3 && car.licensePlate.filter { it.isDigit() }.contains(queryDigits))
        }
        for (mock in mockMatches) {
            if (results.none { it.id == mock.id && it.chassisNumber.equals(mock.chassisNumber, ignoreCase = true) }) {
                results.add(mock)
            }
        }

        cachedSearchResult = results.firstOrNull()
        return SearchResult.Success(results)
    }

    private fun parseCarDoc(doc: DocumentSnapshot, query: String): CarModel {
        return CarModel(
            id = "",
            chassisNumber = doc.getString("vin") ?: doc.getString("chassisNumber") ?: query,
            carModel = doc.getString("mdl") ?: doc.getString("carModel") ?: "غير معروف",
            licensePlate = "",
            color = "",
            phone = "",
            description = doc.getString("dsc") ?: doc.getString("description") ?: "",
            status = doc.getString("sts") ?: doc.getString("status") ?: "مفقود",
            timestamp = ""
        )
    }

}

fun String.convertArabicDigitsToEnglish(): String {
    return this.map { char ->
        when (char) {
            '٠', '۰' -> '0'
            '١', '۱' -> '1'
            '٢', '۲' -> '2'
            '٣', '۳' -> '3'
            '٤', '۴' -> '4'
            '٥', '۵' -> '5'
            '٦', '۶' -> '6'
            '٧', '۷' -> '7'
            '٨', '۸' -> '8'
            '٩', '۹' -> '9'
            else -> char
        }
    }.joinToString("")
}
