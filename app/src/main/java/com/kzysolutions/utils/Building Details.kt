package com.kzysolutions.utils

data class BuildingDetails(
    val building: String = "",
    val address: String = "",
    val city: String = "",
    val pincode: Int = 0,
    val upi: String = "",
    val maintenance: Int = 0,
    val imageUrl: String = "",
    val adminUID: String = ""
)

data class FlatDetails(
    val admin: String = "",
    val adminUid: String = "",
    val buildingId: String = "",
    val flatNumber: String = "",
    val netDue: Long = 0,
    val wingNumber: String = ""
)

data class BuildingDetailsSC(
    val building: String = "",
    val address: String = "",
    val imageUrl: String = "",
    val adminUID: String = ""
)

data class UserFlatDetails(
    val name: String,
    val phone: String,
    val flatNumber: String,
    val wingNumber: String
)

data class PayItemData(
    val imageUrl: String = "",
    val title: String = "",
    val subtitle: String = "",
    val id: String = "",
    val uid: String = "",
    val flatNumber: String = "",
    val wingNumber: String = "",
    val isPaid: Boolean = false
)

data class FlatDetailsPayments(
    val flatNumber: String = "",
    val wingNumber: String = "",
    val netDue: Int = 0
)