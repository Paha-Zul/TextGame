package com.quickbite.game.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.game.Person

/**
 * Created by Paha on 2/8/2016.
 */
object GroupManager {
    private val list:MutableList<Person> = arrayListOf()

    val numPeopleAlive:Int
        get() = list.size

    init{
        list += Person(DataManager.pullRandomName())
        list += Person(DataManager.pullRandomName())
        list += Person(DataManager.pullRandomName())
        list += Person(DataManager.pullRandomName())
        list += Person(DataManager.pullRandomName())
    }

    fun getPeopleList():Array<Person>{
        return list.toTypedArray()
    }

    fun getPerson(name:String): Person?{
        if(name.equals("random")) return list[MathUtils.random(list.size-1)]
        return list.find {person -> person.name.equals(name)}
    }

    fun getRandomPerson():Person{
        return list[MathUtils.random(0, list.size-1)]
    }

    fun killPerson(name:String){
        val person = getPerson(name)
        if(person!=null)
            list -= person
    }
}