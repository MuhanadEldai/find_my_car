package com.muhanad.searchformycar.data

import android.content.Context
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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
    private val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes cache TTL

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
                val stats = CarStats(total = total, missing = missing, found = found, delivered = delivered)
                cachedStats = stats
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
     * Caches result for 5 minutes to drastically reduce data usage and network reads.
     * Persists results locally in SharedPreferences for offline usage.
     */
    suspend fun getStatistics(forceRefresh: Boolean = false): CarStats {
        val currentTime = System.currentTimeMillis()
        if (!forceRefresh && cachedStats != null && lastStatsFetchTime > 0 && (currentTime - lastStatsFetchTime) < CACHE_DURATION_MS) {
            return cachedStats!!
        }

        try {
            firestore?.let { db ->
                // Native Firestore count() aggregation transfers only bytes, no full documents
                val totalSnapshot = db.collection("cars").count().get(AggregateSource.SERVER).await()
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

        val loaded = cachedStats ?: loadStatsFromPrefs()
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
        val query = chassis.trim()
        cachedSearchQuery = query
        if (query.length < 3) {
            cachedSearchResult = null
            return SearchResult.Success(emptyList())
        }

        val results = mutableListOf<CarModel>()

        try {
            firestore?.let { db ->
                val snapshot = db.collection("cars")
                    .get()
                    .await()
                
                val matchedDocs = snapshot.documents.filter { doc ->
                    val vin = doc.getString("vin") ?: doc.getString("chassisNumber") ?: ""
                    val docId = doc.id
                    vin.equals(query, ignoreCase = true) || docId.equals(query, ignoreCase = true)
                }

                for (doc in matchedDocs) {
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
                    results.add(car)
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
