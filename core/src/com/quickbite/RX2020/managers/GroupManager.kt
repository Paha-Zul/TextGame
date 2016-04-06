package com.quickbite.rx2020.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.util.Logger

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

        list[0].addHealth(-20f)
        list[0].addInjury(Person.Injury.InjuryType.Regular) //TODO Testing.
    }

    fun getPeopleList():Array<Person>{
        return list.toTypedArray()
    }

    fun getPerson(name:String): Person?{
        val person = list.find {person -> person.firstName.equals(name)}
        if(person == null) Logger.log("GroupManager", "Trying to find person with name $name and it doesn't exist. People: ${list.toString()}")
        return person;
    }

    fun getRandomPerson():Person?{
        if(list.size > 0) {
            return list[MathUtils.random(0, list.size - 1)]
        }else
            return null
    }

    fun killPerson(name:String){
        val person = getPerson(name)
        if(person!=null) {
            list -= person
            EventManager.callEvent("death", person)
        }
    }
}