package com.quickbite.rx2020.managers

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.TimeUtils
import com.quickbite.rx2020.util.Logger
import java.io.BufferedReader
import java.util.*

/**
 * Created by Paha on 2/6/2016.
 */

object DataManager{
    private val searchActivities: LinkedHashMap<String, SearchActivityJSON> = linkedMapOf() //For Json Events

    private val itemMap: LinkedHashMap<String, ItemJson> = linkedMapOf() //For Json Events

    private val randomFirstNameList:MutableList<String> = arrayListOf()
    private val randomLastNameList:MutableList<String> = arrayListOf()

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

    fun loadRandomNames(firstNameFile:FileHandle, lastNameFile:FileHandle){
        val startTime:Long = TimeUtils.millis()

        var reader:BufferedReader = BufferedReader(firstNameFile.reader());
        reader.forEachLine {line ->  randomFirstNameList += line }

        reader = BufferedReader(lastNameFile.reader());
        reader.forEachLine {line ->  randomLastNameList += line }

        val time:Long = TimeUtils.millis() - startTime
        Logger.log("DataManager", "Took ${(time/1000f).toFloat()}s to load names.")
    }

    fun loadSearchActivities(file:FileHandle){
        val activities: Array<SearchActivityJSON> = json.fromJson(Array<SearchActivityJSON>::class.java, file)
        activities.forEach { activity ->
            searchActivities.put(activity.buttonTitle, activity) //Hash it by the button title.
        }
    }

    fun pullRandomName():Pair<String, String>{
        var index = MathUtils.random(0, randomFirstNameList.size - 1)
        val firstName = randomFirstNameList[index]
        randomFirstNameList.removeAt(index)

        index = MathUtils.random(0, randomFirstNameList.size - 1)
        val lastName = randomLastNameList[index]
        randomLastNameList.removeAt(index)

        return Pair(firstName, lastName)
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
}