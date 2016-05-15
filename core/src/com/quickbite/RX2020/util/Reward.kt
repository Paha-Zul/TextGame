package com.quickbite.rx2020.util

/**
 * Created by Paha on 5/14/2016.
 */
class Reward() {
    var name:String = ""
    var supplies:Array<String> = arrayOf()
    var supplyAmounts:Array<Array<Int>> = arrayOf()
    var parts:Array<String> = arrayOf()

    companion object{
        val rewardMap:MutableMap<String, Reward> = mutableMapOf()
    }
}