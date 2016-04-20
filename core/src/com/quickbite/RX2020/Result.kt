package com.quickbite.rx2020

import com.quickbite.rx2020.gui.GameScreenGUI

/**
 * Created by Paha on 4/5/2016.
 */
class Result(val name:String, var amt:Float, val desc:String = "", var timeLastUpdated:Double = 0.0) {

    companion object{
        var eventChangeMap:MutableMap<String, Result> = mutableMapOf()
            get
            private set

        var recentChangeMap:MutableMap<String, Result> = mutableMapOf()
            get
            private set

        var eventDeathMap:MutableMap<String, Result> = mutableMapOf()
            get
            private set

        var recentDeathMap:MutableMap<String, Result> = mutableMapOf()
            get
            private set

        private val hangTime = 3

        val hasEventResults:Boolean
            get() = eventChangeMap.size > 0 || eventDeathMap.size > 0

        fun clearResultLists(){
            eventChangeMap = mutableMapOf()
            eventDeathMap = mutableMapOf()
        }

        /**
         * Adds a recent change
         * @param name The name to use for the map key. Usually display name for supply, first name for people.
         * @param amt The amount the change was. For instance, someone losing 50 health is -50, gaining 50 energy is simply 50.
         * @param currTime The time the change happened (which is the current time.) This will be used to update the recent supply and health changes.
         * @param gui The GameScreenGUI to update if needed on change.
         * @param isEventRelated True if this has to do with an event, false if it's only for recent non-event changes.
         */
        fun addRecentChange(name: String, amt: Float, currTime: Double, desc: String = "", gui: GameScreenGUI, isEventRelated:Boolean){
            var result = eventChangeMap.getOrPut(name, {Result(name, 0f, desc)})
            result.amt += amt

            //TODO A little bit of a hack until I figure out where to better put the values.
            if(isEventRelated) {
                result = recentChangeMap.getOrPut(name, { Result(name, 0f, desc) })
                result.amt += amt
                result.timeLastUpdated = currTime
            }
        }

        /**
         * Adds a recent death.
         * @param person The person to add. First name for map key, full name for displaying.
         * @param isEventRelated True if this has to do with an event, false if it's only for recent non-event changes.
         */
        fun addRecentDeath(person:Person, isEventRelated: Boolean){
            recentDeathMap.put(person.firstName, Result(person.fullName, 0f, " died"))

            if(isEventRelated)
                eventDeathMap.put(person.firstName, Result(person.fullName, 0f, " died"))
        }

        /**
         * Adds a recent death.
         * @param firstName The first name of the person that died. This is for putting in the map.
         * @param fullName The full name of the person that died. This is for displaying
         * @param isEventRelated True if this has to do with an event, false if it's only for recent non-event changes.
         */
        fun addRecentDeath(firstName:String, fullName:String, isEventRelated: Boolean){
            recentDeathMap.put(firstName, Result(fullName, 0f, " died"))

            if(isEventRelated)
                eventDeathMap.put(firstName, Result(fullName, 0f, " died"))
        }

        /**
         * Clears all recent (non-event) results.
         */
        fun purgeRecentResults(currTime: Double){
            var list:List<Result> = recentChangeMap.values.toList()

            for(result in list){
                if(result.timeLastUpdated + hangTime <= currTime) {
                    recentChangeMap.remove(result.name)
                }
            }
        }

        fun purgeRecentDeaths(){
            recentDeathMap.clear()
        }

        /**
         * Clears all recent deaths
         */
        fun purgeEventResults(){
            recentChangeMap.clear()
            eventDeathMap.clear()
        }


    }
}