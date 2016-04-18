package com.quickbite.rx2020.managers

/**
 * Created by Paha on 3/25/2016.
 */
object ROVManager {
    var ROVMaxHealth = 100f
        get
        private set

    var ROVHealth = 100f

    val ROVPartList:List<SupplyManager.Supply> = listOf(SupplyManager.getSupply("battery"), SupplyManager.getSupply("track"), SupplyManager.getSupply("panel"), SupplyManager.getSupply("storage"))

    private var chargeAmountPerHour = 8.3f
    private var drivingSpeed = 10f

    fun getPowerTick() = chargeAmountPerHour*(SupplyManager.getSupply("battery").currHealth/100f)
    fun getMovementSpeed() = drivingSpeed*(SupplyManager.getSupply("track").currHealth/100f)

    fun addHealthROV(amt:Float){
        ROVHealth += amt
        if(ROVHealth >= ROVMaxHealth)
            ROVHealth = ROVMaxHealth
    }
}