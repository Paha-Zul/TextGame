package com.quickbite.rx2020.managers

import com.quickbite.rx2020.Result
import com.quickbite.rx2020.screens.GameScreen

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
    fun getPowerStorage() = drivingSpeed*(SupplyManager.getSupply("battery").currHealth/100f)
    fun getStorageAmount() = drivingSpeed*(SupplyManager.getSupply("storage").currHealth/100f)

    fun addHealthROV(amt:Float){
        ROVHealth += amt
        if(ROVHealth >= ROVMaxHealth)
            ROVHealth = ROVMaxHealth

        Result.addRecentChange("ROV", amt.toFloat(), GameScreen.currGameTime, "'s HP", GameScreen.gui, isEventRelated = GameEventManager.currActiveEvent != null)
    }
}