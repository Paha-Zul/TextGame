package com.quickbite.rx2020.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.IUpdateable
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.util.Logger
import java.util.*

/**
 * Created by Paha on 2/8/2016.
 */
object SupplyManager : IUpdateable {
    private val supplyMap:LinkedHashMap<String, Supply> = linkedMapOf()

    fun init(){
        supplyMap.clear()
        var list = DataManager.getItemList()
        for(item in list){
            var randStart = MathUtils.random(item.randStartAmt!![0], item.randStartAmt!![1]).toFloat()
            if(item.perMember) randStart = randStart*GroupManager.numPeopleAlive
            val maxAmount = if(item.perMember) item.max*GroupManager.numPeopleAlive else item.max

            if(!TextGame.testMode)
                addNewSupply(item.name, item.abbrName, item.displayName, randStart, maxAmount)
            else
                addNewSupply(item.name, item.abbrName, item.displayName, 10000000f, 10000000)

        }

        supplyMap["edibles"]?.consumePerDay = 5f
        supplyMap["energy"]?.consumePerDay = 3.3f
    }

    fun addNewSupply(name:String, abbrName:String, displayName:String, amt:Float, maxAmount:Int):Supply{
        val supply = Supply(name, abbrName, displayName, amt, maxAmount)
        supplyMap.put(name, supply)
        return supply
    }

    fun addToSupply(name:String, amt:Float):Supply{
        val supply = supplyMap[name]
        if(supply == null) Logger.log("SupplyManager", "Trying to add to supply $name which doesn't exist.", Logger.LogLevel.Warning)
        supply!!.amt += amt
        if(supply!!.amt < 0) supply.amt = 0f
        else if(supply.amt >= supply.maxAmount) supply.amt = supply.maxAmount.toFloat()

        EventManager.callEvent("supplyChanged", supply, amt)
        return supply
    }

    fun setSupply(name:String, amt:Float):Supply{
        val supply = supplyMap[name]!!
        supply.amt = amt
        return supply
    }

    override fun update(delta:Float){

    }

    override fun updateHourly(delta:Float){
        var supply = supplyMap["edibles"]!!
        var amt = supply.amt- ((supply.consumePerDay*GroupManager.numPeopleAlive)/24f)
        supply.amt = if (amt >= 0) amt else 0f

        supply = supplyMap["energy"]!!
        amt = supply.amt - ((supply.consumePerDay)/24f)
        supply.amt = if (amt >= 0) amt else 0f
    }

    fun getSupplyList():Array<Supply>{
        return supplyMap.values.toTypedArray()
    }

    fun getSupply(name:String):Supply = supplyMap[name]!!

    fun clearSupplies() = supplyMap.clear()

    class Supply(val name:String, val abbrName:String, val displayName:String, var amt:Float, var maxAmount:Int, var maxHealth:Float = 100f, var currHealth:Float = 100f){
        var consumePerDay:Float = 0f

        operator fun component1() = displayName
        operator fun component2() = amt
        operator fun component3() = maxAmount
    }
}