package com.quickbite.rx2020.managers

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.TimeUtils
import com.quickbite.rx2020.shuffle
import com.quickbite.rx2020.util.Logger
import com.quickbite.rx2020.util.Reward
import java.util.*

/**
 * Created by Paha on 2/6/2016.
 */

object DataManager{
    private val searchActivities: LinkedHashMap<String, SearchActivityJSON> = linkedMapOf() //For Json Events

    private val itemMap: LinkedHashMap<String, ItemJson> = linkedMapOf() //For Json Events

    var eventDir:FileHandle? = null
    var itemsDir:FileHandle? = null
    var namesDir:FileHandle? = null
    var activitiesDir:FileHandle? = null
    var rewardsDir:FileHandle? = null
    var endDir:FileHandle? = null

    private var tick = 0

    private lateinit var names:NamesJson
    lateinit var end:EndJSON
        get
        private set

    val json: Json = Json()

    /**
     * Updates (loads) the data queued for loading. Over separate frames.
     * @return True when done, false otherwise.
     */
    fun updateLoadData():Boolean{
        when(tick){
            0 -> loadEnd(endDir!!)
            1 -> loadItems(itemsDir!!)
            2 -> loadEvents(eventDir!!)
            3 -> loadRandomNames(namesDir!!)
            4 -> loadRewards(rewardsDir!!)
            5 -> {loadSearchActivities(activitiesDir!!)}
            6 -> return true;
        }

        tick++;
        return false
    }

    private fun loadEvents(dir:FileHandle){
        val list:Array<FileHandle> = dir.list()
        val startTime:Long = TimeUtils.millis()

        for(file: FileHandle in list){
            if(file.isDirectory)
                loadEvents(file)
            else {
                val events: Array<GameEventManager.EventJson> = json.fromJson(Array<GameEventManager.EventJson>::class.java, file)
                val type = file.name().substring(0, file.name().lastIndexOf('.'))

                events.forEach { event ->
                    GameEventManager.addEvent(event, type)
                }
            }
        }

        val time:Long = TimeUtils.millis() - startTime
        Logger.log("DataManager", "Took ${(time/1000f).toFloat()}s to load events.")
    }

    private fun loadItems(dir:FileHandle){
        val startTime:Long = TimeUtils.millis()

        val items: Array<ItemJson> = json.fromJson(Array<ItemJson>::class.java, dir)
        items.forEach { item ->
            itemMap.put(item.name, item)
        }

        val time:Long = TimeUtils.millis() - startTime
        Logger.log("DataManager", "Took ${(time/1000f).toFloat()}s to load items.")
    }

    private fun loadRandomNames(nameFileHandle:FileHandle){
        val startTime:Long = TimeUtils.millis()

        this.names = json.fromJson(NamesJson::class.java, nameFileHandle)
        this.names.maleFirstNames= this.names.maleFirstNames.shuffle()
        this.names.femaleFirstNames= this.names.femaleFirstNames.shuffle()
        this.names.lastNames= this.names.lastNames.shuffle()

        val time:Long = TimeUtils.millis() - startTime
        Logger.log("DataManager", "Took ${(time/1000f).toFloat()}s to load names.")
    }

    private fun loadSearchActivities(file:FileHandle){
        val startTime:Long = TimeUtils.millis()

        val activities: Array<SearchActivityJSON> = json.fromJson(Array<SearchActivityJSON>::class.java, file)
        activities.forEach { activity ->
            searchActivities.put(activity.buttonTitle, activity) //Hash it by the button title.
        }

        val time:Long = TimeUtils.millis() - startTime
        Logger.log("DataManager", "Took ${(time/1000f).toFloat()}s to load search activities.")
    }

    private fun loadRewards(nameFileHandle:FileHandle){
        val startTime:Long = TimeUtils.millis()

        val rewards = json.fromJson(Array<Reward>::class.java, nameFileHandle)
        rewards.forEach { reward -> Reward.rewardMap.put(reward.name, reward) }

        val time:Long = TimeUtils.millis() - startTime
        Logger.log("DataManager", "Took ${(time/1000f).toFloat()}s to load rewards.")
    }

    private fun loadEnd(file:FileHandle){
        this.end = json.fromJson(EndJSON::class.java, file)
    }

    fun pullRandomName():Triple<String, String, Boolean>{
        var firstName = ""
        val male = MathUtils.random(0, 100) > 50
        if(male)
            firstName = names.maleFirstNames.removeAt(names.maleFirstNames.size-1)
        else
            firstName = names.femaleFirstNames.removeAt(names.femaleFirstNames.size-1)

        val lastName = names.lastNames[MathUtils.random(0, names.lastNames.size - 1)]

        return Triple(firstName, lastName, male)
    }

    fun getSearchActiviesList() = searchActivities.values.toList()

    fun getItemList() = itemMap.values.toList()

    fun getItem(name:String) = itemMap[name]

    class ItemJson{
        var name:String = ""
        var abbrName:String = ""
        var displayName:String = ""
        var type:String = ""
        var max:Int = 0
        var worth:Array<Int>? = null
        var perMember:Boolean = false
        var randStartAmt:Array<Int>? = null
        var affectedByHealth:Boolean = false
    }

    class TraitJson{
        var name:String = ""
        lateinit var effects:Array<TraitEffectJson>
    }

    class TraitEffectJson{
        var affects:String = ""
        var scope:String = ""
        var subType:String = ""
        var subName:String = ""
        var subCommand:String = ""
        var amount:Float = 0.0f
        var percent = true
    }

    class SearchActivityJSON(){
        var name:String = "def"
        var description:Array<Array<String>> = arrayOf()
        var buttonTitle:String = "fixme"
        var action:Array<Array<String>>? = null
        var restrictions:Array<Array<String>>? = null

        companion object{
            /**
             * Returns a search activity that has a name
             * @param name The name of the search activity
             * @return The search activity found, or null if not found.
             */
            fun getSearchActivity(name:String): SearchActivityJSON? = searchActivities[name]
        }
    }

    class NamesJson(){
        lateinit var femaleFirstNames:MutableList<String>
        lateinit var maleFirstNames:MutableList<String>
        lateinit var lastNames:MutableList<String>
    }

    class EndJSON(){
        lateinit var win:Array<String>
        lateinit var solar:Array<String>
        lateinit var tracks:Array<String>
        lateinit var batteries:Array<String>
        lateinit var storage:Array<String>
        lateinit var energy:Array<String>
        lateinit var ROV:Array<String>
        lateinit var crew:Array<String>
        lateinit var deathText:Array<String>
    }
}