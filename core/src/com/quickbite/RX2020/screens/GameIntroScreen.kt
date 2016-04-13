package com.quickbite.rx2020.screens

import com.badlogic.gdx.Screen
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.gui.GameIntroGUI

/**
 * Created by Paha on 2/10/2016.
 */
class GameIntroScreen(val game: TextGame) : Screen {
    val gameIntroGUI: GameIntroGUI = GameIntroGUI(this)
    var done:Boolean = false

    override fun show() {
        gameIntroGUI
    }

    override fun hide() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun render(delta: Float) {
        TextGame.stage.draw()
        gameIntroGUI.update(delta)

        if(done)
            game.screen = GameScreen(game)
    }

    override fun resume() {
    }

    override fun dispose() {
    }
}