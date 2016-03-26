package com.quickbite.rx2020

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.managers.DataManager

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
                val chance = MathUtils.random(100)

                System.out.println("selecting $choice with chance of $chance")

                evt = evt.select(choice, chance)

                System.out.println("Selected "+evt?.name)
            }

            System.out.println("Result: "+evt?.resultingAction)
            System.out.println("---------------------")
        }
    }
}