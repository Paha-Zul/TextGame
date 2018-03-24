package com.quickbite.rx2020.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.TimeUtils
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.gui.GroupGUI
import com.quickbite.rx2020.managers.GameEventManager
import com.quickbite.rx2020.managers.GameStats
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.managers.SupplyManager
import com.quickbite.rx2020.objects.Ailment
import com.quickbite.rx2020.screens.GameScreen

/**
 * Created by Paha on 4/7/2016.
 */

object SaveLoad{
    private val saveName = "save"

    private val json: Json = Json()

    fun saveGame(threaded:Boolean, game: GameScreen){
        json.setIgnoreUnknownFields(false)
        json.setUsePrototypes(true)

        val save = gatherGameState(game)

        if(threaded)
            TextGame.threadPool.submit {
                val start = TimeUtils.nanoTime()
                val file = Gdx.files.local(saveName)
                file.writeString(json.toJson(save), false)
                Logger.log("SaveLoad", "Saved game (threaded) in ${(TimeUtils.nanoTime() - start)/1000000000.0} seconds")
            }
        else{
            val start = TimeUtils.nanoTime()
            val file = Gdx.files.local(saveName)
            file.writeString(json.toJson(save), false)
            Logger.log("SaveLoad", "Saved game (non-threaded) in ${(TimeUtils.nanoTime() - start)/1000000000.0} seconds")
        }
    }

    private fun gatherGameState(game: GameScreen): SaveGamePOJO {
        val startTime = TimeUtils.nanoTime()

        val save = SaveGamePOJO()

        save.currTime = GameStats.TimeInfo.totalTimeCounter
        save.currMiles = GameStats.TravelInfo.totalDistTraveled
        save.maxTripMileage = GameStats.TravelInfo.totalDistOfGame

        GroupManager.getPeopleList().forEach {
            person -> save.personList.add(PersonPOJO(person.fullName, person.healthNormal + person.healthInjury,
                person.ailmentList, person.male, person.timeAdded))}

        SupplyManager.getSupplyList().forEach { supply -> save.supplyList.add(SupplyPOJO(supply.name, supply.amt, supply.currHealth)) }
        FunGameStats.statsMap.toList().forEach { stat -> save.funStatList.add(arrayOf(stat.first, stat.second)) }
        FunGameStats.uniqueStatsList.forEach { stat -> save.funStatUniqueList.add(arrayOf(stat.desc, stat.value)) }
        save.remainingEpicEvents.addAll(GameEventManager.getEventNameList("monthly"))
        game.timerList.forEach { pair -> save.eventTimers.add(arrayOf(pair.first, pair.second.remainingTime.toString())) }
        GameEventManager.delayedEventTimerList.forEach { timer ->
            val data:Array<String> = timer.userData as Array<String>
            save.delayedEventTimers.add(arrayOf(data[0], data[1], timer.remainingTime.toString(), data[2]))
        }

        Logger.log("SaveLoad", "Gathered game data in ${(TimeUtils.nanoTime() - startTime)/1000000000.0} seconds")

        return save
    }

    fun loadGame(game: GameScreen){
        json.setIgnoreUnknownFields(false)
        json.setUsePrototypes(true)

        val startTime = TimeUtils.nanoTime()

        val file = Gdx.files.local(saveName)
        val save = json.fromJson(SaveGamePOJO::class.java, file)

        GameStats.TimeInfo.totalTimeCounter = save.currTime
        GameStats.TravelInfo.totalDistTraveled = save.currMiles
        GameStats.TravelInfo.totalDistOfGame = save.maxTripMileage

        GroupManager.clearPeople()
        save.personList.forEach { jsonPerson ->
            val names = jsonPerson.name.split(" ")
            val person = Person(names[0], names[1], jsonPerson.male, jsonPerson.gameTimeAdded) //Make a new person to add to the group.
            person.addHealth(jsonPerson.health - person.totalMaxHealth) //We need to set the health through a bit of roundabout.
            jsonPerson.ailments.forEach { person.addAilment(it) }
            GroupManager.addPerson(person)
        }

        save.supplyList.forEach { supply ->
            val _supply = SupplyManager.getSupply(supply.name)
            _supply.amt = supply.currAmount
            _supply.currHealth = supply.currHealth
        }

        save.funStatList.forEach { stat ->
            FunGameStats.addFunStat(stat[0], stat[1])
        }

        save.funStatUniqueList.forEach { stat ->
            FunGameStats.addFunStat(stat[0], stat[1], true)
        }

        GameEventManager.getEventNameList("monthly").clear() //Let's clear this since it was probably loaded by the game
        GameEventManager.getEventNameList("monthly").addAll(save.remainingEpicEvents) //Load the remaining events in.

        save.eventTimers.forEach { list ->
            game.setTimer(list[0], list[1].toFloat())
        }

        GameEventManager.delayedEventTimerList.clear() //Might as well clear this first. Just in case?
        save.delayedEventTimers.forEach { list ->
            GameEventManager.addDelayedEvent(list[0], list[1], list[2].toFloat(), list[3].toInt())
        }

        GroupGUI.init()

        //TODO Trait saving/loading

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
        var funStatList:MutableList<Array<String>> = mutableListOf()
        var funStatUniqueList:MutableList<Array<String>> = mutableListOf()
        var remainingEpicEvents:MutableList<String> = mutableListOf()
        var eventTimers:MutableList<Array<String>> = mutableListOf()        // [evtType, remainingTime]
        var delayedEventTimers:MutableList<Array<String>> = mutableListOf() // [evtName, evtType, remainingTime, pageNumber]
    }

    private class PersonPOJO(var name:String, var health:Float, var ailments:List<Ailment>, var male:Boolean, val gameTimeAdded:Long){
        constructor():this("", 0f, listOf(), false, 0)
    }

    private class SupplyPOJO(var name:String, var currAmount:Float, var currHealth:Float){
        constructor():this("", 0f, 0f)
    }

}