package com.quickbite.rx2020.managers

import com.badlogic.gdx.Gdx
import java.util.*

/**
 * Created by Paha on 2/8/2016.
 */
object EventManager {
    val eventMap: HashMap<String, (args: List<Any>) -> Unit> = hashMapOf()

    fun onEvent(name:String, event:(args:List<Any>) -> Unit){
        eventMap.put(name, event)
    }

    fun callEvent(name:String, vararg args:Any){
        val func = eventMap[name]
        if(func != null)
            func(listOf(args[0]))
        else
            Gdx.app.error("EventSystem", "No event found for $name, is it spelled correctly?")
    }

    fun callEvent(name:String, args:List<Any>){
        val func = eventMap[name]
        if(func != null)
            func(args)
        else
            Gdx.app.error("EventSystem", "No event found for $name, is it spelled correctly?")
    }
}