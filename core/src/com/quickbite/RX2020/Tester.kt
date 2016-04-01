package com.quickbite.rx2020

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.managers.EventManager

/**
 * Created by Paha on 2/6/2016.
 */
object Tester {
    fun testEvents(numTests:Int){
        System.out.println("---------------------")

        for(i in 0.rangeTo(numTests)){
            var evt: DataManager.EventJson? = DataManager.EventJson.getRandomRoot()
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
    }
}