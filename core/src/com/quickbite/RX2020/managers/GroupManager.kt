package com.quickbite.rx2020.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.IUpdateable
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.util.Logger

/**
 * Created by Paha on 2/8/2016.
 */
object GroupManager : IUpdateable {
    private val list:MutableList<Person> = arrayListOf()

    private val emptyFoodHealthDrain = 0.3f

    val numPeopleAlive:Int
        get() = list.size

    fun init(){
        list.clear()
        val health = if(TextGame.testMode) 1000000f else 100f
        val range = MathUtils.random(0,4) + 4 //4 - 8

        for(i in 0.rangeTo(range-1))
            list += Person(DataManager.pullRandomName(), health)

        list[0].addInjury(Person.Injury.InjuryType.Regular)
    }

    override fun update(delta: Float) {

    }

    override fun updateHourly(delta: Float) {
        val outOfFood = SupplyManager.getSupply("edibles").amt <= 0

        //For each person...
        getPeopleList().forEach { person ->
            //Update their injuries. If they are done, remove them from the person.
            person.injuryList.forEach { injury ->
                injury.updateHourly(delta)
                if(injury.done)
                    person.removeInjury(injury)
            }

            //If we are out of food, drain the person's health
            if(outOfFood)
                person.addHealth(emptyFoodHealthDrain)
        }

    }

    fun getPeopleList():Array<Person> = list.toTypedArray()

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

    fun clearPeople() = list.clear()

    fun addPerson(person:Person) {
        list += person
    }

    fun killPerson(name:String){
        val person = getPerson(name)
        if(person!=null) {
            list -= person
            EventManager.callEvent("death", person)
        }
    }
}