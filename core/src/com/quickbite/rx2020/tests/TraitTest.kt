package com.quickbite.rx2020.tests

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.managers.TraitManager

object TraitTest {

    private val listOfTraitsAdded = mutableListOf<DataManager.TraitJson>()

    fun test(){
        System.out.println("-- Testing Traits --")

        for(i in 0.until(100)){
            val randomIndex = MathUtils.random(DataManager.traitList.professions.size-1)
            val randomTrait = DataManager.traitList.professions[randomIndex]
            TraitManager.addTrait(randomTrait, "Mike")
            System.out.println("Adding trait $randomTrait for Mike")

            listOfTraitsAdded += randomTrait
        }

        System.out.println("After adding traits")
        printDataFromMaps()

        System.out.println()
        System.out.println("Testing modifier amounts")
        testModifierAmount()
        System.out.println()

        removeTraits()
        System.out.println("After removing traits")
        printDataFromMaps()

        System.out.println("-- Done Testing Traits --")

        //Clear these both to make sure nothing gets stuck in here after testing
        TraitManager.individualTraitMap.clear()
        TraitManager.globalTraitMap.clear()
    }

    private fun printDataFromMaps(){
        System.out.println()

        System.out.println("Individuals")
        TraitManager.individualTraitMap.toList().forEach{ kv ->
            System.out.println(kv.first)
            kv.second.values.forEach {
                it.toList().forEach { kv ->
                    System.out.println("${kv.first}:${kv.second}")
                }}} //Print out

        System.out.println()

        System.out.println("Globals")
        TraitManager.globalTraitMap.toList().forEach { kv1 ->
            System.out.println(kv1)
        }
    }

    private fun testModifierAmount(){
        val itemList = DataManager.getItemList()

        for(i in 1 until 20){
            val randomItem = itemList[MathUtils.random(itemList.size - 1)]
            val baseAmount = MathUtils.random(50, 150).toFloat()
            val result:Pair<Float, Boolean>
            if(randomItem.type == "ROVPart")
                result = TraitManager.getTraitModifier("addRndAmt", subType = randomItem.type)
            else
                result = TraitManager.getTraitModifier("addRndAmt", randomItem.name)

            val modifiedAmount:Float=
                    if(result.second) //If we are using percent
                        baseAmount + baseAmount*(result.first/100f) //Divide by 100 to get actual percent
                    else
                        baseAmount + result.first //If not using percent, add it straight up

            System.out.println("Item ${randomItem.name}")
            System.out.println("Base amount: $baseAmount, Modified amount: $modifiedAmount, modifier: ${result.first}")
        }
    }

    private fun removeTraits(){
        listOfTraitsAdded.forEach{ trait ->
            TraitManager.removeTrait(trait, "Mike")
            System.out.println("Removing trait $trait for Mike")
        }
    }

}