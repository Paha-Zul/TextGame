package com.quickbite.game

import com.badlogic.gdx.math.MathUtils

/**
 * Created by Paha on 2/8/2016.
 */
class SupplyManager(val game:GameScreen, val groupManager:GroupManager) {
    val map:MutableMap<String, Supply> = hashMapOf()

    init{
        addNewSupply("edibles", "Edibles", 0f)
        addNewSupply("parts", "Parts", 0f)
        addNewSupply("medkits", "Medkits", 0f)
        addNewSupply("wealth", "Wealth", 0f)
        addNewSupply("energy", "Energy", 0f)
        addNewSupply("ammo", "Ammo", 0f)
        addNewSupply("solar panels", "Solar Panels", 0f)
        addNewSupply("tracks", "Tracks", 0f)
        addNewSupply("battery", "Battery", 0f)
        addNewSupply("storage", "Storage", 0f)

        map["edibles"]?.amt = (MathUtils.random(75, 225)*groupManager.numPeopleAlive).toFloat() //Initially set the food
        map["parts"]?.amt = MathUtils.random(50, 150).toFloat()

        map["edibles"]?.consumePerDay = 5f
        map["parts"]?.consumePerDay = 3.3f
    }

    fun addNewSupply(name:String, displayName:String, amt:Float){
        map.put(name, Supply(name, displayName, amt))
    }


    fun update(delta:Float){

    }

    fun updatePerTick(){
        val food = map["edibles"]!!
        val _f = food.amt- ((food.consumePerDay*groupManager.numPeopleAlive)/24f)
        food.amt = if (_f >= 0) _f else 0f

        val scrap = map["parts"]!!
        val _s = scrap.amt - ((scrap.consumePerDay)/24f)
        scrap.amt = if (_s >= 0) _s else 0f
    }

    fun getSupplyList():Array<Supply>{
        return map.values.toTypedArray()
    }

    class Supply(val name:String, val displayName:String, var amt:Float){
        var consumePerDay:Float = 0f
    }
}