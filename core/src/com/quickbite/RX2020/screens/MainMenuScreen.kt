package com.quickbite.rx2020.screens

import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.quickbite.rx2020.SaveLoad
import com.quickbite.rx2020.TextGame

/**
 * Created by Paha on 2/3/2016.
 */
class MainMenuScreen(val game: TextGame) : Screen {
    val buttonTable: Table = Table()
    val titleTable:Table = Table()

    override fun show() {
        var labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        val style: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        style.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        style.fontColor = Color.BLACK

        val continueButton = TextButton("Continue", style);
        continueButton.label.setFontScale(0.4f)
        if(!SaveLoad.saveExists()) {
            continueButton.setColor(0f, 0f, 0f, 0.3f)
            continueButton.isDisabled = true
        }

        val startButton = TextButton("Start", style);
        startButton.label.setFontScale(0.4f)

        val titleLabel = Label("RX-2020", labelStyle)
        titleLabel.setFontScale(0.8f)

        startButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                TextGame.stage.clear()
                game.screen = GameIntroScreen(game)
            }
        })

        continueButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                TextGame.stage.clear()
                game.screen = GameScreen(game)
                SaveLoad.loadGame()
            }
        })

        titleTable.add(titleLabel).pad(20f)

        buttonTable.add(continueButton).width(150f).height(50f)
        buttonTable.row().padTop(20f)
        buttonTable.add(startButton).width(150f).height(50f).padBottom(50f)

        buttonTable.setFillParent(true)
        buttonTable.bottom()

        titleTable.setFillParent(true)
        titleTable.top()

        TextGame.stage.addActor(titleTable)
        TextGame.stage.addActor(buttonTable)
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