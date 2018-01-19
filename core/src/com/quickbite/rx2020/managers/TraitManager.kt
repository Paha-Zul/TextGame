package com.quickbite.rx2020.managers

import com.quickbite.rx2020.MutablePair

object TraitManager {
    private val blankTraitEffect = DataManager.TraitJson()

    val globalTraitMap:HashMap<String, HashMap<String,  MutablePair<Float, DataManager.TraitJson>>> = hashMapOf()
    val individualTraitMap:HashMap<String, HashMap<String, HashMap<String, MutablePair<Float, DataManager.TraitJson>>>> = hashMapOf()

    fun addTrait(trait: DataManager.TraitJson, person:String = "" ){
        trait.effects.forEach { t ->
            val map = if(t.scope == "global") globalTraitMap else individualTraitMap.getOrPut(person, {hashMapOf()})

            val innerMap = map.getOrPut(t.affects!!, { hashMapOf()})
            val key2 = t.subName ?: t.subType ?: t.subCommand ?: t.affects!!

            val value = innerMap.getOrPut(key2, { MutablePair(0f, trait) })
            value.first += t.amount
        }
    }

    fun removeTrait(trait: DataManager.TraitJson, person:String = "" ){
        trait.effects.forEach { traitEffect ->
            //Get the right map
            val map = if(traitEffect.scope == "global") globalTraitMap else individualTraitMap.getOrPut(person, {hashMapOf()})

            //Get the inner map
            val innerMap = map.getOrPut(traitEffect.affects!!, { hashMapOf()})
            //Here's our key for the inner map
            val key2 = traitEffect.subName ?: traitEffect.subType ?: traitEffect.subCommand ?: traitEffect.affects!!

            //Get the value from the inner map
            var value = innerMap.getOrElse(key2, {MutablePair(0f, trait)})
            if(value.first == 0.0f) //If our value is already 0, simply return here.
                return

            value.first -= traitEffect.amount

            //TODO Do we really need to bother removing maps when they are empty? Waste of CPU time eh?

            if(value.first == 0.0f) { //If the value is at 0 (we can have both negative and positives so we have to check against 0
                innerMap.remove(key2) //Remove the mapping from the inner map
                if(innerMap.isEmpty()) //If the inner map is empty
                    map.remove(traitEffect.affects!!) //Remove the inner map from the upper map
            }
        }
    }

    fun getTraitModifier(affects:String, subName:String? = null, subType:String? = null, subCommand:String? = null, person:String? = null):Pair<Float, Boolean>{
        val key2 = subName ?: subType ?: subCommand ?: affects

        val value = globalTraitMap.getOrElse(affects, { hashMapOf()}).getOrElse(key2, {MutablePair(0f, blankTraitEffect)})

        var modifier = value.first
        if(person != null)
            modifier += individualTraitMap.getOrPut(person, { hashMapOf()}).getOrPut(affects, { hashMapOf()}).getOrElse(key2, {0f}) as Float

        val isPercent = value.second.effects.firstOrNull {
            it.affects == affects && (it.subName == subName || it.subType == it.subType || it.subCommand == subCommand)}?.percent ?: true

        return Pair(modifier, isPercent)
    }
}