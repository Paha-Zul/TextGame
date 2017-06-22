package com.quickbite.rx2020.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.interfaces.IResetable
import com.quickbite.rx2020.interfaces.IUpdateable
import com.quickbite.rx2020.util.Logger
import com.quickbite.rx2020.util.Tester
import java.util.*

/**
 * Created by Paha on 2/8/2016.
 *
 * A Manager for supplies.
 */
object SupplyManager : IUpdateable, IResetable{
    private val supplyMap:LinkedHashMap<String, Supply> = linkedMapOf()

    fun init(){
        supplyMap.clear()
        val list = DataManager.getItemList()
        for(item in list){
            var randStart = MathUtils.random(item.randStartAmt!![0], item.randStartAmt!![1]).toFloat()
            if(item.perMember) randStart *= GroupManager.numPeopleAlive
            val maxAmount = if(item.perMember) item.max*GroupManager.numPeopleAlive else item.max

            if(!Tester.TESTING)
                addNewSupply(item.name, item.abbrName, item.displayName, randStart, maxAmount, item.affectedByHealth)
            else
                addNewSupply(item.name, item.abbrName, item.displayName, 10000000f, 10000000, item.affectedByHealth)

        }

        supplyMap["edibles"]?.consumePerDay = 5f
        supplyMap["energy"]?.consumePerDay = 3.3f
    }

    fun addNewSupply(name:String, abbrName:String, displayName:String, amt:Float, maxAmount:Int, affectByHealth:Boolean):Supply{
        val supply = Supply(name, abbrName, displayName, amt, maxAmount, 100f, 100f, affectByHealth)
        supplyMap.put(name, supply)
        return supply
    }

    fun addToSupply(name:String, amt:Float):Supply{
        val supply = supplyMap[name] //Get the supply.
        return addToSupply(supply, amt)
    }

    fun addToSupply(supply:Supply?, amt:Float):Supply{
        val _amt = amt //Let's make the passed in val mutable

        //Log it if the supply is null
        if(supply == null) Logger.log("SupplyManager", "Trying to add to supply ${supply?.name} which doesn't exist.", Logger.LogLevel.Warning)
        else {
            val oldAmt = supply.amt
            supply.amt += amt //Usually health for any supply will be 100/100, but for parts that degrade it will not?
            if (supply.amt < 0) supply.amt = 0f
            else if (supply.amt >= supply.maxAmount) supply.amt = supply.maxAmount.toFloat()

            EventManager.callEvent("supplyChanged", supply, _amt, oldAmt)
        }

        return supply!!
    }

    /**
     * Adds health to a certain
     */
    fun addHealthToSupply(name:String, amt:Float):Supply{
        val supply = supplyMap[name] //Get the supply.
        return addHealthToSupply(supply, amt)
    }

    fun addHealthToSupply(supply:Supply?, amt:Float):Supply{
        val _amt = amt //Let's make the passed in val mutable

        //Log it if the supply is null
        if(supply == null) Logger.log("SupplyManager", "Trying to add to supply ${supply?.name} which doesn't exist.", Logger.LogLevel.Warning)
        else {
            val oldAmt = supply.currHealth
            supply.currHealth += amt //Usually health for any supply will be 100/100, but for parts that degrade it will not?
            if (supply.currHealth < 0) supply.currHealth = 0f
            else if (supply.currHealth >= supply.maxHealth) supply.currHealth = supply.currHealth.toFloat()

            EventManager.callEvent("supplyHealthChanged", supply, _amt, oldAmt)
        }

        return supply!!
    }

    fun setSupplyAmount(name:String, amt:Float):Supply{
        val supply = supplyMap[name]!!
        return setSupplyAmount(supply, amt)
    }

    fun setSupplyAmount(supply:Supply, amt:Float):Supply{
        val oldAmt = supply.amt
        val amtChanged = supply.amt
        supply.amt = amt

        EventManager.callEvent("supplyChanged", supply, amtChanged, oldAmt)

        return supply
    }

    override fun update(delta:Float){

    }

    override fun updateHourly(delta:Float){
        var supply = supplyMap["edibles"]!!
        var amt = -(supply.consumePerDay*GroupManager.numPeopleAlive)/24f
        EventManager.callEvent("addRndAmt", amt.toString(), amt.toString(), "edibles")

        supply = supplyMap["energy"]!!
        amt = -(supply.consumePerDay)/24f
        EventManager.callEvent("addRndAmt", amt.toString(), amt.toString(), "energy")
    }

    fun getSupplyList():Array<Supply>{
        return supplyMap.values.toTypedArray()
    }

    fun getSupply(name:String):Supply = supplyMap[name]!!

    fun clearSupplies() = supplyMap.clear()

    override fun reset() {
        supplyMap.clear()
    }

    /**
     * Generic supply class that holds info for supplies
     * @param name The name of the supply
     * @param abbrName The abbreviated name of the supply
     * @param displayName The display name of the supply
     * @param amt The initial amount of the supply
     * @param maxAmount The max amount the supply can stack to
     * @param currHealth The initial and current health of the supply
     * @param maxHealth The max health of the supply
     * @param affectedByHealth True if the supply's function is affected by the health of the supply (ex: energy gain from broken solar panels)
     */
    class Supply(val name:String, val abbrName:String, val displayName:String, var amt:Float, var maxAmount:Int, var maxHealth:Float = 100f, var currHealth:Float = 100f, val affectedByHealth:Boolean){
        var consumePerDay:Float = 0f

        operator fun component1() = displayName
        operator fun component2() = amt
        operator fun component3() = maxAmount
    }
}