package com.quickbite.game

import com.quickbite.game.managers.GroupManager

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

    var _health:Float = 100f
    var health:Int = 100
        get() = _health.toInt()

    var maxHealth = 100f

    constructor(name:Pair<String, String>):this(name.first, name.second)

    operator fun component1() = _firstName
    operator fun component2() = _lastName

    fun addHealth(amt:Float):Float{
        _health+=amt
        if(_health >= maxHealth)
            _health = maxHealth
        if(_health <= 0)
            GroupManager.killPerson(firstName)

        return amt
    }

    fun addPercentHealth(perc:Float):Float{
        val amt = maxHealth*perc
        _health -= amt
        if(_health >= maxHealth)
            _health = maxHealth
        if(_health <= 0)
            GroupManager.killPerson(firstName)

        return amt
    }
}