package com.quickbite.game

import com.badlogic.gdx.math.MathUtils
import com.quickbite.game.managers.DataManager
import com.quickbite.game.managers.SupplyManager

/**
 * Created by Paha on 3/10/2016.
 */
object TradeManager {
    var exomerList:MutableList<Triplet<String, Int, Int>>? = null
        get
        private set

    var otherList:MutableList<Triplet<String, Int, Int>>? = null
        get
        private set

    fun generateLists() {
        val list = DataManager.getItemList()

        exomerList = mutableListOf()
        otherList = mutableListOf()
        for(item in list){
            exomerList!!.add(Triplet(item.displayName, SupplyManager.getSupply(item.name).amt.toInt(), MathUtils.random(item.worth!![0], item.worth!![1])))

            otherList!!.add(Triplet(item.displayName, MathUtils.random(item.randStartAmt!![0], item.randStartAmt!![1]), MathUtils.random(item.worth!![0], item.worth!![1])))
        }
    }

    class Triplet<out A, out B, out C>(val val1:A, val val2:B, val val3:C)
}