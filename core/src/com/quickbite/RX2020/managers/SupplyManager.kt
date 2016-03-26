package com.quickbite.rx2020.managers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import java.util.*

/**
 * Created by Paha on 2/8/2016.
 */
object SupplyManager {
    private val supplyMap:LinkedHashMap<String, Supply> = linkedMapOf()

    init{
        var list = DataManager.getItemList()
        for(item in list){
            val randStart = MathUtils.random(item.randStartAmt!![0], item.randStartAmt!![1]).toFloat()
            if(item.perMember) randStart*GroupManager.numPeopleAlive
            val maxAmount = if(item.perMember) item.max*GroupManager.numPeopleAlive else item.max

            addNewSupply(item.name, item.abbrName, item.displayName, randStart, maxAmount)
        }

        supplyMap["edibles"]?.consumePerDay = 5f
        supplyMap["parts"]?.consumePerDay = 3.3f
        supplyMap["energy"]?.consumePerDay = 3.3f
    }

    fun addNewSupply(name:String, abbrName:String, displayName:String, amt:Float, maxAmount:Int){
        supplyMap.put(name, Supply(name, abbrName, displayName, amt, maxAmount))
    }

    fun addToSupply(name:String, amt:Float):Supply{
        val supply = supplyMap[name]
        if(supply == null) Gdx.app.error("SupplyManager", "Trying to add to supply $name which doesn't exist.")
        else supply.amt += amt
        return supply!!
    }

    fun setSupply(name:String, amt:Float):Supply{
        val supply = supplyMap[name]!!
        supply.amt = amt
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

    fun getSupply(name:String):Supply = supplyMap[name]!!

    class Supply(val name:String, val abbrName:String, val displayName:String, var amt:Float, var maxAmount:Int){
        var consumePerDay:Float = 0f

        operator fun component1() = displayName
        operator fun component2() = amt
        operator fun component3() = maxAmount
    }
}