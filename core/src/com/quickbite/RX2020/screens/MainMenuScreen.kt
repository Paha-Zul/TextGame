package com.quickbite.rx2020.screens

import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.Result
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.gui.MainMenuGUI
import com.quickbite.rx2020.managers.*
import com.quickbite.rx2020.util.CustomTimer
import com.quickbite.rx2020.util.FunGameStats
import com.quickbite.rx2020.util.Logger

/**
 * Created by Paha on 2/3/2016.
 */
class MainMenuScreen(val game: TextGame) : Screen {

    override fun show() {
        TextGame.backgroundColor = Color(0f,0f,0f,1f)

        //Reset everything
        FunGameStats.reset()
        GroupManager.reset()
        EventManager.reset()
        GameStats.reset()
        SupplyManager.reset()
        ROVManager.reset()
        Result.reset()
        CustomTimer.reset()
        ChainTask.reset()

        GameStats.init()

        MainMenuGUI(this).showMainMenu()

        Logger.writeLog("log.txt")
    }

    override fun hide() {
        //throw UnsupportedOperationException()
    }

    override fun resize(width: Int, height: Int) {
        //throw UnsupportedOperationException()
    }

    override fun pause() {
        //throw UnsupportedOperationException()
    }

    override fun render(delta: Float) {
        TextGame.stage.draw()
    }

    override fun resume() {
        //throw UnsupportedOperationException()
    }

    override fun dispose() {
        //throw UnsupportedOperationException()
    }

}