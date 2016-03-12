package com.quickbite.game

import com.badlogic.gdx.math.MathUtils
import com.quickbite.game.managers.DataManager
import com.quickbite.game.managers.SupplyManager

/**
 * Created by Paha on 3/10/2016.
 */
object TradeManager {
    var exomerList:MutableList<TradeSupply>? = null
        get
        private set

    var otherList:MutableList<TradeSupply>? = null
        get
        private set

    fun generateLists() {
        val list = DataManager.getItemList()

        exomerList = mutableListOf()
        otherList = mutableListOf()
        for(item in list){
            exomerList!!.add(TradeSupply(item.name, item.displayName, SupplyManager.getSupply(item.name).amt.toFloat(), MathUtils.random(item.worth!![0], item.worth!![1])))

            otherList!!.add(TradeSupply(item.name, item.displayName, MathUtils.random(item.randStartAmt!![0], item.randStartAmt!![1]).toFloat(), MathUtils.random(item.worth!![0], item.worth!![1])))
        }
    }

    class Triplet<A, B, C>(var val1:A, var val2:B, var val3:C)
    class Quadruplet<A, B, C, D>(var val1:A, var val2:B, var val3:C, var val4:D)
    class TradeSupply(val name:String, val displayName:String, var amt:Float, val worth:Int)
}