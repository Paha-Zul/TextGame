package com.quickbite.game

import com.badlogic.gdx.math.MathUtils
import com.quickbite.game.managers.SupplyManager
import com.quickbite.game.screens.GameScreen

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
            game.onTimeTick(delta)
        }
    }

    fun updateTimeTick(){
        SupplyManager.updatePerTick()
        TravelInfo.totalDistTraveled += TravelInfo.currMPH
    }

    object TravelInfo{
        //Need total distance of the game, distance traveled, distance to go, mph traveling
        var totalDistOfGame:Int = MathUtils.random(36000, 108000)
        var totalDistTraveled:Int = 0
        var totalDistToGo:Int = 0
        var currMPH:Int = 10
    }

    object TimeInfo{
        val timeScale:Int = 24
        var totalTimeCounter:Float = 0f
        var currTime:Int = 0
        var lastTime:Int = 0
    }
}