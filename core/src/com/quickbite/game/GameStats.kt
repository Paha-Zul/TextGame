package com.quickbite.game

import com.quickbite.game.managers.GroupManager
import com.quickbite.game.managers.SupplyManager
import com.quickbite.game.screens.GameScreen

/**
 * Created by Paha on 2/8/2016.
 */
object GameStats {
    lateinit var groupManager: GroupManager
    lateinit var supplyManager: SupplyManager
    lateinit var game: GameScreen

    fun init(gameScreen: GameScreen){
        game = gameScreen

        groupManager = GroupManager()
        supplyManager = SupplyManager(gameScreen, groupManager)
    }

    fun update(delta:Float){
        supplyManager.update(delta)

        TimeInfo.totalTimeCounter+=delta
        TimeInfo.currTime = (TimeInfo.totalTimeCounter%TimeInfo.timeScale).toInt()

        if(TimeInfo.currTime != TimeInfo.lastTime){
            TimeInfo.lastTime = TimeInfo.currTime
            game.onTimeTick(delta)
        }
    }

    fun updateTimeTick(){
        supplyManager.updatePerTick()
    }

    object TravelInfo{

    }

    object TimeInfo{
        val timeScale:Int = 24
        var totalTimeCounter:Float = 0f
        var currTime:Int = 0
        var lastTime:Int = 0
    }
}