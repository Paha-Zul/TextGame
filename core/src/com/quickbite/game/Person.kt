package com.quickbite.game

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

    constructor(name:Pair<String, String>):this(name.first, name.second)

    operator fun component1() = _firstName
    operator fun component2() = _lastName

    fun addHealth(amt:Float){
        _health+=amt
        if(_health >= 100)
            _health = 100f
    }
}