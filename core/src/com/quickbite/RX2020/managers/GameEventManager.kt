package com.quickbite.rx2020.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.gui.GameScreenGUI
import com.quickbite.rx2020.interfaces.IResetable
import com.quickbite.rx2020.interfaces.IUpdateable
import com.quickbite.rx2020.shuffle
import com.quickbite.rx2020.util.CustomTimer
import com.quickbite.rx2020.util.GH
import com.quickbite.rx2020.util.Logger
import java.util.*

/**
 * Created by Paha on 4/4/2016.
 */

object GameEventManager : IUpdateable, IResetable{
    var currActiveEvent:EventJson? = null
        get
        set(value){
            field = value
        }

    var lastCurrEvent:EventJson? = null //Mainly for debugging.

    private val rootMap:MutableMap<String, MutableList<String>> = hashMapOf() //For Event roots!
    val eventMap: HashMap<String, EventJson> = HashMap() //For Json Events

    val delayedEventTimerList:MutableList<CustomTimer> = mutableListOf() //A list of timers for delayed events

    override fun update(delta: Float) {
        for(i in (delayedEventTimerList.size - 1) downTo 0){
            delayedEventTimerList[i].update(delta)
            if(delayedEventTimerList[i].done)
                delayedEventTimerList.removeAt(i)
        }
    }

    override fun updateHourly(delta: Float) {

    }

    fun setNewRandomRoot(type:String):EventJson?{
        val event = getAndSetEvent("", type)
        currActiveEvent = event
        Logger.log("GameEventManager", "Picking new event ${event?.name} for type $type")
        return event
    }

    fun getEventNameList(type:String):MutableList<String>{
        return rootMap.getOrElse(type, {mutableListOf()})
    }

    fun addEvent(event:EventJson, type: String = ""){
        eventMap.put(event.name, event) //Add the event to the main map.

        //If the event is a root, add it to the right list for later use.
        if(event.root){
            rootMap.getOrPut(type, { mutableListOf()}).add(event.name)

        //Otherwise, add all non root events to the general use map
        }else
            GameEventManager.eventMap.put(event.name, event)
    }

    /**
     * Adds a one shot timer to fire a delayed event.
     * @param name The name of the event to call
     * @param type The type of the event to call. Leave as "" if it's not a root event.
     * @param seconds The seconds to wait until the event is fire.
     */
    fun addDelayedEvent(name:String, type:String, seconds:Float, page:Int = 0){
        val timer = CustomTimer(seconds, true, { GameScreenGUI.openEventGUI(this.getAndSetEvent(name, type)!!, page)})
        timer.userData = arrayOf(name, type, page.toString())
        delayedEventTimerList += timer
    }

    /**
     * Gets an event. If the type parameter is not supplied, gets the event from the non root event map.
     * @param eventName The name of the event to get. If "" (empty), will return a random event from the map.
     * @param type The type of event. If left out, gets an event from the non root event map
     * @return The event retrieved from the event map.
     */
    private fun getEvent(eventName:String="", type:String = "", randomizePeople:Boolean = false, randomizedPeopleList:List<Person>? = null):EventJson?{
        val list = getEventNameList(type)

        //Get the event either by name or randomly.
        val event:EventJson? = if(!eventName.isEmpty()) eventMap[eventName] else if(list.size > 0) eventMap[list[MathUtils.random(0, list.size-1)]] else null

        if(event == null) Logger.log("GameEventManager", "Event with name $eventName wasn't found in the $type map. Is it accidentally not marked as root? Does it even exist?")
        else {
            if (randomizePeople) event.randomPersonList = GroupManager.getPeopleList().copyOf().shuffle().toList()
            else if (randomizedPeopleList != null) event.randomPersonList = randomizedPeopleList

            //As a special case for the game, we only want 1 occurrence of each epic event.
            if (type == "epic") list.remove(event.name)

            GH.replaceEventDescription(event) //TODO Watch this. May need to thread it.
        }
        return event
    }

    fun getAndSetEvent(eventName:String, type:String = ""):EventJson?{
        val event = getEvent(eventName, type, true)
        currActiveEvent = event
        return event
    }

    class EventJson{
        var root:Boolean = false
        lateinit var name:String
        lateinit var title:String
        lateinit var description:Array<String>
        lateinit var modifiedDescription:Array<String>
        var choices:Array<String>? = null // The choices, like 'yes' or 'no' || 'Kill him', 'Let him go', 'Have him join you'
        var restrictions:Array<String>? = null //The restrictions on the choices.
        var outcomes:Array<Array<String>>? = null //The possible outcomes for each choice, ie: 'He died', 'He killed you first!'
        var chances:Array<FloatArray>? = null //The chances of each outcome happening
        /** The resulting actions. This can be null on events that lead to other events. Not null if the event is a result and ends there.*/
        var resultingAction:Array<Array<String>>? = null

        //Each time a root event is retrieved to start and event, this should be randomed to use for future events.
        var randomPersonList:List<Person> = listOf()

        val hasChoices:Boolean
            get() = choices != null && choices!!.isNotEmpty()

        /** If the event has outcomes (like a choice text but it's the end of the event, ie: "You died" */
        val hasOutcomes:Boolean
            get() = outcomes != null && outcomes!!.isNotEmpty() && outcomes!![0].isNotEmpty()

        val hasActions:Boolean
            get() = resultingAction != null && resultingAction!!.isNotEmpty() && resultingAction!![0].isNotEmpty()

        val hasDescriptions:Boolean
            get() = description.isNotEmpty() && !(description.size == 1 && description[0].isEmpty())

        val hasRestrictions:Boolean
            get() = restrictions != null && restrictions!!.isNotEmpty()

        /**
         * Selects another Event using a choice and chance.
         * @param choice The text of the choice (ie: 'Craft a Net')
         * @return The child event chosen by the choice and chance parameters, or null if no child events match the choice/chance or exist.
         */
        fun selectChildEvent(choice:String): EventJson?{
            var outcomeIndex:Int = -1
            val chance = MathUtils.random(100)

            val choiceIndex:Int = getChoiceIndex(choice)
            //If our result is valid, find the outcome that is a result of it.
            if(choiceIndex >= 0){
                if(chances!!.isEmpty()) { //If the chances/outcomes are empty, return null
                    Logger.log("DataManager", "Event $name with title $title doesn't have any chances for the outcomes. Returning null.")
                    return null
                }

                outcomeIndex = getOutcome(choiceIndex, chance)

                if(outcomeIndex < 0) { //If the outcomeIndex is negative, we have no outcome. Return null.
                    Logger.log("DataManager", "Event $name with choice ($choice) does not have any outcomes. This may be intended but notifying for the heck of it.", Logger.LogLevel.Info)
                    return null
                }

                val outcomeText = outcomes!![choiceIndex][outcomeIndex]
                val outcomeEvent = getEvent(outcomeText, "", false, this.randomPersonList)
                return outcomeEvent
            }

            return null
        }

        /**
         * Gets the index for the choice
         * @param choice The text choice (eg: 'Craft Net')
         * @return The index of the choice text in the event, 0 if there are no choices but there are outcomes, and -1 if there are no choices or outcomes.
         */
        private fun getChoiceIndex(choice:String):Int{
            val choiceIndex:Int = choices!!.indexOf(choice) //Get the index of the choice

            //If the result is -1 but we have outcomes, this is a special case. Return 0!
            if(choiceIndex == -1 && outcomes != null && outcomes!!.isNotEmpty())
                return 0

            return choiceIndex //Otherwise, return the choice index.
        }

        /**
         * Gets the outcome index.
         */
        private fun getOutcome(choiceIndex:Int, chance:Int):Int{
            var counter:Float = 0f
            var outcomeIndex = -1

            if((outcomes!!.size == 1 && (outcomes!![0].isEmpty() || outcomes!![0][0].isEmpty())))
                return -1

            if(chances!!.size != outcomes!!.size)
                Logger.log("GameEventManager", "The number of outcomes don't match the number of chances. This could be a problem.", Logger.LogLevel.Warning)

            //For each outcome chance, increment counter. If the chance is less than the counter, that is our outcome.
            for(i in chances!![choiceIndex].indices){
                if(chances!![choiceIndex][i] <= 0)
                    Logger.log("GameEventManager", "Chance for choice ${if(choices == null || choices!!.isEmpty()) "(no choice)" else choices!![choiceIndex]} under event ${this.name} is 0. Not good", Logger.LogLevel.Warning)
                counter += chances!![choiceIndex][i]
                if(chance <= counter) {
                    outcomeIndex = i
                    break //break out
                }
            }

            return outcomeIndex
        }
    }

    override fun reset() {
        delayedEventTimerList.clear()
    }
}