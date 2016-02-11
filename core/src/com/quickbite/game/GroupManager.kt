package com.quickbite.game

import com.badlogic.gdx.math.MathUtils

/**
 * Created by Paha on 2/8/2016.
 */
class GroupManager {
    private val list:MutableList<Person> = arrayListOf()

    val numPeopleAlive:Int
        get() = list.size

    init{
        list += Person("John")
        list += Person("Jacob")
        list += Person("Will")
        list += Person("Brad")
        list += Person("Ben")
    }

    fun getPeopleList():Array<Person>{
        return list.toTypedArray()
    }

    fun getPerson(name:String):Person?{
        if(name.equals("random")) return list[MathUtils.random(list.size-1)]
        return list.find {person -> person.name.equals(name)}
    }

    fun killPerson(name:String){
        val person = getPerson(name)
        if(person!=null)
            list -= person
    }
}