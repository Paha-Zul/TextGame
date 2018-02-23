package com.quickbite.rx2020.util

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.managers.EventManager
import com.quickbite.rx2020.managers.GameEventManager
import java.util.*

/**
 * Created by Paha on 2/6/2016.
 *
 * A test object to test connections between all events in the event json files. This will screw up game data so only use for testing!
 *
 * <p>Takes each root event and runs through each child event, storing each child name in a hashmap. If the hashmap
 * is not empty at the end, each event in the map will be printed to indicate it is not linked to another event. This doesn't
 * always mean something went wrong, as special events (game over texts) aren't linked to any root event.</p>
 *
 * <p> When testEvents() is run, real supplies in the game are modified. Do not use this on an existing save</p>
 */
object Tester {
    const val TESTING = false
    private val eventMap:HashSet<String> = hashSetOf()

    fun testEvents(numTests:Int){
        //Loop over every mapped event and add them to the local event map for testing
        GameEventManager.eventMap.values.forEach { evt -> eventMap.add(evt.name) }

        System.out.println("---------------------")

        //TODO This needs to be more thorough and check every choice and outcome (FREAKING BAD)

        //A func to print out the event name and test it
        val func = {event:GameEventManager.EventJson ->
            val evt:GameEventManager.EventJson = event
            System.out.println("Event: "+evt.name)

            testEvent(evt)

            System.out.println("---------------------")
        }

        //Check all the dailies
        for(eventName in GameEventManager.getEventNameList("daily"))
            func(GameEventManager.getAndSetEvent(eventName, "daily")!!)

        //Check all the weekly
        for(eventName in GameEventManager.getEventNameList("weekly"))
            func(GameEventManager.getAndSetEvent(eventName, "weekly")!!)

        //We use .toList() to make a copy because of the specialness of epic events.
        for(eventName in GameEventManager.getEventNameList("monthly").toList())
            func(GameEventManager.getAndSetEvent(eventName, "monthly")!!)

        //We use .toList() to make a copy because of the specialness of monthly native events
        for(eventName in GameEventManager.getEventNameList("monthlyNative").toList())
            func(GameEventManager.getAndSetEvent(eventName, "monthlyNative")!!)

        //We use .toList() to make a copy because of the specialness of return events (delayed)
        for(eventName in GameEventManager.getEventNameList("returnEvents").toList())
            func(GameEventManager.getAndSetEvent(eventName, "returnEvents")!!)

        System.out.println("Event Testing Done!")

        //If the event map is not empty, notify of every event left in here
        if(!eventMap.isEmpty()) {
            Logger.log("Tester", "Event map is not empty. This means that something was not linked up. Map:")

            val list = eventMap.toList()
            list.forEach {evtName ->
                Logger.log("Tester - evts", evtName)
            }
        }
    }

    private fun testEvent(event:GameEventManager.EventJson){
        //Remove the event from the map
        eventMap.remove(event.name)
        //Go over all the outcomes individually
        if(event.outcomes!=null){
            event.outcomes!!.forEachIndexed { i, list -> list.forEachIndexed { j, outcomeName ->
                eventMap.remove(outcomeName)
//                val _evt = GameEventManager.getEvent(outcomeName)
                val _evt = GameEventManager.getAndSetEvent(outcomeName)!!
                _evt.randomPersonList = event.randomPersonList
                System.out.println("Testing ${_evt.name}") //Print we are testing
                testEvent(_evt) //Recursively tes the child event
            }}
        }

        //If we have actions, execute them.
        testActions(event)
    }

    private fun testActions(event:GameEventManager.EventJson){
//        GameEventManager.currActiveEvent = event
        //Execute any actions
        val list = event.resultingAction;
        if(list != null) {
            for (params in list.iterator()) {
                if (params.isNotEmpty()) {
                    System.out.println("Calling action ${params[0]}")
                    EventManager.callEvent(params[0], params.slice(1 until params.size))
                }
            }
        }
    }

    private fun getRandom():String{
        val rand = MathUtils.random(1, 100)

        return when{
        //TODO Don't forget epic events when they are actually implemented
            rand >= 50 -> "common"
//            rand >= 66 -> return "rare"
            else -> "rare"
        }
    }
}