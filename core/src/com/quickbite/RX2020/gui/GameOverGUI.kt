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
import com.quickbite.rx2020.util.FunGameStats
import com.quickbite.rx2020.util.GH

/**
 * Created by Paha on 5/3/2016.
 */
class GameOverGUI(val game:TextGame) {
    var mainTable: Table = Table()
    lateinit var nextButton: ImageButton
    //25 - 75 random weeks

    var rowCounter = 0
    var toMenu = false

    //Figure out some time stuff.
    val time = GH.formatTime()
    val totalMonths = time.first
    val totalDays = time.second
    val totalHours = time.third

    fun gameOver(){
        TextGame.stage.clear()

        var text:Array<String> = arrayOf()

        when(GameStats.gameOverStatus){
            "won" -> text = DataManager.end.win
            "LostPanel" -> text = DataManager.end.solar
            "LostTrack" -> text = DataManager.end.tracks
            "LostBattery" -> text = DataManager.end.batteries
            "LostStorage" -> text = DataManager.end.storage
            "LostEnergy" -> text = DataManager.end.energy
            "LostROV" -> text = DataManager.end.ROV
            "LostLife" -> text = DataManager.end.crew
            else -> text = arrayOf("What did I lose to? The reason was ${GameStats.gameOverStatus}")
        }

        nextButton = ImageButton(TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("nextButton")))

        var page = 0
        nextButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                page++
                if(displayTextPage(text, page)) {
                    if(!toMenu) {
                        if(GameStats.gameOverStatus == "won" || GameStats.gameOverStatus == "LostLife")
                            displayFunStats()
                        else
                            displayTextPage(DataManager.end.deathText, 0)
                        toMenu = true
                    }else
                        backToMainMenu()
                }
            }
        })

        mainTable.setFillParent(true)
        TextGame.stage.addActor(mainTable)

        displayTextPage(text, page)
    }

    fun displayTextPage(text:Array<String>, page:Int):Boolean{
        if(page >= text.size)
            return true

        mainTable.clear()

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        //Replace lots of stuff.
        var modifiedDesc = text[page]
        modifiedDesc = modifiedDesc.replace("%o", GroupManager.initialGroupSize.toString()).replace("%a", GroupManager.numPeopleAlive.toString()).replace("%r", GameStats.TravelInfo.totalDistOfGame.toString()).
                replace("%m", totalMonths.toString()).replace("%d", totalDays.toString()).replace("%h", totalHours.toString()).replace("%i", GameStats.gameOverStatus).replace("%w", MathUtils.random(25, 75).toString())
        .replace("%e", GameStats.TravelInfo.totalDistToGo.toString()).replace("%t", GameStats.TravelInfo.totalDistTraveled.toString()).replace("%f", GH.formatTimeText(GH.formatTime()))

        //Make the description label
        val descLabel = Label(modifiedDesc, labelStyle)
        descLabel.setFontScale(0.2f)
        descLabel.color.a = 0f
        descLabel.setWrap(true)
        descLabel.setAlignment(Align.center)

        //Add the label and the button.y
        mainTable.add(descLabel).fill().expand().pad(0f, 30f, 0f, 30f)
        mainTable.row()
        mainTable.add(nextButton).size(64f)

        nextButton.color.a = 0f

        descLabel.addAction(Actions.fadeIn(0.5f))
        nextButton.addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeIn(0.5f)))

        return false
    }

    fun displayFunStats(){
        mainTable.clear()
        var counter = 0

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        val descLabel = Label("Fun Stats", labelStyle)
        descLabel.setFontScale(0.4f)
        descLabel.color.a = 0f

        descLabel.addAction(Actions.sequence(Actions.delay(0.2f*counter), Actions.fadeIn(0.4f)))
        counter++

        mainTable.add(descLabel)
        mainTable.row()

        val uniqueStats = FunGameStats.uniqueStatsList
        val otherStats = FunGameStats.statsMap.toList()
        val innerTable = Table()
        var statTable = Table()

        val perSide:Int = (uniqueStats.size + otherStats.size)/2

        otherStats.forEach { stat ->
            val label = Label("${stat.first}: ", labelStyle)
            label.setFontScale(0.15f)
            label.color.a = 0f
            label.setAlignment(Align.left)

            val valueLabel = Label("${stat.second}", labelStyle)
            valueLabel.setFontScale(0.15f)
            valueLabel.color.a = 0f

            statTable = addToTable(statTable, innerTable, perSide, label, valueLabel)
            label.addAction(Actions.sequence(Actions.delay(0.2f*counter), Actions.fadeIn(0.4f)))
            valueLabel.addAction(Actions.sequence(Actions.delay(0.2f*counter), Actions.fadeIn(0.4f)))

            rowCounter++
            counter++
        }

        uniqueStats.forEach { stat ->
            val label = Label("${stat.desc}: ", labelStyle)
            label.setFontScale(0.15f)
            label.color.a = 0f

            val valueLabel = Label("${stat.value}", labelStyle)
            valueLabel.setFontScale(0.15f)
            valueLabel.color.a = 0f

            statTable = addToTable(statTable, innerTable, perSide, label, valueLabel)
            label.addAction(Actions.sequence(Actions.delay(0.2f*counter), Actions.fadeIn(0.4f)))
            valueLabel.addAction(Actions.sequence(Actions.delay(0.2f*counter), Actions.fadeIn(0.4f)))

            rowCounter++
            counter++
        }

        innerTable.add(statTable) //Add the final table.

        mainTable.add(innerTable).fill().expand()
        mainTable.row()
        mainTable.add(nextButton).size(64f)

        nextButton.color.a = 0f

        descLabel.addAction(Actions.fadeIn(0.5f))
        nextButton.addAction(Actions.sequence(Actions.delay(0.5f), Actions.fadeIn(0.5f)))
    }

    fun addToTable(statTable:Table, innerTable:Table, perSide:Int, label1:Label, label2:Label):Table{
        var statTable = statTable
        if(rowCounter >= perSide) {
            innerTable.add(statTable).space(0f, 50f, 0f, 50f)
            statTable = Table()
            rowCounter = 0
        }

        statTable.add(label1).fillX().expandX()
        statTable.add(label2).fillX().expandX()
        statTable.row()

        return statTable
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