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
        val style:Label.LabelStyle = Label.LabelStyle(TextGame.font, Color.WHITE)
        val timerTable:Table = Table()

        timeTitleLabel = Label("Time of Day", style)
        timeLabel = Label(""+game.currTime, style)

        timerTable.add(timeTitleLabel)
        timerTable.row()
        timerTable.add(timeLabel)

        table.add(timerTable)

        TextGame.stage.addActor(table)
    }

    fun update(delta:Float){

    }
}