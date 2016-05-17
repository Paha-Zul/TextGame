package com.quickbite.rx2020

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.interfaces.IUpdateable
import com.quickbite.rx2020.managers.EventManager
import com.quickbite.rx2020.managers.GroupManager

/**
 * Created by Paha on 2/8/2016.
 */
class Person(_firstName:String, _lastName:String, val male:Boolean, _timeAdded:Long) {
    var firstName:String = ""
        get
    var lastName:String = ""
        get
    var fullName:String = ""
        get() = "$firstName $lastName"
    var timeAdded:Long = 0
        get

    var healthNormal:Float = 100f
        get
        private set

    var healthInjury:Float = 0f
        get
        private set

    var maxHealth = 100f
        get
        private set

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

    constructor(firstName:String, lastName:String, currHealth:Float, maxHealth:Float, male:Boolean, _timeAdded: Long):this(firstName, lastName, male, _timeAdded){
        this.maxHealth = maxHealth
        this.healthNormal = currHealth
    }

    constructor(name:Pair<String, String>, currHealth:Float, maxHealth:Float, male:Boolean, _timeAdded: Long):this(name.first, name.second, male, _timeAdded){
        this.maxHealth = maxHealth
        this.healthNormal = currHealth
    }

    operator fun component1() = firstName
    operator fun component2() = lastName

    val isDead:Boolean
        get() = healthNormal <= 0

    fun addHealth(amt:Float):Float{
        healthNormal += amt
        if(healthNormal >= maxHealth - healthInjury)
            healthNormal = maxHealth - healthInjury
        if(isDead)
            GroupManager.killPerson(firstName)

        EventManager.callEvent("healthChanged", this, amt)
        return amt
    }

    fun addPercentHealth(perc:Float):Float{
        val amt = maxHealth*(perc/100f)

        EventManager.callEvent("healthChanged", this, amt)
        return addHealth(amt)
    }

    fun addAilment(level: Ailment.AilmentLevel, type: Ailment.AilmentType){
        val disability = Ailment(level, type)
        ailments.add(disability)
        val isInjury = disability.type == Ailment.AilmentType.Injury

        //Only shift health if it's an injury
        if(isInjury) {
            healthNormal -= disability.hpLost
            healthInjury += disability.hpLost
        }

        //Increment the right counter
        when(isInjury){
            true -> numInjury++
            else -> numSickness++
        }

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

    class Ailment(var level: AilmentLevel, var type: AilmentType): IUpdateable {
        enum class AilmentType {Injury, Sickness}
        enum class AilmentLevel {Minor, Regular, Major, Trauma}
        val done:Boolean
            get() = hoursRemaining <= 0

        //Need this empty constructor for loading/saving to json files.
        private constructor():this(AilmentLevel.Minor, AilmentType.Injury)

        var hoursRemaining = 0
        var hpLost = 0
        var hpLostPerHour = 0f

        init{
            when(level){
                AilmentLevel.Minor ->{ hoursRemaining = MathUtils.random(10*24, 30*24); hpLost = MathUtils.random(0, 25); hpLostPerHour = 0.12f}
                AilmentLevel.Regular ->{ hoursRemaining = MathUtils.random(30*24, 50*24); hpLost = MathUtils.random(25, 50); hpLostPerHour = 0.14f}
                AilmentLevel.Major ->{ hoursRemaining = MathUtils.random(50*24, 70*24); hpLost = MathUtils.random(50, 75); hpLostPerHour = 0.19f}
                AilmentLevel.Trauma ->{ hoursRemaining = MathUtils.random(70*24, 90*24); hpLost = MathUtils.random(75, 100); hpLostPerHour = 0.29f}
            }
        }

        override fun update(delta: Float) {}
        override fun updateHourly(delta: Float) {
            this.hoursRemaining--
        }
    }


    override fun toString(): String {
        return "$fullName - "+(if(male){"male"}else{"female"});
    }
}