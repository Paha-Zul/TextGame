package com.quickbite.rx2020.managers

import com.quickbite.rx2020.Person
import com.quickbite.rx2020.Result
import com.quickbite.rx2020.interfaces.IResetable
import com.quickbite.rx2020.util.GH

/**
 * Created by Paha on 6/18/2017.
 *
 * A manager object to manage results and static data using the Result class.
 */
object ResultManager : IResetable{
    var eventChangeMap:MutableMap<String, Result> = mutableMapOf()
        get
        private set

    var recentChangeMap:MutableMap<String, Result> = mutableMapOf()
        get
        private set

    var recentDeathMap:MutableMap<String, Result> = mutableMapOf()
        get
        private set

    var recentDeathResult: Result? = null
        get
        set

    /** The time for the change to hang around on the GUI */
    const private val hangTime = 3

    val hasEventResults:Boolean
        get() = eventChangeMap.isNotEmpty() || recentDeathResult != null

    fun clearResultLists(){
        eventChangeMap = mutableMapOf()
    }

    /**
     * Adds a recent change
     * @param name The name to use for the map key. Usually display name for supply, first name for people.
     * @param amt The amount the change was. For instance, someone losing 50 health is -50, gaining 50 energy is simply 50.
     * @param currTime The time the change happened (which is the current time.) This will be used to update the recent supply and health changes.
     * @param isEventRelated True if this has to do with an event, false if it's only for recent non-event changes.
     */
    fun addRecentChange(name: String, amt: Float, currTime: Double, desc: String = "", isEventRelated: Boolean){
        //TODO A little bit of a hack until I figure out where to better put the values.
        if(isEventRelated) {
            val result = eventChangeMap.getOrPut(name, { Result(name, 0f, desc) })
            if(result.amt<0 != amt<0) //If both are not the same sign, simply assign the new amt to the result
                result.amt = amt
            else //Otherwise, add on to it
                result.amt += amt
        }else{
            val result = recentChangeMap.getOrPut(name, { Result(name, 0f, desc) })
            if(result.amt<0 != amt<0) //If both are not the same sign, simply assign the new amt to the result
                result.amt = amt
            else //Otherwise, add on to it
                result.amt += amt
            result.timeLastUpdated = currTime
        }
    }

    /**
     * Adds a recent death.
     * @param person The person to add. First name for map key, full name for displaying.
     */
    fun addRecentDeath(person: Person){
        val text = GH.specialDeathTextReplacement(" has died. (He1) survived for %t before (his1) untimely demise.", person)
        recentDeathMap.put(person.firstName, Result(person.fullName, 0f, text))
    }

    /**
     * Adds a recent death.
     * @param firstName The first name of the person that died. This is for putting in the map.
     * @param fullName The full name of the person that died. This is for displaying
     * @param isEventRelated True if this has to do with an event, false if it's only for recent non-event changes.
     */
    fun addRecentDeath(firstName:String, fullName:String, desc: String){
        recentDeathMap.put(firstName, Result(fullName, 0f, desc))
    }

    /**
     * Clears all recent (non-event) results.
     */
    fun purgeRecentResults(currTime: Double){
        val list:List<Result> = recentChangeMap.values.toList()

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
    }

    override fun reset() {
        purgeEventResults()
        purgeRecentDeaths()
    }
}