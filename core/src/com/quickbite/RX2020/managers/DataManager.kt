package com.quickbite.rx2020.managers

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.TimeUtils
import com.quickbite.rx2020.util.Logger
import java.util.*

/**
 * Created by Paha on 2/6/2016.
 */

object DataManager{
    private val searchActivities: LinkedHashMap<String, SearchActivityJSON> = linkedMapOf() //For Json Events

    private val itemMap: LinkedHashMap<String, ItemJson> = linkedMapOf() //For Json Events

    private lateinit var names:NamesJson
    lateinit var end:EndJSON
        get
        private set

    val json: Json = Json()

    fun loadEvents(dir:FileHandle){
        val list:Array<FileHandle> = dir.list()
        val startTime:Long = TimeUtils.millis()

        for(file: FileHandle in list){
            if(file.isDirectory)
                loadEvents(file)
            else {
                val events: Array<GameEventManager.EventJson> = json.fromJson(Array<GameEventManager.EventJson>::class.java, file)
                var rootMap:HashMap<String, GameEventManager.EventJson>? = null
                if(file.name().equals("rare.json"))
                    rootMap = GameEventManager.rareRootEventMap
                else if(file.name().equals("common.json"))
                    rootMap = GameEventManager.commonRootEventMap
                else if(file.name().equals("epic.json"))
                    rootMap = GameEventManager.epicRootEventMap
                else if(file.name().equals("special.json"))
                    rootMap = GameEventManager.specialEvebtMap

                events.forEach { event ->
                    if (event.root) rootMap!!.put(event.name, event)
                    else GameEventManager.eventMap.put(event.name, event)
                }
            }
        }

        val time:Long = TimeUtils.millis() - startTime
        Logger.log("DataManager", "Took ${(time/1000f).toFloat()}s to load events.")
    }

    fun loadItems(dir:FileHandle){
        val startTime:Long = TimeUtils.millis()

        val items: Array<ItemJson> = json.fromJson(Array<ItemJson>::class.java, dir)
        items.forEach { item ->
            itemMap.put(item.name, item)
        }

        val time:Long = TimeUtils.millis() - startTime
        Logger.log("DataManager", "Took ${(time/1000f).toFloat()}s to load items.")
    }

    fun loadRandomNames(nameFileHandle:FileHandle){
        val startTime:Long = TimeUtils.millis()

        this.names = json.fromJson(NamesJson::class.java, nameFileHandle)

        val time:Long = TimeUtils.millis() - startTime
        Logger.log("DataManager", "Took ${(time/1000f).toFloat()}s to load names.")
    }

    fun loadSearchActivities(file:FileHandle){
        val activities: Array<SearchActivityJSON> = json.fromJson(Array<SearchActivityJSON>::class.java, file)
        activities.forEach { activity ->
            searchActivities.put(activity.buttonTitle, activity) //Hash it by the button title.
        }
    }

    fun loadEnd(file:FileHandle){
        this.end = json.fromJson(EndJSON::class.java, file)
    }

    fun pullRandomName():Triple<String, String, Boolean>{
        var firstName = ""
        var male = MathUtils.random(0, 100) > 50
        if(male)
            firstName = names.maleFirstNames[MathUtils.random(0, names.maleFirstNames.size-1)]
        else
            firstName = names.femaleFirstNames[MathUtils.random(0, names.femaleFirstNames.size-1)]

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
        var max:Int = 0
        var worth:Array<Int>? = null
        var perMember:Boolean = false
        var randStartAmt:Array<Int>? = null
        var affectedByHealth:Boolean = false
    }

    class SearchActivityJSON(){
        var name:String = "def"
        var description:String = "def"
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
        lateinit var femaleFirstNames:Array<String>
        lateinit var maleFirstNames:Array<String>
        lateinit var lastNames:Array<String>
    }

    class EndJSON(){
        lateinit var win:Array<String>
        lateinit var lose:Array<String>
    }
}