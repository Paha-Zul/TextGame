package com.quickbite.rx2020.screens

import com.badlogic.gdx.Screen
import com.quickbite.rx2020.util.SaveLoad
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.gui.GameOverGUI

/**
 * Created by Paha on 5/3/2016.
 */
class GameOverScreen(val game:TextGame) : Screen {
    lateinit var gameOverGUI:GameOverGUI;

    override fun show() {
        gameOverGUI = GameOverGUI(game)
        gameOverGUI.gameOver()
    }

    override fun pause() {

    }

    override fun resize(p0: Int, p1: Int) {

    }

    override fun hide() {

    }

    override fun render(p0: Float) {
        TextGame.stage.draw()
    }

    override fun resume() {

    }

    override fun dispose() {

    }
}