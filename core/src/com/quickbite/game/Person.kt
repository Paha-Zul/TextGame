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

    var health:Int = 100

    constructor(name:Pair<String, String>):this(name.first, name.second)
}