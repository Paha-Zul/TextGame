package com.quickbite.rx2020.managers

import com.quickbite.rx2020.Result
import com.quickbite.rx2020.clamp
import com.quickbite.rx2020.interfaces.IResetable
import com.quickbite.rx2020.screens.GameScreen
import java.util.*

/**
 * Created by Paha on 3/25/2016.
 */
object ROVManager : IResetable{
    var ROVPartMap:LinkedHashMap<String, SupplyManager.Supply> = linkedMapOf()

    private var chargeAmountPerHour = 8.3f
    private var drivingSpeed = 10f

    fun init(){
        ROVPartMap = linkedMapOf(Pair("ROV", SupplyManager.Supply("ROV", "ROV", "ROV", 1f, 1, 100f, 100f, true)), Pair("battery", SupplyManager.getSupply("battery")),
                Pair("track", SupplyManager.getSupply("track")), Pair("panel", SupplyManager.getSupply("panel")), Pair("storage", SupplyManager.getSupply("storage")))
    }

    fun addHealthToPart(name:String, amt:Float){
        val part = ROVPartMap[name]
        if(part != null) {
            part.currHealth += amt
            part.currHealth.clamp(0f, part.maxHealth)
        }

        Result.addRecentChange("${part!!.name} health", amt.toFloat(), GameScreen.currGameTime, "", isEventRelated = GameEventManager.currActiveEvent != null)
    }

    fun addHealthROV(amt:Float){
        val ROV = ROVPartMap["ROV"]!!
        ROV.currHealth += amt
        ROV.currHealth = ROV.currHealth.clamp(0f,  ROV.maxHealth)

        Result.addRecentChange("ROV", amt.toFloat(), GameScreen.currGameTime, "'s HP", isEventRelated = GameEventManager.currActiveEvent != null)
    }

    override fun reset() {
        ROVPartMap.clear()
    }
}