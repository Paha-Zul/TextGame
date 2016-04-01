package com.quickbite.rx2020

import com.badlogic.gdx.Gdx

/**
 * Created by Paha on 3/27/2016.
 */
object Logger {
    var loggerEnabled = true
    var toConsole = true

    private val logList:MutableList<String> = mutableListOf("Starting Logger")

    enum class LogLevel{
        Info, Warning, Error
    }

    @JvmStatic
    fun log(prefix:String, message:String, logLevel: LogLevel = LogLevel.Info){
        val string = "[$logLevel] [$prefix]: $message"
        if(toConsole) Gdx.app.log(prefix, message)
        logList.add(string)
    }

    @JvmStatic
    fun writeLog(fileName:String){
       val handle = Gdx.files.internal("$fileName")
        handle.file().printWriter().use { out ->
            logList.forEach {
                out.println("$it")
            }
        }
    }

}