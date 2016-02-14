package com.quickbite.game.managers

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Json
import java.io.BufferedReader
import java.util.*

/**
 * Created by Paha on 2/6/2016.
 */

object DataManager{
    private val rootEventMap: HashMap<String, EventJson> = HashMap() //For Json Events
    private val eventMap: HashMap<String, EventJson> = HashMap() //For Json Events

    private val randomFirstNameList:MutableList<String> = arrayListOf()
    private val randomLastNameList:MutableList<String> = arrayListOf()

    val json: Json = Json()

    fun loadEvents(dir:FileHandle){
        val list:Array<FileHandle> = dir.list()

        for(file: FileHandle in list){
            if(file.isDirectory)
                loadEvents(file)
            else {
                val event: EventJson = json.fromJson(EventJson::class.java, file)
                if (event.root) rootEventMap.put(file.nameWithoutExtension(), event)
                else eventMap.put(file.nameWithoutExtension(), event)
            }
        }
    }

    fun loadRandomNames(firstNameFile:FileHandle, lastNameFile:FileHandle){
        var reader:BufferedReader = BufferedReader(firstNameFile.reader());
        reader.forEachLine {line ->  randomFirstNameList += line }

        reader = BufferedReader(lastNameFile.reader());
        reader.forEachLine {line ->  randomLastNameList += line }
    }

    fun pullRandomName():Pair<String, String>{
        var index = MathUtils.random(0, randomFirstNameList.size - 1)
        val firstName = randomFirstNameList[index]
        randomFirstNameList.removeAt(index)

        index = MathUtils.random(0, randomFirstNameList.size - 1)
        val lastName = randomLastNameList[index]
        randomLastNameList.removeAt(index)

        return Pair(firstName, lastName)
    }

    class EventJson{
        var root:Boolean = false
        lateinit var name:String
        lateinit var description:String
        var choices:Array<String>? = null // The choices, like 'yes' or 'no' || 'Kill him', 'Let him go', 'Have him join you'
        var outcomes:Array<Array<String>>? = null //The possible outcomes for each choice, ie: 'He died', 'He killed you first!'
        var chances:Array<IntArray>? = null //The chances of each outcome happening
        var resultingAction:Array<String>? = null //The resulting action. This can be null on events that lead to other events. Not null if the event is a result and ends there.

        var randomName:String = ""

        /**
         * Selects another Event using a choice and chance.
         */
        fun select(choice:String, chance:Int): EventJson?{
            var outcomeIndex:Int = -1

            val choiceIndex:Int = choices!!.indexOf(choice) //Get the index of the choice
            //If our result is valid, find the outcome that is a result of it.
            if(choiceIndex >= 0){
                var counter:Int = 0 //Keep track of percentages
                if(chances!!.isEmpty()) //If the chances/outcomes are empty, return null
                     return null

                //For each outcome chance, increment counter. If the chance is less than the counter, that is our outcome.
                for(i in chances!![choiceIndex].indices){
                    counter += chances!![choiceIndex][i]
                    if(chance <= counter) {
                        outcomeIndex = i
                        break //break out
                    }
                }

                if(outcomeIndex < 0)
                    return null

                val outcomeEvent = DataManager.eventMap[outcomes!![choiceIndex][outcomeIndex]]!!
                outcomeEvent.randomName = GroupManager.getRandomPerson().firstName
                return outcomeEvent
            }

            return null
        }

        companion object{
            fun getRandomRoot():EventJson{
                val event = DataManager.rootEventMap.values.toTypedArray()[MathUtils.random(DataManager.rootEventMap.size-1)]
                event.randomName = GroupManager.getRandomPerson().firstName
                return event;
            }
        }
    }
}