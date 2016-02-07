package com.quickbite.game

import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener

/**
 * Created by Paha on 2/3/2016.
 */
class MainMenuScreen(val game:TextGame) : Screen {
    val table: Table = Table()

    override fun show() {
        //game.setScreen(GameScreen(game))
        DataManager.loadEvents()

        val style:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        style.font = TextGame.font
        style.fontColor = Color.BLACK

        val startButton = TextButton("Start", style);

        startButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                table.remove()
                game.setScreen(GameScreen(game))
            }
        })

        table.add(startButton).width(150f).height(50f)
        table.setFillParent(true)

        TextGame.stage.addActor(table)
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
        //throw UnsupportedOperationException()
    }

    override fun resume() {
        //throw UnsupportedOperationException()
    }

    override fun dispose() {
        //throw UnsupportedOperationException()
    }
}