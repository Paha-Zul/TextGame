package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.SaveLoad
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.gui.actions.CallbackAction
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.managers.GameStats
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.screens.MainMenuScreen
import com.quickbite.rx2020.util.GH

/**
 * Created by Paha on 5/3/2016.
 */
class GameOverGUI(val game:TextGame) {
    var page = 1
    var mainTable: Table = Table()
    lateinit var nextButton: ImageButton
    //25 - 75 random weeks

    fun gameOver(){
        TextGame.stage.clear()

        nextButton = ImageButton(TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("nextButton")))

        nextButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                page++
                when(page){
                    2 -> page2()
                    3 -> page3()
                    else -> backToMainMenu()
                }
            }
        })

        mainTable.setFillParent(true)
        TextGame.stage.addActor(mainTable)

        page1()
    }

    fun page1(){
        //Figure out some time stuff.
        val hours = GameStats.TimeInfo.totalTimeCounter.toInt()
        val totalMonths =( hours/(24*30)).toInt()
        val totalDays = ((hours/24) - totalMonths*(24*30)).toInt()
        val totalHours =  (hours - (totalDays*24) - totalMonths*(24*30)).toInt()

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        //Replace lots of stuff.
        var titleDesc = DataManager.end.desc[0]
        titleDesc = titleDesc.replace("%o", 10.toString()).replace("%a", GroupManager.numPeopleAlive.toString()).replace("%r", GameStats.TravelInfo.totalDistOfGame.toString()).
                replace("%m", totalMonths.toString()).replace("%d", totalDays.toString()).replace("%h", totalHours.toString())

        val titleLabel = Label(titleDesc, labelStyle)
        titleLabel.setFontScale(0.2f)
        titleLabel.color.a = 0f
        titleLabel.setWrap(true)
        titleLabel.setAlignment(Align.center)

        mainTable.add(titleLabel).fill().expand().pad(0f, 30f, 0f, 30f)
        mainTable.row()
        mainTable.add(nextButton).size(64f)

        nextButton.color.a = 0f

        titleLabel.addAction(Actions.fadeIn(0.5f))
        nextButton.addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeIn(0.5f)))
    }

    fun page2(){
        mainTable.clear()
        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        val bodyLabel = Label(DataManager.end.desc[1], labelStyle)
        bodyLabel.setFontScale(0.2f)
        bodyLabel.color.a = 0f
        bodyLabel.setWrap(true)
        bodyLabel.setAlignment(Align.center)

        mainTable.add(bodyLabel).fill().expand().pad(0f, 30f, 0f, 30f)
        mainTable.row()
        mainTable.add(nextButton).size(64f)

        nextButton.color.a = 0f

        bodyLabel.addAction(Actions.fadeIn(0.5f))
        nextButton.addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeIn(0.5f)))
    }

    fun page3(){
        mainTable.clear()
        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        var finalDesc = DataManager.end.desc[2]
        finalDesc = finalDesc.replace("%w", MathUtils.random(25, 75).toString())

        val finalLabel = Label(finalDesc, labelStyle)
        finalLabel.setFontScale(0.2f)
        finalLabel.color.a = 0f
        finalLabel.setWrap(true)
        finalLabel.setAlignment(Align.center)

        mainTable.add(finalLabel).fill().expand().pad(0f, 30f, 0f, 30f)
        mainTable.row()
        mainTable.add(nextButton).size(64f)

        nextButton.color.a = 0f

        finalLabel.addAction(Actions.fadeIn(0.5f))
        nextButton.addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeIn(0.5f)))
    }

    fun backToMainMenu(){
        SaveLoad.deleteSave()
        mainTable.addAction(Actions.sequence(Actions.fadeOut(1f), CallbackAction({game.screen = MainMenuScreen(game)})))
        ChainTask.addTaskToEveryFrameList(ChainTask({TextGame.backgroundColor.r > 0},{
            val amt = GH.lerpValue(TextGame.backgroundColor.r, 1f, 0f, 1f)
            TextGame.backgroundColor.r = amt
            TextGame.backgroundColor.g = amt
            TextGame.backgroundColor.b = amt
        }))
    }

}