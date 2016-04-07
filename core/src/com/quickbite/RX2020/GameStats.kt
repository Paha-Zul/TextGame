package com.quickbite.rx2020

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.managers.SupplyManager
import com.quickbite.rx2020.screens.GameScreen

/**
 * Created by Paha on 2/8/2016.
 */
object GameStats : Updateable {
    lateinit var game: GameScreen

    fun init(gameScreen: GameScreen){
        game = gameScreen
    }

    override fun update(delta:Float){
        SupplyManager.update(delta)

        TimeInfo.totalTimeCounter+=delta
        TimeInfo.currTime = (TimeInfo.totalTimeCounter%TimeInfo.timeScale).toInt()

        if(TimeInfo.currTime != TimeInfo.lastTime){
            TimeInfo.lastTime = TimeInfo.currTime
            game.onHourTick(delta)
        }
    }

    override fun updateHourly(delta:Float){
        SupplyManager.updateHourly(delta)
        TravelInfo.totalDistTraveled += TravelInfo.currMPH
    }

    object TravelInfo{
        //Need total distance of the game, distance traveled, distance to go, mph traveling
        var totalDistOfGame:Int = MathUtils.random(36000, 108000)
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
        var totalTimeCounter:Float = 0f
        var currTime:Int = 0 //The total time accumulated while traveling
        var lastTime:Int = 0 //The last tick of time. Only really used for determining when the hourly update should be called.
        var timeOfDay:Int = 0 //The time of day, formatted to 12 hour cycles.
            get() {
                var _t = ((GameStats.TimeInfo.currTime.toInt())%12)
                if(_t == 0) _t = 12
                return _t
            }

        var totalDaysTraveled:Int = 0
            get() = (totalTimeCounter/timeScale).toInt() + 1

    }
}