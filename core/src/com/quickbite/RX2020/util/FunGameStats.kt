package com.quickbite.rx2020.util

import com.quickbite.rx2020.interfaces.IResetable

/**
 * Created by Paha on 5/3/2016.
 */
object FunGameStats : IResetable{
    var statsMap:MutableMap<String, String> = hashMapOf()
    var uniqueStatsList:MutableList<FunStat> = mutableListOf()

    fun addFunStat(desc:String, value:String, unique:Boolean = false){
        if(unique)
            uniqueStatsList.add(FunStat(desc, value))
        else {
            var stat = statsMap.getOrPut(desc, { "0" })
            stat = (stat.toInt() + value.toInt()).toString()
            statsMap.put(desc, stat)
        }
    }

    class FunStat(val desc:String, var value:String){

    }

    override fun reset() {
        statsMap = mutableMapOf()
        uniqueStatsList = mutableListOf()
    }
}