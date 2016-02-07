package com.quickbite.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

/**
 * Created by Paha on 2/5/2016.
 */
class GameScreenGUI(val game : GameScreen) {
    val table: Table = Table()

    lateinit var timeTitleLabel:Label
    lateinit var timeLabel:Label
    lateinit var distanceTitleLabel:Label
    lateinit var distanceLabel:Label

    lateinit var distProgressBar:ProgressBar


    fun init(){
        val style:Label.LabelStyle = Label.LabelStyle(TextGame.font, Color.RED)
        val barStyle:ProgressBar.ProgressBarStyle = ProgressBar.ProgressBarStyle()
        barStyle.background = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("art/bar.png"))))
        barStyle.knobBefore = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("art/pixel.png"))))

        val timerTable:Table = Table()
        val distanceTable:Table = Table()

        distProgressBar = ProgressBar(0f, game.totalDistOfGame.toFloat(), 20f, false, barStyle)

        timeTitleLabel = Label("Time of Day", style)
        timeLabel = Label("0000"+game.currTime, style)

        distanceTitleLabel = Label("Distance", style)
        distanceLabel = Label("0 / "+game.totalDistOfGame, style)

        timeTitleLabel.setFontScale(0.8f)
        timeLabel.setFontScale(0.5f)

        distanceTitleLabel.setFontScale(0.8f)
        distanceLabel.setFontScale(0.5f)

        timerTable.add(timeTitleLabel)
        timerTable.row()
        timerTable.add(timeLabel)


        distanceTable.add(distanceTitleLabel)
        distanceTable.row()
        distanceTable.add(distanceLabel)
        //distanceTable.row()
        //distanceTable.add(distProgressBar).height(25f).width(150f)

        table.add(timerTable)
        table.add().padLeft(50f)
        table.add(distanceTable)
        table.setFillParent(true)
        table.top()

        TextGame.stage.addActor(table)
    }

    fun update(delta:Float){

    }

    fun updateOnTimeTick(delta:Float){
        var time:String = ""

        when{
            game.currTime < 10 -> time = "0"+game.currTime.toInt()+"00"
            else -> time = ""+game.currTime.toInt()+"00"
        }

        timeLabel.setText(time)

        distanceLabel.setText("" + game.totalDistTraveled + " / " + game.totalDistOfGame)
        distProgressBar.setValue(game.totalDistTraveled.toFloat())
    }

    fun triggerEventGUI(){

    }
}