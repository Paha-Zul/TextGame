package com.quickbite.game.managers

/**
 * Created by Paha on 3/25/2016.
 */
object ROVManager {
    private var ROVMaxHealth = 100f
    var ROVHealth = 100f

    private var chargeAmountPerHour = 8.3f
    private var drivingSpeed = 10f

    var batteryHealth = 100f
        get
        set

    var storageHealth = 100f
        get
        set

    var solarPanelHealth = 100f
        get
        set

    var trackHealth = 100f
        get
        set

    fun getPowerTick() = chargeAmountPerHour*(batteryHealth/100f)
    fun getMovementSpeed() = drivingSpeed*(trackHealth/100f)

    fun addHealthROV(amt:Float){
        ROVHealth += amt
        //TODO Future breakdown code here.
    }
}