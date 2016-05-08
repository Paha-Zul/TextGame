package com.quickbite.rx2020.util

/**
 * Created by Paha on 5/3/2016.
 */
object FunGameStats {
    val statsMap:MutableMap<String, String> = hashMapOf()
    val statsList:MutableList<FunStat> = mutableListOf()

    fun addFunStat(desc:String, value:String, unique:Boolean = false){
        if(unique)
            statsList.add(FunStat(desc, value))
        else {
            var stat = statsMap.getOrPut(desc, { value })
            stat = (stat.toInt() + value.toInt()).toString()
            statsMap.put(desc, stat)
        }
    }

    class FunStat(val desc:String, var value:String){

    }
}