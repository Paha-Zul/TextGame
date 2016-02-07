package com.quickbite.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align

/**
 * Created by Paha on 2/5/2016.
 */
class GameScreenGUI(val game : GameScreen) {
    val table: Table = Table()

    lateinit var timeTitleLabel:Label
    lateinit var timeLabel:Label
    lateinit var distanceTitleLabel:Label
    lateinit var distanceLabel:Label

    /* Gui elements for events */
    val eventTable:Table = Table()
    val eventChoicesTable:Table = Table()
    val outerTable:Table = Table()

    val defLabelStyle = Label.LabelStyle(TextGame.font, Color.BLACK)

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

    fun triggerEventGUI(event: DataManager.EventJson){
        outerTable.clear()
        eventTable.clear()
        eventChoicesTable.clear()

        outerTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("pixel", Texture::class.java)))

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.font, Color.BLACK)

        val textButtonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.font
        textButtonStyle.fontColor = Color.BLACK

        val padding:Int = 400/(event.choices!!.size+1)/2

        for(choice in event.choices?.iterator()){
            val button = TextButton(choice, textButtonStyle)
            button.pad(0f, 10f, 0f, 10f)
            eventChoicesTable.add(button)

            button.addListener(object:ChangeListener(){
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    game.paused = false
                    outerTable.remove()
                }
            })
        }

        val titleLabel = Label(event.name, defLabelStyle)
        titleLabel.setAlignment(Align.top)

        val descLabel = Label(event.description, defLabelStyle)
        descLabel.setAlignment(Align.center)
        descLabel.setFontScale(0.5f)
        descLabel.setWrap(true)

        eventTable.add(titleLabel).expandX().fillX()
        eventTable.row().expand().fill()
        eventTable.add(descLabel).expand().fill()
        eventTable.row().expand().fill()
        eventTable.add(eventChoicesTable).expand().fill()

        //eventTable.debugAll()

        outerTable.setSize(400f, 400f)
        outerTable.setPosition(Gdx.graphics.width/2f - 200, Gdx.graphics.height/2f - 200)
        outerTable.add(eventTable).expand().fill()

        TextGame.stage.addActor(outerTable)
    }
}