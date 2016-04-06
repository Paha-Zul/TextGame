package com.quickbite.rx2020.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.Updateable
import com.quickbite.rx2020.util.Logger
import java.util.*

/**
 * Created by Paha on 2/8/2016.
 */
object SupplyManager : Updateable {
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
        supplyMap["energy"]?.consumePerDay = 3.3f
    }

    fun addNewSupply(name:String, abbrName:String, displayName:String, amt:Float, maxAmount:Int){
        supplyMap.put(name, Supply(name, abbrName, displayName, amt, maxAmount))
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

        if(supply.amt <= 0)
            GroupManager.getPeopleList().forEach { person -> person.addHealth(-0.30f)}

        supply = supplyMap["energy"]!!
        amt = supply.amt - ((supply.consumePerDay)/24f)
        supply.amt = if (amt >= 0) amt else 0f
    }

    fun getSupplyList():Array<Supply>{
        return supplyMap.values.toTypedArray()
    }

    fun getSupply(name:String):Supply = supplyMap[name]!!

    class Supply(val name:String, val abbrName:String, val displayName:String, var amt:Float, var maxAmount:Int, var maxHealth:Float = 100f, var currHealth:Float = 100f){
        var consumePerDay:Float = 0f

        operator fun component1() = displayName
        operator fun component2() = amt
        operator fun component3() = maxAmount
    }
}