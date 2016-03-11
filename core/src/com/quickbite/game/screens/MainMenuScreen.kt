package com.quickbite.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.quickbite.game.Game
import com.quickbite.game.managers.DataManager

/**
 * Created by Paha on 2/3/2016.
 */
class MainMenuScreen(val game: Game) : Screen {
    val buttonTable: Table = Table()
    val titleTable:Table = Table()

    override fun show() {
        //game.setScreen(GameScreen(game))

        var labelStyle:Label.LabelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        val style: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        style.font = Game.manager.get("spaceFont2", BitmapFont::class.java)
        style.fontColor = Color.BLACK

        val contButton = TextButton("Continue (does nothing)", style);
        contButton.label.setFontScale(0.4f)
        val startButton = TextButton("Start", style);
        startButton.label.setFontScale(0.4f)

        val titleLabel = Label("RX-2020", labelStyle)
        titleLabel.setFontScale(0.8f)

        startButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                buttonTable.remove()
                game.screen = GameIntroScreen(game)
            }
        })

        titleTable.add(titleLabel).pad(20f)

        buttonTable.add(contButton).width(150f).height(50f)
        buttonTable.row().padTop(20f)
        buttonTable.add(startButton).width(150f).height(50f).padBottom(50f)

        buttonTable.setFillParent(true)
        buttonTable.bottom()

        titleTable.setFillParent(true)
        titleTable.top()

        Game.stage.addActor(titleTable)
        Game.stage.addActor(buttonTable)
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
        Game.stage.draw()
        //throw UnsupportedOperationException()
    }

    override fun resume() {
        //throw UnsupportedOperationException()
    }

    override fun dispose() {
        //throw UnsupportedOperationException()
    }
}