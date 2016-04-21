package com.quickbite.rx2020.util

import com.quickbite.rx2020.managers.EventManager
import com.quickbite.rx2020.managers.GameEventManager
import com.quickbite.rx2020.managers.ROVManager
import com.quickbite.rx2020.managers.SupplyManager

/**
 * Created by Paha on 4/10/2016.
 */
object GH {
    /**
     * Lerps a float value from start to target.
     * @param curr The current value of the lerp.
     * @param start The start value of the lerp.
     * @param target The target value of the lerp.
     * @param seconds The time in seconds for the lerp to happen.
     * @return The value of the lerp after the calculated tick amount.
     */
    fun lerpValue(curr: Float, start: Float, target: Float, seconds: Float): Float {
        var curr = curr
        val amt = Math.abs(start - target) / seconds / 60f
        if (start < target) {
            curr += amt
            if (curr >= target) curr = target
        } else {
            curr -= amt
            if (curr <= target) curr = target
        }

        return curr
    }

    fun executeEventActions(event: GameEventManager.EventJson) {
        val list = event.resultingAction
        if (list != null) {
            for (params in list) {
                if (params.size > 0)
                    EventManager.callEvent(params[0], params.slice(1.rangeTo(params.size - 1)))
            }
        }
    }

    fun getEventFromChoice(currEvent:GameEventManager.EventJson, choiceText:String):GameEventManager.EventJson?{
        return currEvent.selectChildEvent(choiceText)
    }

    fun parseAndCheckRestrictions(restrictionList:Array<Array<String>>):Boolean{
        var passed = true
        restrictionList.forEach { params ->
            var name:String = params[0]
            var operation:String = params[1]
            var amount:Int = params[2].toInt()

            var amtToCheck = 0
            if(name.equals("ROV"))
                amtToCheck = ROVManager.ROVPartMap["ROV"]!!.currHealth.toInt()
            else
                amtToCheck = SupplyManager.getSupply(name).amt.toInt()

            if(operation.equals("<")){
                passed = amtToCheck < amount
            }else if(operation.equals("<=")){
                passed =  amtToCheck <= amount
            }else if(operation.equals(">")){
                passed =  amtToCheck > amount
            }else{
                passed =  amtToCheck >= amount
            }
        }

        return passed
    }

}