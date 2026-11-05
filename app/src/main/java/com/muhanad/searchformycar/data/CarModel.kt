package com.muhanad.searchformycar.data

data class CarModel(
    val id: String = "",
    val chassisNumber: String = "",
    val carModel: String = "",
    val licensePlate: String = "",
    val color: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val description: String = "",
    val status: String = "مفقود",
    val timestamp: String = "2026/10/29 • 09:15 م",
    val isFavorite: Boolean = false
)
