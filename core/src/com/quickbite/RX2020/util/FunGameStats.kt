package com.quickbite.rx2020.util

import com.quickbite.rx2020.interfaces.IResetable

/**
 * Created by Paha on 5/3/2016.
 *
 * A recorder of fun game stats, like how many people died total during your journey.
 */
object FunGameStats : IResetable{
    var statsMap:MutableMap<String, String> = hashMapOf()
    var uniqueStatsList:MutableList<FunStat> = mutableListOf()

    /**
     * Adds a fun stat to show at the end of the game, like "total gathered food: 30" or something
     * @param desc The description to use. For non-unique stats, the desc will be the name of a supply (ie: "food"). For unique
     * stats, the desc will usually be a unique name ("Billy Bob")
     * @param value The value to use somewhere in the description ("food" or "Solar Panels"  etc...)
     * @param unique Whether this is a unique stat (like someone dying). If true, it will get it's own entry. If false,
     * the 'desc' param is used to stack the value on existing values (addFunStat("food", "30", false) will add '30' to the 'food' desc)
     */
    fun addFunStat(desc:String, value:String, unique:Boolean = false){
        //If unique, give it it's own entry
        if(unique)
            uniqueStatsList.add(FunStat(desc, value))

        //Otherwise, get the stat and increment it.
        else {
            var stat = statsMap.getOrPut(desc, { "0" })
            stat = (stat.toFloat() + value.toFloat()).toString()
            statsMap.put(desc, stat)
        }
    }

    class FunStat(val desc:String, var value:String)

    override fun reset() {
        statsMap = mutableMapOf()
        uniqueStatsList = mutableListOf()
    }
}