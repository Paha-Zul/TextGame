package com.quickbite.rx2020

import com.badlogic.gdx.math.MathUtils
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
        get() = _firstName+" "+_lastName

    private var _healthNormal:Float = 100f
    val healthNormal:Int
        get() = _healthNormal.toInt()

    private var _healthInjury:Float = 0f
    val healthInjury:Int
        get() = _healthInjury.toInt()

    var maxHealth = 100f
        get
        private set


    private var injuries:MutableList<Injury> = mutableListOf()
    private val injuryList:List<Injury>
        get() = injuries.toList()

    constructor(name:Pair<String, String>):this(name.first, name.second)

    operator fun component1() = _firstName
    operator fun component2() = _lastName

    fun addHealth(amt:Float):Float{
        _healthNormal +=amt
        if(_healthNormal >= maxHealth - healthInjury)
            _healthNormal = maxHealth - healthInjury
        if(_healthNormal <= 0)
            GroupManager.killPerson(firstName)

        return amt
    }

    fun addPercentHealth(perc:Float):Float{
        val amt = maxHealth*(perc/100f)
        _healthNormal -= amt
        if(_healthNormal >= maxHealth - healthInjury)
            _healthNormal = maxHealth - healthInjury
        if(_healthNormal <= 0)
            GroupManager.killPerson(firstName)

        return amt
    }

    fun addInjury(type: Injury.InjuryType){
        val injury = Injury(type)
        injuries.add(injury)
        _healthNormal -= injury.hpLost
        _healthInjury += injury.hpLost
    }

    class Injury(val type:InjuryType){
        enum class InjuryType{
            Minor, Regular, Major, Trauma
        }

        var daysRemaining = 0
        var hpLost = 0

        init{
            when(type){
                InjuryType.Minor ->{ daysRemaining = MathUtils.random(10, 30); hpLost = MathUtils.random(0, 25)}
                InjuryType.Regular ->{ daysRemaining = MathUtils.random(30, 50); hpLost = MathUtils.random(25, 50)}
                InjuryType.Major ->{ daysRemaining = MathUtils.random(50, 70); hpLost = MathUtils.random(50, 75)}
                InjuryType.Trauma ->{ daysRemaining = MathUtils.random(70, 90); hpLost = MathUtils.random(75, 100)}
            }
        }
    }
}