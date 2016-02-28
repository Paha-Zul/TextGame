package com.quickbite.game.screens

import com.badlogic.gdx.Screen
import com.quickbite.game.GameIntroGUI
import com.quickbite.game.Game

/**
 * Created by Paha on 2/10/2016.
 */
class GameIntroScreen(val game: Game) : Screen {
    val gameIntroGUI:GameIntroGUI = GameIntroGUI(this)
    var done:Boolean = false

    override fun show() {
        gameIntroGUI.firstPage()
    }

    override fun hide() {
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun render(delta: Float) {
        Game.stage.draw()
        gameIntroGUI.update(delta)

        if(done)
            game.screen = GameScreen(game)
    }

    override fun resume() {
    }

    override fun dispose() {
    }
}