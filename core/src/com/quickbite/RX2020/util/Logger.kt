package com.quickbite.rx2020.util

import com.badlogic.gdx.Gdx

/**
 * Created by Paha on 3/27/2016.
 */
object Logger {
    var loggerEnabled = true
    var toConsole = true

    private val logPerm = arrayOf(true, true, true, true)
    private val consolePerm = arrayOf(true, true, true, true)

    private val logList:MutableList<String> = mutableListOf("Starting Logger")

    val logListArray:Array<String>
        get() = logList.toTypedArray()

    enum class LogLevel{
        Debug, Info, Warning, Error
    }

    @JvmStatic
    fun log(prefix:String, message:String, logLevel: LogLevel = LogLevel.Info){
        val string = "[$logLevel] [$prefix]: $message"
        if(toConsole && consolePerm[logLevel.ordinal]) {
            Gdx.app.log(prefix, message)
        }

        if(loggerEnabled && logPerm[logLevel.ordinal]) {
            logList.add(string)
        }
    }

    @JvmStatic
    fun writeLog(fileName:String){
        if(loggerEnabled) {
            val handle = Gdx.files.internal("$fileName")
            handle.file().printWriter().use { out ->
                logList.forEach {
                    out.println("$it")
                }
            }
        }
    }

    @JvmStatic
    fun setLogPerm(logLevel: LogLevel, value:Boolean){
        logPerm[logLevel.ordinal] = value
    }

    @JvmStatic
    fun setConsolePerm(logLevel: LogLevel, value:Boolean){
        logPerm[logLevel.ordinal] = value
    }

}