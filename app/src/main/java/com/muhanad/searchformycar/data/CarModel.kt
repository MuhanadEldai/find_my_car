package com.muhanad.searchformycar.data

import com.google.firebase.firestore.PropertyName

data class CarModel(
    val id: String = "",
    @get:PropertyName("vin") @set:PropertyName("vin") @field:PropertyName("vin") var chassisNumber: String = "",
    @get:PropertyName("mdl") @set:PropertyName("mdl") @field:PropertyName("mdl") var carModel: String = "",
    @get:PropertyName("plt") @set:PropertyName("plt") @field:PropertyName("plt") var licensePlate: String = "",
    @get:PropertyName("clr") @set:PropertyName("clr") @field:PropertyName("clr") var color: String = "",
    @get:PropertyName("ownerName") @set:PropertyName("ownerName") @field:PropertyName("ownerName") var ownerName: String = "",
    @get:PropertyName("phn") @set:PropertyName("phn") @field:PropertyName("phn") var phone: String = "",
    @get:PropertyName("dsc") @set:PropertyName("dsc") @field:PropertyName("dsc") var description: String = "",
    @get:PropertyName("sts") @set:PropertyName("sts") @field:PropertyName("sts") var status: String = "مفقود",
    @get:PropertyName("ts") @set:PropertyName("ts") @field:PropertyName("ts") var timestamp: String = "2026/10/29 • 09:15 م",
    val isFavorite: Boolean = false
)
