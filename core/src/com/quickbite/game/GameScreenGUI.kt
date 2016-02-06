package com.quickbite.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table

/**
 * Created by Paha on 2/5/2016.
 */
class GameScreenGUI(val game : GameScreen) {
    val table: Table = Table()

    lateinit var timeTitleLabel:Label
    lateinit var timeLabel:Label

    fun init(){
        val style:Label.LabelStyle = Label.LabelStyle(TextGame.font, Color.RED)
        val timerTable:Table = Table()

        timeTitleLabel = Label("Time of Day", style)
        timeLabel = Label(""+game.currTime, style)

        timeTitleLabel.setFontScale(0.8f)
        timeLabel.setFontScale(0.5f)

        timerTable.add(timeTitleLabel)
        timerTable.row()
        timerTable.add(timeLabel)

        table.add(timerTable)
        table.setFillParent(true)
        table.top()

        TextGame.stage.addActor(table)
    }

    fun update(delta:Float){
        var time:String = ""

        when{
            game.currTime < 10 -> time = "0"+game.currTime.toInt()+"00"
            else -> time = ""+game.currTime.toInt()+"00"
        }

        timeLabel.setText(time)
    }
}