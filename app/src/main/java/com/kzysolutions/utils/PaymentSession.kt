package com.kzysolutions.utils


object PaymentSession {
    lateinit var flatId: String
    lateinit var userId: String
    var amount: Int = 0
    lateinit var buildingId: String
    var flatDetails: FlatDetailsPayments? = null

    fun store(flatId: String, userId: String, amount: Int, buildingId: String, flatDetails: FlatDetailsPayments?) {
        this.flatId = flatId
        this.userId = userId
        this.amount = amount
        this.buildingId = buildingId
        this.flatDetails = flatDetails
    }

    fun restore(): PaymentSession {
        return this
    }
}
