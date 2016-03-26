package com.quickbite.game.managers

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
            val currAmt = SupplyManager.getSupply(item.name).amt.toFloat()
            exomerList!!.add(TradeSupply(item.name, item.abbrName, item.displayName, currAmt, currAmt, MathUtils.random(item.worth!![0], item.worth!![1])))

            val rndAmt = MathUtils.random(item.randStartAmt!![0], item.randStartAmt!![1]).toFloat()
            otherList!!.add(TradeSupply(item.name, item.abbrName, item.displayName, rndAmt, rndAmt, MathUtils.random(item.worth!![0], item.worth!![1])))
        }
    }

    class TradeSupply(val name:String, val abbrName:String, val displayName:String, val amt:Float, var currAmt:Float, val worth:Int)
}