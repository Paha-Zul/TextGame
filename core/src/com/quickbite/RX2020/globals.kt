package com.quickbite.rx2020

import com.quickbite.rx2020.managers.TraitManager

object Globals {
    val baseSurvivorHP = 100.0f
    var maxSurvivorHP = baseSurvivorHP
        private set

    private val baseROVHP = 100.0f
    var maxROVHP = baseROVHP
        private set

    val baseROVTravelSpeed = 10f
    var ROVTravelSpeed = baseROVTravelSpeed

    init{
        TraitManager.addListener("ROVMaxHp", "", {traitEffect, removing, _ ->
            if(!removing) //If we are not removing, add health
                maxROVHP += baseROVHP*(traitEffect.amount/100f)
            else //If we are removing, subtract health
                maxROVHP -= baseROVHP*(traitEffect.amount/100f)
        })

        //Change of survivor max HP global modifier
        TraitManager.addListener("survivorMaxHP", "", {traitEffect, removing, person ->
            val amt = if(!removing) //If we are not removing, add health
                baseSurvivorHP*(traitEffect.amount/100f)
            else //If we are removing, subtract health
                -baseSurvivorHP*(traitEffect.amount/100f)

            if(traitEffect.scope == "global") //If the scope is global...
                maxSurvivorHP += amt
            else if(traitEffect.scope == "individual") //If it's individual, only change the bonus max health of a specific survivor
                person!!.bonusMaxHealth += amt
        })

        //Changing of travel speed global modifier
        TraitManager.addListener("travelSpeed", "", {traitEffect, removing, _ ->
            val amt = if(!removing) //If we are not removing, positive!
                traitEffect.amount
            else //If we are removing, negative!
                -traitEffect.amount

            ROVTravelSpeed += amt
            ROVTravelSpeed.clamp(baseROVTravelSpeed, 100f) //100 is some arbitrary high number
        })
    }
}