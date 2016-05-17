package com.quickbite.rx2020.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.interfaces.IUpdateable
import com.quickbite.rx2020.interfaces.IResetable
import com.quickbite.rx2020.screens.GameScreen

/**
 * Created by Paha on 2/8/2016.
 * Holds game stats like travel information (distance, speed, ect.) and time information (the total time traveled, current day, ect)
 */
object GameStats : IUpdateable, IResetable{
    lateinit var game: GameScreen
    var win:Boolean = false
    var gameOverStatus:String = "?"

    fun init(){
        TravelInfo.totalDistOfGame = MathUtils.random(36000, 108000)
    }

    override fun update(delta:Float){
        SupplyManager.update(delta)

        TimeInfo.totalTimeCounter+=delta
        TimeInfo.currTime = (TimeInfo.totalTimeCounter% TimeInfo.timeScale).toInt()

        if(TimeInfo.currTime != TimeInfo.lastTime){
            TimeInfo.lastTime = TimeInfo.currTime
            game.onHourTick(delta)
        }
    }

    override fun updateHourly(delta:Float){
        TravelInfo.totalDistTraveled += TravelInfo.currMPH
    }

    object TravelInfo{
        //Need total distance of the game, distance traveled, distance to go, mph traveling
        var totalDistOfGame:Int = 0
            get
            set

        var totalDistTraveled:Int = 0
            get
            set

        var currMPH:Int = 10
            get
            set

        var totalDistToGo:Int = 0
            get() = totalDistOfGame - totalDistTraveled
    }

    object TimeInfo{
        val timeScale:Int = 24
        var totalTimeCounter:Float = 0f //The total time traveled
        var currTime:Int = 0 //The current time of the current 24 hour day period
        var lastTime:Int = 0 //The last tick of time. Only really used for determining when the hourly update should be called.
        var timeOfDay:Int = 0 //The time of day, formatted to 12 hour cycles.
            get() {
                var _t = ((currTime.toInt())%12)
                if(_t == 0) _t = 12
                return _t
            }

        val totalDaysTraveled:Int
            get() = (totalTimeCounter / timeScale).toInt() + 1

    }

    override fun reset() {
        TravelInfo.totalDistToGo = 0
        TravelInfo.totalDistOfGame = 0
        TravelInfo.totalDistTraveled = 0

        TimeInfo.totalTimeCounter = 0f
        TimeInfo.currTime = 0
        TimeInfo.lastTime = 0
        TimeInfo.timeOfDay = 0
    }
}