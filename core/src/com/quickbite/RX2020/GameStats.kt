package com.quickbite.rx2020

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.managers.SupplyManager
import com.quickbite.rx2020.screens.GameScreen

/**
 * Created by Paha on 2/8/2016.
 */
object GameStats {
    lateinit var game: GameScreen

    fun init(gameScreen: GameScreen){
        game = gameScreen
    }

    fun update(delta:Float){
        SupplyManager.update(delta)

        TimeInfo.totalTimeCounter+=delta
        TimeInfo.currTime = (TimeInfo.totalTimeCounter%TimeInfo.timeScale).toInt()

        if(TimeInfo.currTime != TimeInfo.lastTime){
            TimeInfo.lastTime = TimeInfo.currTime
            game.onHourTick(delta)
        }
    }

    fun updateTimeTick(){
        SupplyManager.updatePerTick()
        TravelInfo.totalDistTraveled += TravelInfo.currMPH
    }

    object TravelInfo{
        //Need total distance of the game, distance traveled, distance to go, mph traveling
        var totalDistOfGame:Int = MathUtils.random(36000, 108000)
            get
            private set

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
        var currTime:Int = 0
        var lastTime:Int = 0
        var timeOfDay:Int = 0
            get() {
                var _t = ((GameStats.TimeInfo.currTime.toInt())%12)
                if(_t == 0) _t = 12
                return _t
            }
        var totalDaysTraveled:Int = 0
            get() = (totalTimeCounter/timeScale).toInt() + 1

    }
}