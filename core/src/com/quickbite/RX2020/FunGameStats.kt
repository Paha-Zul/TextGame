package com.quickbite.rx2020

/**
 * Created by Paha on 5/3/2016.
 */
object FunGameStats {
    val statsMap:MutableMap<String, String> = hashMapOf()
    val statsList:MutableList<Stat> = mutableListOf()

    fun addStat(desc:String, value:String, unique:Boolean = false){
        if(unique)
            statsList.add(Stat(desc, value))
        else {
            var stat = statsMap.getOrPut(desc, { value })
            stat = (stat.toInt() + value.toInt()).toString()
            statsMap.put(desc, stat)
        }
    }

    class Stat(val desc:String, var value:String){

    }
}