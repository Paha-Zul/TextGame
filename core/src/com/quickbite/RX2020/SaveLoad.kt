package com.quickbite.rx2020

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.TimeUtils
import com.quickbite.rx2020.managers.GameStats
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.managers.SupplyManager
import com.quickbite.rx2020.util.Logger

/**
 * Created by Paha on 4/7/2016.
 */

object SaveLoad{
    private val saveName = "save"

    private val json:Json = Json()

    fun saveGame(threaded:Boolean){
        json.setIgnoreUnknownFields(false)
        json.setUsePrototypes(true)

        val startTime = TimeUtils.nanoTime()
        val save = SaveGamePOJO()

        save.currTime = GameStats.TimeInfo.totalTimeCounter
        save.currMiles = GameStats.TravelInfo.totalDistTraveled
        save.maxTripMileage = GameStats.TravelInfo.totalDistOfGame

        GroupManager.getPeopleList().forEach { person -> save.personList.add(PersonPOJO(person.fullName, person.healthNormal+person.healthInjury, person.disabilityList, person.male))}

        SupplyManager.getSupplyList().forEach { supply -> save.supplyList.add(SupplyPOJO(supply.name, supply.amt, supply.currHealth)) }

        Logger.log("SaveLoad", "Gather game data in ${(TimeUtils.nanoTime() - startTime)/1000000000.0} seconds")

        if(threaded)
            TextGame.threadPool.submit {
                val start = TimeUtils.nanoTime()
                var file = Gdx.files.local(saveName)
                file.writeString(json.toJson(save), false)
                Logger.log("SaveLoad", "Saved game (threaded) in ${(TimeUtils.nanoTime() - start)/1000000000.0} seconds")
            }
        else{
            val start = TimeUtils.nanoTime()
            var file = Gdx.files.local(saveName)
            file.writeString(json.toJson(save), false)
            Logger.log("SaveLoad", "Saved game (non-threaded) in ${(TimeUtils.nanoTime() - start)/1000000000.0} seconds")
        }
    }

    fun loadGame(){
        json.setIgnoreUnknownFields(false)
        json.setUsePrototypes(true)

        val startTime = TimeUtils.nanoTime()

        var file = Gdx.files.local(saveName)
        val save = json.fromJson(SaveGamePOJO::class.java, file)

        GameStats.TimeInfo.totalTimeCounter = save.currTime
        GameStats.TravelInfo.totalDistTraveled = save.currMiles
        GameStats.TravelInfo.totalDistOfGame = save.maxTripMileage

        GroupManager.clearPeople()
        save.personList.forEach { jsonPerson ->
            var names = jsonPerson.name.split(" ")
            val person = Person(names[0], names[1], jsonPerson.male) //Make a new person to add to the group.
            person.addHealth(jsonPerson.health - person.maxHealth) //We need to set the health through a bit of roundabout.
            person.disabilityList = jsonPerson.disabilities
            GroupManager.addPerson(person)
        }

        save.supplyList.forEach { supply ->
            val _supply = SupplyManager.getSupply(supply.name)
            _supply.amt = supply.currAmount
            _supply.currHealth = supply.currHealth
        }

        Logger.log("SaveLoad", "Loaded game in ${(TimeUtils.nanoTime() - startTime)/1000000000.0} seconds")
    }

    fun saveExists():Boolean = Gdx.files.local(saveName).exists()

    fun deleteSave():Boolean = Gdx.files.local(saveName).delete()

    private class SaveGamePOJO(){
        var currTime:Float = 0f
        var currMiles:Int = 0
        var maxTripMileage:Int = 0
        var personList:MutableList<PersonPOJO> = mutableListOf()
        var supplyList:MutableList<SupplyPOJO> = mutableListOf()
    }

    private class PersonPOJO(var name:String, var health:Float, var disabilities:List<Person.Disability>, var male:Boolean){
        constructor():this("", 0f, listOf(), false)
    }

    private class SupplyPOJO(var name:String, var currAmount:Float, var currHealth:Float){
        constructor():this("", 0f, 0f)
    }

}