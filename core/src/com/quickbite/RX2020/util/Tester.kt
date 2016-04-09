package com.quickbite.rx2020.util

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.managers.EventManager
import com.quickbite.rx2020.managers.GameEventManager

/**
 * Created by Paha on 2/6/2016.
 */
object Tester {
    fun testEvents(numTests:Int){
        System.out.println("---------------------")

        //TODO This needs to be more thorough and check every choice and outcome (FREAKING BAD)

        val func = {event:GameEventManager.EventJson ->
            var evt:GameEventManager.EventJson? = event
            System.out.println("Event: "+evt?.name)

            //While the event is no null and has choices, loop over them
            while(evt != null && !evt.choices!!.isEmpty()){
                val index = MathUtils.random(evt.choices!!.size - 1)
                val choice = evt.choices!![index]

                //Execute any actions
                val list = evt.resultingAction;
                if(list != null) {
                    for (params in list.iterator()) {
                        if (params.size > 0) {
                            EventManager.callEvent(params[0], params.slice(1.rangeTo(params.size-1)))
                        }
                    }
                }

                //Select a new event choice.
                System.out.println("selecting $choice")
                evt = evt.selectChildEvent(choice)
                System.out.println("Selected "+evt?.name)
            }

            System.out.println("Result: "+evt?.resultingAction)
            System.out.println("---------------------")
        }

        for(event in GameEventManager.commonRootEventMap.values)
            func(event)

        for(event in GameEventManager.rareRootEventMap.values)
            func(event)
    }

    fun getRandom():String{
        val rand = MathUtils.random(1, 100)

        when{
            //TODO Don't forget epic events when they are actually implemented
            rand >= 50 -> return "common"
//            rand >= 66 -> return "rare"
            else -> return "rare"
        }
    }
}