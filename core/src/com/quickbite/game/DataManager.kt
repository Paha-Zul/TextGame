package com.quickbite.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Json
import java.util.*

/**
 * Created by Paha on 2/6/2016.
 */

object DataManager{
    val rootEventMap:HashMap<String, EventJson> = HashMap() //For Json Events
    val eventMap:HashMap<String, EventJson> = HashMap() //For Json Events

    val json: Json = Json()

    fun loadEvents(){
        val handle:FileHandle = Gdx.files.internal("files/events/")
        val list:Array<FileHandle> = handle.list()

        for(file:FileHandle in list){
            val event:EventJson = json.fromJson(EventJson::class.java, file)
            if(event.root) rootEventMap.put(file.nameWithoutExtension(), event)
            else eventMap.put(file.nameWithoutExtension(), event)
        }
    }

    class EventJson{
        var root:Boolean = false
        lateinit var name:String
        lateinit var description:String
        var choices:Array<String>? = null // The choices, like 'yes' or 'no' || 'Kill him', 'Let him go', 'Have him join you'
        var outcomes:Array<Array<String>>? = null //The possible outcomes for each choice, ie: 'He died', 'He killed you first!'
        var chances:Array<IntArray>? = null //The chances of each outcome happening
        var resultingAction:String? = null //The resulting action. This can be null on events that lead to other events. Not null if the event is a result and ends there.

        fun selected(choice:String, chance:Int):EventJson?{
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

                val outcomeEvent = DataManager.eventMap[outcomes!![choiceIndex][outcomeIndex]]
                return outcomeEvent
            }

            return null
        }
    }
}