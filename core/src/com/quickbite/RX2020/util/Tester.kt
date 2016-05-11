package com.quickbite.rx2020.util

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.managers.EventManager
import com.quickbite.rx2020.managers.GameEventManager

/**
 * Created by Paha on 2/6/2016.
 */
object Tester {
    val eventMap:MutableMap<String, GameEventManager.EventJson> = hashMapOf()

    fun testEvents(numTests:Int){
        eventMap.putAll(GameEventManager.getMap("common"))
        eventMap.putAll(GameEventManager.getMap("rare"))

        System.out.println("---------------------")

        //TODO This needs to be more thorough and check every choice and outcome (FREAKING BAD)

        val func = {event:GameEventManager.EventJson ->
            var evt:GameEventManager.EventJson = event
            System.out.println("Event: "+evt.name)

            testEvent(evt)

            System.out.println("---------------------")
        }

        for(eventName in GameEventManager.commonRootEventMap.keys)
            func(GameEventManager.getEvent(eventName, "common"))

        for(eventName in GameEventManager.rareRootEventMap.keys)
            func(GameEventManager.getEvent(eventName, "rare"))

        System.out.println("Event Testing Done!")

        assert(eventMap.isEmpty())

        if(!eventMap.isEmpty()) {
            Logger.log("Tester", "Event map is not empty. This means that something was not linked up. Map:")

            val list = eventMap.toList()
            list.forEach {evt ->
                Logger.log("Tester - evts", evt.first)
            }
        }
    }

    private fun testEvent(event:GameEventManager.EventJson){
        eventMap.remove(event.name)
        //Go over all the outcomes individually
        if(event.outcomes!=null){
            event.outcomes!!.forEachIndexed { i, list -> list.forEachIndexed { j, outcomeName ->
                eventMap.remove(outcomeName)
                val _evt = GameEventManager.getEvent(outcomeName)
                _evt.randomPersonList = event.randomPersonList
                System.out.println("Testing ${_evt.name}")
                testEvent(_evt)
            }}
        }

        //If we have actions, execute them.
        testActions(event)
    }

    private fun testActions(event:GameEventManager.EventJson){
        GameEventManager.currActiveEvent = event
        //Execute any actions
        val list = event.resultingAction;
        if(list != null) {
            for (params in list.iterator()) {
                if (params.size > 0) {
                    System.out.println("Calling action ${params[0]}")
                    EventManager.callEvent(params[0], params.slice(1.rangeTo(params.size-1)))
                }
            }
        }
    }

    private fun getRandom():String{
        val rand = MathUtils.random(1, 100)

        when{
            //TODO Don't forget epic events when they are actually implemented
            rand >= 50 -> return "common"
//            rand >= 66 -> return "rare"
            else -> return "rare"
        }
    }
}