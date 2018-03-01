package com.quickbite.rx2020

import com.quickbite.rx2020.managers.EventManager
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.objects.Ailment
import com.quickbite.rx2020.objects.Trait

/**
 * Created by Paha on 2/8/2016.
 */
class Person(_firstName:String, _lastName:String, val male:Boolean, _timeAdded:Long) {
    val traitList:MutableList<Trait> = mutableListOf()

    var firstName:String = ""
    var lastName:String = ""
    var fullName:String = ""
        get() = "$firstName $lastName"
    var timeAdded:Long = 0

    var healthNormal:Float = 100f
        private set

    var healthInjury:Float = 0f
        private set

    var baseMaxHealth = Globals.maxSurvivorHP
    var bonusMaxHealth = Globals.maxSurvivorHP
    val totalMaxHealth:Float
        get() = baseMaxHealth + bonusMaxHealth

    var numInjury = 0
    var numSickness = 0

    init{
        firstName = _firstName
        lastName = _lastName
        timeAdded = _timeAdded
    }

    private val ailments:MutableList<Ailment> = mutableListOf()
    val ailmentList:List<Ailment>
        get() = ailments.toList()

    val hasInjury:Boolean
        get() = numInjury > 0

    val hasSickness:Boolean
        get() = numSickness > 0

    constructor(name:Pair<String, String>, male:Boolean, _timeAdded: Long):this(name.first, name.second, male, _timeAdded)

    constructor(firstName:String, lastName:String, currHealth:Float, male:Boolean, _timeAdded: Long):this(firstName, lastName, male, _timeAdded){
        this.healthNormal = currHealth
    }

    constructor(name:Pair<String, String>, currHealth:Float, male:Boolean, _timeAdded: Long):this(name.first, name.second, male, _timeAdded){
        this.healthNormal = currHealth
    }

    operator fun component1() = firstName
    operator fun component2() = lastName

    val isDead:Boolean
        get() = healthNormal <= 0

    fun addHealth(amt:Float):Float{
        healthNormal += amt
        if(healthNormal >= totalMaxHealth - healthInjury)
            healthNormal = totalMaxHealth - healthInjury
        if(isDead)
            GroupManager.killPerson(firstName)

        EventManager.callEvent("healthChanged", listOf(fullName), amt)
        return amt
    }

    fun addPercentHealth(perc:Float):Float{
        val amt = totalMaxHealth*(perc/100f)

        EventManager.callEvent("healthChanged", listOf(fullName), amt)
        return addHealth(amt)
    }


    /**
     * Adds an ailment
     * @param ailment The ailment to add
     */
    fun addAilment(ailment: Ailment){
        ailments.add(ailment)
        val isInjury = ailment.type == Ailment.AilmentType.Injury

        if(isInjury) {
            healthNormal -= ailment.HPTakenByInjury
            healthInjury += ailment.HPTakenByInjury
        }

        when(isInjury){
            true -> numInjury++
            else -> numSickness++
        }

        if(isDead)
            GroupManager.killPerson(firstName)
    }

    /**
     * Adds an ailment to this person. This will deal with setting up health changes.
     * @param level The level of the ailment
     * @param type The type of the ailment
     */
    fun addAilment(level: Ailment.AilmentLevel, type: Ailment.AilmentType){
        val ailment = Ailment(level, type)
        addAilment(ailment)
    }

    /**
     * Reapplies all existing ailments. This will remove them and clear any effects to the person and reapply each ailment.
     * This is used for situations like when traits for ailments are added/removed and ailment effects will change
     */
    fun reapplyExistingAilments(){
        val existingAilments = ailments.toList() //Gotta make a copy here or clear() will erase both
        ailments.clear()

        //We add back the injury health and clear the injury health. We do this instead of calling
        //removeAilment because the ailments could have been altered
        healthNormal += healthInjury
        healthInjury = 0f

        existingAilments.forEach { addAilment(it) }
    }

    /**
     * Removes a specific ailment from this person
     * @param ailment The ailment to remove
     */
    fun removeAilment(ailment: Ailment):Boolean{
        val removed = ailments.remove(ailment)
        val isInjury = ailment.type == Ailment.AilmentType.Injury
        //Only shift health if it's an injury
        if(removed && isInjury) {
            healthNormal += ailment.HPTakenByInjury
            healthInjury -= ailment.HPTakenByInjury
        }

        //Decrement the right counter
        when(isInjury){
            true -> numInjury--
            else -> numSickness--
        }

        return removed
    }

    /**
     * Removes the worst ailment from this person
     */
    fun removeWorstAilment():Boolean{
        var worst: Ailment? = null
        var removed = false

        ailments.forEach { injury ->
            if(worst == null || worst!!.type < injury.type)
                worst = injury
        }

        if(worst!=null)
            removed = removeAilment(worst!!)

        return removed
    }

    /**
     * Removes the ailment with the longest duration from this person
     */
    fun removeLongestAilment():Boolean{
        var longest: Ailment? = null
        var removed = false

        ailments.forEach { injury ->
            if(longest == null || longest!!.hoursRemaining < injury.hoursRemaining)
                longest = injury
        }

        if(longest!=null)
            removed = removeAilment(longest!!)

        return removed
    }

    override fun toString(): String {
        return "$fullName - "+(if(male){"male"}else{"female"});
    }
}