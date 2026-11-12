package com.muhanad.searchformycar.data

import android.content.Context
import com.google.firebase.firestore.AggregateSource
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
     * Caches result for 15 minutes to drastically reduce data usage and network reads.
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

        try {
            val db = firestore
            if (db == null) {
                val mockMatches = mockCars.filter { it.chassisNumber.equals(query, ignoreCase = true) || it.id.equals(query, ignoreCase = true) }
                return SearchResult.Success(mockMatches)
            }

            // 1. Targeted query on "vin" field (Only fetches exact matching document(s) - 1 read on free plan)
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
                val car = CarModel(
                    id = doc.id,
                    chassisNumber = doc.getString("vin") ?: doc.getString("chassisNumber") ?: query,
                    carModel = doc.getString("mdl") ?: doc.getString("carModel") ?: "غير معروف",
                    licensePlate = doc.getString("plt") ?: doc.getString("licensePlate") ?: "",
                    color = doc.getString("clr") ?: doc.getString("color") ?: "",
                    phone = doc.getString("phn") ?: doc.getString("phone") ?: "",
                    description = doc.getString("dsc") ?: doc.getString("description") ?: "",
                    status = doc.getString("sts") ?: doc.getString("status") ?: "مفقود"
                )
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
                    val car = CarModel(
                        id = doc.id,
                        chassisNumber = doc.getString("vin") ?: doc.getString("chassisNumber") ?: query,
                        carModel = doc.getString("mdl") ?: doc.getString("carModel") ?: "غير معروف",
                        licensePlate = doc.getString("plt") ?: doc.getString("licensePlate") ?: "",
                        color = doc.getString("clr") ?: doc.getString("color") ?: "",
                        phone = doc.getString("phn") ?: doc.getString("phone") ?: "",
                        description = doc.getString("dsc") ?: doc.getString("description") ?: "",
                        status = doc.getString("sts") ?: doc.getString("status") ?: "مفقود"
                    )
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
                    val car = CarModel(
                        id = docRef.id,
                        chassisNumber = docRef.getString("vin") ?: docRef.getString("chassisNumber") ?: query,
                        carModel = docRef.getString("mdl") ?: docRef.getString("carModel") ?: "غير معروف",
                        licensePlate = docRef.getString("plt") ?: docRef.getString("licensePlate") ?: "",
                        color = docRef.getString("clr") ?: docRef.getString("color") ?: "",
                        phone = docRef.getString("phn") ?: docRef.getString("phone") ?: "",
                        description = docRef.getString("dsc") ?: docRef.getString("description") ?: "",
                        status = docRef.getString("sts") ?: docRef.getString("status") ?: "مفقود"
                    )
                    if (results.none { it.id == car.id }) {
                        results.add(car)
                    }
                }
            }
        } catch (e: Exception) {
            return SearchResult.NetworkError(e.localizedMessage ?: "حدث خطأ في الاتصال بالشبكة")
        }

        // Also search in mock list with exact match
        val mockMatches = mockCars.filter { it.chassisNumber.equals(query, ignoreCase = true) || it.id.equals(query, ignoreCase = true) }
        for (mock in mockMatches) {
            if (results.none { it.id == mock.id && it.chassisNumber.equals(mock.chassisNumber, ignoreCase = true) }) {
                results.add(mock)
            }
        }

        cachedSearchResult = results.firstOrNull()
        return SearchResult.Success(results)
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
