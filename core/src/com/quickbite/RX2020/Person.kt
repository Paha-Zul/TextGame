package com.quickbite.rx2020

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.managers.EventManager
import com.quickbite.rx2020.managers.GroupManager

/**
 * Created by Paha on 2/8/2016.
 */
class Person(private val _firstName:String, private val _lastName:String) {
    var firstName:String = ""
        get() = _firstName
    var lastName:String = ""
        get() = _lastName
    var fullName:String = ""
        get() = "$_firstName $_lastName"

    var healthNormal:Float = 100f
        get
        private set

    var healthInjury:Float = 0f
        get
        private set

    var maxHealth = 100f
        get
        private set


    private var injuries:MutableList<Injury> = mutableListOf()
    var injuryList:List<Injury>
        get() = injuries.toList()
        set(value){
            injuries.clear()
            value.forEach { injury -> addInjury(injury) }
        }

    constructor(name:Pair<String, String>):this(name.first, name.second)

    operator fun component1() = _firstName
    operator fun component2() = _lastName

    fun addHealth(amt:Float):Float{
        healthNormal +=amt
        if(healthNormal >= maxHealth - healthInjury)
            healthNormal = maxHealth - healthInjury
        if(healthNormal <= 0)
            GroupManager.killPerson(firstName)

        EventManager.callEvent("healthChanged", this, amt)
        return amt
    }

    fun addPercentHealth(perc:Float):Float{
        val amt = maxHealth*(perc/100f)

        EventManager.callEvent("healthChanged", this, amt)
        return addHealth(amt)
    }

    fun addInjury(type: Injury.InjuryType){
        val injury = Injury(type)
        injuries.add(injury)
        healthNormal -= injury.hpLost
        healthInjury += injury.hpLost
    }

    fun removeInjury(injury: Injury){
        val removed = injuries.remove(injury)
        if(removed) {
            healthNormal += injury.hpLost
            healthInjury -= injury.hpLost
        }
    }

    /**
     * Used for loading in injuries from a save mostly.
     */
    private fun addInjury(injury:Injury){
        injuries.add(injury)
        healthNormal -= injury.hpLost
        healthInjury += injury.hpLost
    }

    class Injury(var type:InjuryType):IUpdateable{
        val done:Boolean
            get() = hoursRemaining <= 0

        //Need this empty constructor for loading/saving to json files.
        private constructor():this(InjuryType.Minor)

        enum class InjuryType{
            Minor, Regular, Major, Trauma
        }

        var hoursRemaining = 0
        var hpLost = 0

        init{
            when(type){
                InjuryType.Minor ->{ hoursRemaining = MathUtils.random(10*24, 30*24); hpLost = MathUtils.random(0, 25)}
                InjuryType.Regular ->{ hoursRemaining = MathUtils.random(30*24, 50*24); hpLost = MathUtils.random(25, 50)}
                InjuryType.Major ->{ hoursRemaining = MathUtils.random(50*24, 70*24); hpLost = MathUtils.random(50, 75)}
                InjuryType.Trauma ->{ hoursRemaining = MathUtils.random(70*24, 90*24); hpLost = MathUtils.random(75, 100)}
            }
        }

        override fun update(delta: Float) {

        }

        override fun updateHourly(delta: Float) {
            this.hoursRemaining--
        }
    }


    override fun toString(): String {
        return firstName;
    }
}