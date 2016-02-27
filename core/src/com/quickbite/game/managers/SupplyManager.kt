package com.quickbite.game.managers

import com.badlogic.gdx.math.MathUtils
import java.util.*

/**
 * Created by Paha on 2/8/2016.
 */
object SupplyManager {
    private val supplyMap:LinkedHashMap<String, Supply> = linkedMapOf()

    init{
        addNewSupply("energy", "Energy", MathUtils.random(50, 150).toFloat(), 200)
        addNewSupply("edibles", "Edibles", (MathUtils.random(75, 225)*GroupManager.numPeopleAlive).toFloat(), 250*GroupManager.numPeopleAlive) //Initially set the food
        addNewSupply("parts", "Parts", MathUtils.random(50, 150).toFloat(), 250)
        addNewSupply("medkits", "Med-kits", MathUtils.random(0, 5).toFloat(), 10)
        addNewSupply("wealth", "Wealth", MathUtils.random(1, 100).toFloat(), 250)
        addNewSupply("ammo", "Ammo", MathUtils.random(50, 150).toFloat(), 250)
        addNewSupply("solar panels", "Solar Panels", MathUtils.random(0, 2).toFloat(), 5)
        addNewSupply("tracks", "Tracks", MathUtils.random(0, 2).toFloat(), 5)
        addNewSupply("battery", "Battery", MathUtils.random(0, 2).toFloat(), 5)
        addNewSupply("storage", "Storage", MathUtils.random(0, 2).toFloat(), 5)

        supplyMap["edibles"]?.consumePerDay = 5f
        supplyMap["parts"]?.consumePerDay = 3.3f
    }

    fun addNewSupply(name:String, displayName:String, amt:Float, maxAmount:Int){
        supplyMap.put(name, Supply(name, displayName, amt, maxAmount))
    }

    fun addToSupply(name:String, amt:Float):Supply{
        val supply = supplyMap[name]!!
        supply.amt += amt
        return supply
    }

    fun update(delta:Float){

    }

    fun updatePerTick(){
        val food = supplyMap["edibles"]!!
        val _f = food.amt- ((food.consumePerDay*GroupManager.numPeopleAlive)/24f)
        food.amt = if (_f >= 0) _f else 0f

        val scrap = supplyMap["parts"]!!
        val _s = scrap.amt - ((scrap.consumePerDay)/24f)
        scrap.amt = if (_s >= 0) _s else 0f
    }

    fun getSupplyList():Array<Supply>{
        return supplyMap.values.toTypedArray()
    }


    class Supply(val name:String, val displayName:String, var amt:Float, var maxAmount:Int){
        var consumePerDay:Float = 0f
    }
}