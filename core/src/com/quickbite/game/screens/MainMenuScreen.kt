package com.quickbite.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.quickbite.game.TextGame
import com.quickbite.game.managers.DataManager

/**
 * Created by Paha on 2/3/2016.
 */
class MainMenuScreen(val game: TextGame) : Screen {
    val table: Table = Table()

    override fun show() {
        //game.setScreen(GameScreen(game))
        DataManager.loadEvents(Gdx.files.internal("files/events/"))
        DataManager.loadRandomNames(Gdx.files.internal("files/text/firstNames.txt"), Gdx.files.internal("files/text/lastNames.txt"))
        DataManager.loadSearchActivities(Gdx.files.internal("files/searchActivities.json"))

        val style: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        style.font = TextGame.font
        style.fontColor = Color.BLACK

        val startButton = TextButton("Start", style);

        startButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                table.remove()
                game.screen = GameIntroScreen(game)
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