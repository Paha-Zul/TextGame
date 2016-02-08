package com.quickbite.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align

/**
 * Created by Paha on 2/5/2016.
 */
class GameScreenGUI(val game : GameScreen) {
    private val table: Table = Table()

    private lateinit var timeTitleLabel:Label
    private lateinit var timeLabel:Label
    private lateinit var distanceTitleLabel:Label
    private lateinit var distanceLabel:Label

    /* Gui elements for events */
    private val eventTable:Table = Table()
    private val eventChoicesTable:Table = Table()
    private val outerTable:Table = Table()

    private lateinit var pauseButton:ImageButton

    private val defLabelStyle = Label.LabelStyle(TextGame.font, Color.BLACK)

    private lateinit var distProgressBar:ProgressBar

    fun init(){
        val style:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val barStyle:ProgressBar.ProgressBarStyle = ProgressBar.ProgressBarStyle()
        barStyle.background = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("art/bar.png"))))
        barStyle.knobBefore = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("art/pixel.png"))))

        val pauseButtonStyle = ImageButton.ImageButtonStyle()
        var drawable = TextureRegionDrawable(TextureRegion(TextGame.manager.get("play", Texture::class.java)))
        pauseButtonStyle.imageChecked = drawable
        pauseButtonStyle.imageCheckedOver = drawable

        drawable = TextureRegionDrawable(TextureRegion(TextGame.manager.get("pause", Texture::class.java)))
        pauseButtonStyle.imageUp =  drawable
        pauseButtonStyle.imageOver =  drawable
        pauseButtonStyle.imageDown =  drawable

        val timerTable:Table = Table()
        val distanceTable:Table = Table()

        distProgressBar = ProgressBar(0f, game.totalDistOfGame.toFloat(), 20f, false, barStyle)

        timeTitleLabel = Label("Time of Day", style)
        timeLabel = Label("0000"+game.currTime, style)

        distanceTitleLabel = Label("Distance", style)
        distanceLabel = Label("0 / "+game.totalDistOfGame, style)

        timeTitleLabel.setFontScale(0.7f)
        timeLabel.setFontScale(0.4f)

        distanceTitleLabel.setFontScale(0.7f)
        distanceLabel.setFontScale(0.4f)

        pauseButton = ImageButton(pauseButtonStyle)
        pauseButton.setSize(40f, 40f)
        pauseButton.setPosition(Gdx.graphics.width.toFloat() - pauseButton.width, Gdx.graphics.height.toFloat() - pauseButton.height)

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
        TextGame.stage.addActor(pauseButton)

        addListeners()
    }

    fun addListeners(){
        pauseButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                when{
                    pauseButton.isChecked -> game.pause()
                    else -> game.resume()
                }
            }
        })
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

    fun triggerEventGUI(event: DataManager.EventJson, callbackTask : (choice:String)->Unit){
        outerTable.clear()
        eventTable.clear()
        eventChoicesTable.clear()
        game.game.pause()

        outerTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("log", Texture::class.java)))

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        val textButtonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.BLACK

        val padding:Int = 400/(event.choices!!.size+1)/2

        for(choice in event.choices?.iterator()){
            val button = TextButton(choice, textButtonStyle)
            button.pad(0f, 10f, 0f, 10f)
            button.label.setFontScale(0.5f)
            eventChoicesTable.add(button)

            button.addListener(object:ChangeListener(){
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    game.game.resume()
                    outerTable.remove()
                    callbackTask(button.text.toString())
                }
            })
        }

        eventChoicesTable.bottom()

        val titleLabel = Label(event.name, labelStyle)
        titleLabel.setAlignment(Align.left)
        titleLabel.setFontScale(0.5f)

        val descLabel = Label(event.description, labelStyle)
        descLabel.setAlignment(Align.top)
        descLabel.setFontScale(0.3f)
        descLabel.setWrap(true)

        eventTable.add(titleLabel).expandX().fillX().padLeft(40f).height(45f)
        eventTable.row().expand().fill()
        eventTable.add(descLabel).width(310f).padTop(10f)
        eventTable.row().expand().fill()
        eventTable.add(eventChoicesTable).expandX().fillX().bottom().padBottom(25f)

        //eventTable.debugAll()

        outerTable.setSize(400f, 400f)
        outerTable.setPosition(Gdx.graphics.width/2f - 200, Gdx.graphics.height/2f - 200)
        outerTable.add(eventTable).expand().fill()

        //outerTable.debugAll()

        TextGame.stage.addActor(outerTable)
    }
}