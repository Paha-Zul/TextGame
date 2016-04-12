package com.quickbite.rx2020.screens

import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.SaveLoad
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.managers.SupplyManager
import com.quickbite.rx2020.util.GH

/**
 * Created by Paha on 2/3/2016.
 */
class MainMenuScreen(val game: TextGame) : Screen {
    private val mainTable:Table = Table()
    private val buttonTable: Table = Table()
    private val titleTable:Table = Table()

    override fun show() {
        TextGame.backgroundColor = Color(0f,0f,0f,1f)

        var labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val style: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        style.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        style.fontColor = Color.WHITE

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
                ChainTask.addTaskToEveryFrameList(crazyFade())
                GroupManager.init()
                SupplyManager.init()
            }
        })

        continueButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                ChainTask.addTaskToEveryFrameList(continueGameFade())
                GroupManager.init()
                SupplyManager.init()
            }
        })

        titleTable.add(titleLabel).pad(20f)

        buttonTable.add(continueButton).width(150f).height(50f)
        buttonTable.row().padTop(20f)
        buttonTable.add(startButton).width(150f).height(50f).padBottom(50f)

        buttonTable.bottom()

        titleTable.top()

        mainTable.add(titleTable).fill().expand()
        mainTable.row()
        mainTable.add(buttonTable).fill().expand()
        mainTable.setFillParent(true)

        TextGame.stage.addActor(mainTable)
    }

    fun continueGameFade():ChainTask{
        val chainTask = ChainTask({ mainTable.color.a > 0 }, {
            val value = GH.lerpValue(mainTable.color.a, 1f, 0f, 1f)
            mainTable.color.a = value
        },{
            TextGame.stage.clear();

            //Load the game!
            game.screen = GameScreen(game)
            SaveLoad.loadGame()

            TextGame.backgroundColor.a = 0f
            TextGame.batch.color = Color(0f,0f,0f,0f)
            val blackPixel = TextGame.smallGuiAtlas.findRegion("pixelBlack")
            val task = ChainTask({TextGame.backgroundColor.r < 1},
                    {
                        TextGame.batch.begin()
                        var amt = TextGame.backgroundColor.r + 0.01f
                        TextGame.backgroundColor.r = amt; TextGame.backgroundColor.g=amt; TextGame.backgroundColor.b=amt; TextGame.backgroundColor.b=amt; TextGame.backgroundColor.a=amt
                        TextGame.batch.color = Color(0f, 0f, 0f, (1-amt))
                        TextGame.batch.draw(blackPixel, -TextGame.viewport.screenWidth/2f, -TextGame.viewport.screenHeight/2f, TextGame.viewport.screenWidth.toFloat(), TextGame.viewport.screenHeight.toFloat())
                        TextGame.batch.end()
                    })
            ChainTask.addTaskToEveryFrameList(task)
        })

        return chainTask
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

    fun crazyFade():ChainTask{
        val tsk = ChainTask({TextGame.backgroundColor.r < 1f}, {TextGame.backgroundColor.r+=0.05f; TextGame.backgroundColor.g+=0.05f; TextGame.backgroundColor.b+=0.05f}, {game.screen = GameIntroScreen(game)})

        return tsk
    }
}