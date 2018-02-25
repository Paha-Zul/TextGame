package com.quickbite.rx2020

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.interfaces.IUpdateable
import com.quickbite.rx2020.managers.EventManager
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.objects.Ailment
import com.quickbite.rx2020.util.Trait

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

    private var ailments:MutableList<Ailment> = mutableListOf()
    var ailmentList:List<Ailment>
        get() = ailments.toList()
        set(value){
            ailments.clear()
            value.forEach { injury -> addAilment(injury) }
        }

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
     * Adds an ailment to this person. This will deal with setting up health changes.
     * @param level The level of the ailment
     * @param type The type of the ailment
     * @param damageModifier The modifier for the ailment's damage which is altered by traits
     * @param durationModifier The modifier for the ailment's duration which is altered by traits
     */
    fun addAilment(level: Ailment.AilmentLevel, type: Ailment.AilmentType, damageModifier:Float = 0f, durationModifier:Float = 0f){
        val ailment = Ailment(level, type)
        ailments.add(ailment) //Add the ailment
        val isInjury = ailment.type == Ailment.AilmentType.Injury //Store whether it's an injury or sickness

        //Only shift health if it's an injury
        if(isInjury) {
            val amount = ailment.hpLost - ailment.hpLost*(damageModifier/100f) //We reduce by the modifier amount
            healthNormal -= amount //Reduce normal health
            healthInjury += amount //Add injury health
        }

        //Increment the right counter
        when(isInjury){
            true -> numInjury++
            else -> numSickness++
        }

        //Subtract the modified amount if any
        ailment.hoursRemaining -= (ailment.totalDuration*(damageModifier/100f)).toInt()

        if(isDead)
            GroupManager.killPerson(firstName)
    }

    fun removeAilment(ailment: Ailment):Boolean{
        val removed = ailments.remove(ailment)
        val isInjury = ailment.type == Ailment.AilmentType.Injury
        //Only shift health if it's an injury
        if(removed && isInjury) {
            healthNormal += ailment.hpLost
            healthInjury -= ailment.hpLost
        }

        //Decrement the right counter
        when(isInjury){
            true -> numInjury--
            else -> numSickness--
        }

        return removed
    }

    fun removeWorstAilment():Boolean{
        var worst: Ailment? = null
        var removed = false

        ailmentList.forEach { injury ->
            if(worst == null || worst!!.type < injury.type)
                worst = injury
        }

        if(worst!=null)
            removed = removeAilment(worst!!)

        return removed
    }

    fun removeLongestAilment():Boolean{
        var longest: Ailment? = null
        var removed = false

        ailmentList.forEach { injury ->
            if(longest == null || longest!!.hoursRemaining < injury.hoursRemaining)
                longest = injury
        }

        if(longest!=null)
            removed = removeAilment(longest!!)

        return removed
    }

    /**
     * Used for loading in injuries from a save mostly.
     */
    private fun addAilment(ailment: Ailment){
        ailments.add(ailment)
        val isInjury = ailment.type == Ailment.AilmentType.Injury

        if(isInjury) {
            healthNormal -= ailment.hpLost
            healthInjury += ailment.hpLost
        }

        when(isInjury){
            true -> numInjury++
            else -> numSickness++
        }
    }

    override fun toString(): String {
        return "$fullName - "+(if(male){"male"}else{"female"});
    }
}