package com.quickbite.rx2020.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.MutablePair
import com.quickbite.rx2020.Person

object TraitManager {
    private val listeners:HashMap<String, MutableList<(DataManager.TraitEffectJson, Boolean, Person?)->Unit>> = hashMapOf()

    private val blankTraitEffect = DataManager.TraitJson()

    //This hashmap stores global traits that aren't tied to a person
    val globalTraitMap:HashMap<String,  MutablePair<Float, DataManager.TraitJson>> = hashMapOf()

    //This hasmap stores traits tied to an individual
    val individualTraitMap:HashMap<String, MutablePair<Float, DataManager.TraitJson>> = hashMapOf()

    fun addTrait(trait: DataManager.TraitJson, person:Person? = null){

        trait.effects.forEach { traitEffect ->
            val map = if(traitEffect.scope == "global") globalTraitMap else individualTraitMap
            val key = traitEffect.affects + traitEffect.subName + traitEffect.subType + traitEffect.subCommand +
                    if(traitEffect.scope == "individual") person!!.fullName else ""

            if(traitEffect.amountRange.isNotEmpty())
                traitEffect.amount = if(MathUtils.randomBoolean()) traitEffect.amountRange[0] else traitEffect.amountRange[1]

            //When adding a trait we wanna check listeners in case some trait like
            //maxSurvivorHP needs to be triggered. We can basically combine the trait.affects
            //+ key2 to get the key for the listener map
            callListener(key, false, traitEffect, person)

            val value = map.getOrPut(key, { MutablePair(0f, trait) })
            value.first += traitEffect.amount
        }
    }

    fun removeTrait(trait: DataManager.TraitJson, person:Person? = null){
        trait.effects.forEach { traitEffect ->
            //Get the right map
            val map = if(traitEffect.scope == "global") globalTraitMap else individualTraitMap
            val key = traitEffect.affects + traitEffect.subName + traitEffect.subType + traitEffect.subCommand +
                    if(traitEffect.scope == "individual") person!!.fullName else ""

            //Call a listener here when removing a trait. If we have a trait that affects maxHP or something
            //then we need to modify the values here.
            callListener(key, true, traitEffect, person)

            //Get the value from the inner map
            val value = map.getOrElse(key, {MutablePair(0f, trait)})
            if(value.first == 0.0f) //If our value is already 0, simply return here.
                return

            value.first -= traitEffect.amount

            //TODO Do we really need to bother removing maps when they are empty? Waste of CPU time eh?

            if(value.first == 0.0f) { //If the value is at 0 (we can have both negative and positives so we have to check against 0
                map.remove(key) //Remove the mapping from the inner map
                if(map.isEmpty()) //If the inner map is empty
                    map.remove(traitEffect.affects) //Remove the inner map from the upper map
            }
        }
    }

    fun removeTrait(traitKeyName:String){
        val trait = globalTraitMap[traitKeyName] ?: individualTraitMap[traitKeyName]
        //TODO How to get a person's name from this?
    }

    private fun callListener(key:String, removing:Boolean, traitEffect:DataManager.TraitEffectJson, person:Person?){
        listeners[key]?.forEach{it.invoke(traitEffect, removing, person)}
    }

    /**
     * Gets the modifier amount from a trait retrieved by the input parameter
     * @param affects
     * @param subName
     * @param subType
     * @param subCommand
     * @param people A list of people to use to get a trait modifier.
     * @return Returns a pair with the
     */
    fun getTraitModifier(affects:String, subName:String = "", subType:String = "", subCommand:String = "", people:List<Person>? = null):Pair<Float, Boolean>{
        val key = affects + subName + subType + subCommand

        //This is the global trait map. It doesn't refer to a specific person...
        val globalTrait = globalTraitMap.getOrElse(key, {MutablePair(0f, blankTraitEffect)})
        var modifier = globalTrait.first //And here we store the modifier amount

        //For each person, add on to the modifier. So if two people are involved in the event, Tom and Jerry, then we try
        //to get the individual modifiers for both
        people?.forEach { person ->
            val trait = individualTraitMap.getOrElse(key+person.fullName, {MutablePair(0f, blankTraitEffect)})
            modifier += trait.first
        }

        val isPercent = globalTrait.second.effects.firstOrNull {
            it.affects == affects && (it.subName == subName || it.subType == it.subType || it.subCommand == subCommand)}?.percent ?: true

        return Pair(modifier, isPercent)
    }

    /**
     * Adds a listener for adding/removing traits from the trait manager. This is useful for global modifications like
     * ROV max health and an individual's max health
     * @param name The name of the trait to listen for
     * @param subName A secondary name (or category) to combine with name to listen for (like name:addHealth, subCommand:remove)
     * @param listener The listener to add to the list
     * @return The listener that was added.
     */
    fun addListener(name:String, subName:String, listener:(DataManager.TraitEffectJson, Boolean, Person?)->Unit):(DataManager.TraitEffectJson, Boolean, Person?)->Unit{
        listeners.getOrPut(name+subName, { mutableListOf(listener)})
        return listener
    }

    fun clearAll(){
        //We gotta do toList() here to create a copy to keep from a modification exception
        globalTraitMap.toList().forEach {
//            removeTrait(it.second)
            //TODO Under construction...
        }
    }
}