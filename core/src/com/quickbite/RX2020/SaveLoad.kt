package com.quickbite.rx2020

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.managers.SupplyManager

/**
 * Created by Paha on 4/7/2016.
 */

object SaveLoad{
    val json:Json = Json()

    fun saveGame(){
        val save = SaveGamePOJO()

        save.currTime = GameStats.TimeInfo.currTime
        save.currMiles = GameStats.TravelInfo.totalDistTraveled
        save.maxTripMileage = GameStats.TravelInfo.totalDistOfGame

        GroupManager.getPeopleList().forEach { person -> save.personList.add(PersonPOJO(person.fullName, person.healthNormal, person.injuryList))}

        SupplyManager.getSupplyList().forEach { supply -> save.supplyList.add(SupplyPOJO(supply.name, supply.amt, supply.currHealth)) }

        var file = Gdx.files.local("save.json")
        file.writeString(json.toJson(save), false)
    }

    fun loadGame(){
        var file = Gdx.files.local("save.json")
        val save = json.fromJson(SaveGamePOJO::class.java, file)

        GameStats.TimeInfo.currTime = save.currTime
        GameStats.TravelInfo.totalDistTraveled = save.currMiles
        GameStats.TravelInfo.totalDistOfGame = save.maxTripMileage

        GroupManager.clearPeople()
        save.personList.forEach { person ->
            var names = person.name.split(" ")
            val pers = Person(names[0], names[1])
            pers.injuryList = person.injuries
            GroupManager.addPerson(pers)
        }

        save.supplyList.forEach { supply ->
            val _supply = SupplyManager.getSupply(supply.name)
            _supply.amt = supply.currAmount
            _supply.currHealth = supply.currHealth
        }
    }

    private class SaveGamePOJO(){
        var currTime:Int = 0
        var currMiles:Int = 0
        var maxTripMileage:Int = 0
        var personList:MutableList<PersonPOJO> = mutableListOf()
        var supplyList:MutableList<SupplyPOJO> = mutableListOf()
    }

    private class PersonPOJO(var name:String, var health:Float, var injuries:List<Person.Injury>){
        constructor():this("", 0f, listOf())
    }

    private class SupplyPOJO(var name:String, var currAmount:Float, var currHealth:Float){
        constructor():this("", 0f, 0f)
    }

}