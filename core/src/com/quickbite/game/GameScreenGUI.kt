package com.quickbite.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align

/**
 * Created by Paha on 2/5/2016.
 */
class GameScreenGUI(val game : GameScreen) {
    var displaying:Int = 0

    private val table: Table = Table()
    private val travelInfoTable:Table = Table()
    private val tabTable:Table = Table()

    private val timeInfoTable:Table = Table()
    private lateinit var timeTitleLabel:Label
    private lateinit var timeLabel:Label

    /* GUI elements for travel info */
    private val distanceTable:Table = Table()
    private lateinit var distanceTitleLabel:Label
    private lateinit var distanceLabel:Label
    private lateinit var totalDays:Label

    /* GUI elements for people */
    private val groupStatsTable:Table = Table() //Holds the group and supply tables
    private val groupTable:Table = Table() //For the group
    private val supplyTable:Table = Table() //For the supplies

    /* Gui elements for events */
    private val eventTable:Table = Table()
    private val eventChoicesTable:Table = Table()
    private val outerTable:Table = Table()
    private val supplyAmountList:MutableList<Label> = arrayListOf()

    private lateinit var supplyButtonTab:TextButton
    private lateinit var travelInfoButtonTab:TextButton
    private lateinit var groupButtonTab:TextButton

    /* GUI elements for supplies */

    private lateinit var pauseButton:ImageButton

    private val defLabelStyle = Label.LabelStyle(TextGame.font, Color.BLACK)

    private lateinit var distProgressBar:ProgressBar

    fun init(){
        buildTravelScreenGUI()
        applyTravelTab(groupTable)
        groupButtonTab.isChecked = true
        //applyTravelScreenGUI()
    }

    fun update(delta:Float){

    }

    fun updateOnTimeTick(delta:Float){
        var time:String

        when{
            GameStats.TimeInfo.currTime < 10 -> time = "0"+GameStats.TimeInfo.currTime.toInt()+"00"
            else -> time = ""+GameStats.TimeInfo.currTime.toInt()+"00"
        }

        timeLabel.setText(time)
        totalDays.setText(""+(GameStats.TimeInfo.totalTimeCounter/GameStats.TimeInfo.timeScale).toInt()+" Days")

        distanceLabel.setText("" + game.totalDistTraveled + " / " + game.totalDistOfGame)
        distProgressBar.setValue(game.totalDistTraveled.toFloat())

        updateSuppliesGUI()
    }

    private fun updateSuppliesGUI(){
        val list = GameStats.supplyManager.getSupplyList()
        for(i in list.indices){
            supplyAmountList[i].setText( list[i].amt.toInt().toString())
        }
    }

    fun addListeners(){
        pauseButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                when{
                    pauseButton.isChecked -> game.pauseGame()
                    else -> game.resumeGame()
                }
            }
        })

        supplyButtonTab.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                applyTravelTab(supplyTable)
                supplyButtonTab.isChecked = true
                travelInfoButtonTab.isChecked = false
                groupButtonTab.isChecked = false
            }
        })

        travelInfoButtonTab.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                applyTravelTab(travelInfoTable)
                travelInfoButtonTab.isChecked = true
                supplyButtonTab.isChecked = false
                groupButtonTab.isChecked = false
            }
        })

        groupButtonTab.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                applyTravelTab(groupTable)
                groupButtonTab.isChecked = true
                travelInfoButtonTab.isChecked = false
                supplyButtonTab.isChecked = false
            }
        })
    }

    fun applyTravelTab(tableToApply:Table){
        table.remove()
        table.clear()

        table.add(tabTable).top()
        table.add(tableToApply).left().top()

        table.top().left()
        table.setFillParent(true)

        TextGame.stage.addActor(table)
    }

    /**
     * Applies the travel screen GUI stuff, which is initially only the group stats
     * and supplies info.
     */
    fun applyTravelScreenGUI(){
        table.remove()
        table.clear()

        table.add(tabTable).top()
        table.add(groupStatsTable).left().top().padRight(30f)

        table.top().left()
        table.setFillParent(true)

        TextGame.stage.addActor(table)
    }

    fun buildTravelScreenGUI(){
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

        distProgressBar = ProgressBar(0f, game.totalDistOfGame.toFloat(), 20f, false, barStyle)

        pauseButton = ImageButton(pauseButtonStyle)
        pauseButton.setSize(40f, 40f)
        pauseButton.setPosition(Gdx.graphics.width.toFloat() - pauseButton.width, Gdx.graphics.height.toFloat() - pauseButton.height)

        //distanceTable.row()
        //distanceTable.add(distProgressBar).height(25f).width(150f)

        TextGame.stage.addActor(pauseButton)

        buildTabTable()
        buildTravelInfoTable()
        buildStatsTable()

        addListeners()
    }

    private fun buildTabTable(){
        val textButtonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.checkedFontColor = Color.GREEN
        textButtonStyle.fontColor = Color.WHITE

        travelInfoButtonTab = TextButton("Stats", textButtonStyle)
        travelInfoButtonTab.label.setFontScale(0.5f)
        travelInfoButtonTab.label.setAlignment(Align.left)

        supplyButtonTab = TextButton("Supplies", textButtonStyle)
        supplyButtonTab.label.setFontScale(0.5f)
        supplyButtonTab.label.setAlignment(Align.left)

        groupButtonTab = TextButton("Group", textButtonStyle)
        groupButtonTab.label.setFontScale(0.5f)
        groupButtonTab.label.setAlignment(Align.left)

        tabTable.add(travelInfoButtonTab).width(100f).height(25f).padRight(10f)
        tabTable.row()
        tabTable.add(supplyButtonTab).width(100f).height(25f).padRight(10f)
        tabTable.row()
        tabTable.add(groupButtonTab).width(100f).height(25f).padRight(10f)
    }

    private fun buildTravelInfoTable(){
        travelInfoTable.top().left()
        travelInfoTable.add(timeInfoTable).top().padRight(30f)
        travelInfoTable.add(distanceTable).top()

        buildTimeInfo()
        buildTravelInfo()
    }

    private fun buildTimeInfo(){
        val style:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        /* Time related stuff */
        timeTitleLabel = Label("Time of Day", style)
        timeLabel = Label("0000"+GameStats.TimeInfo.currTime, style)

        timeTitleLabel.setFontScale(0.5f)
        timeLabel.setFontScale(0.3f)

        timeInfoTable.add(timeTitleLabel)
        timeInfoTable.row()
        timeInfoTable.add(timeLabel)
    }

    private fun buildTravelInfo(){
        val style:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        /* Travel info related stuff */
        distanceTitleLabel = Label("Travel Info", style)
        distanceLabel = Label("0 / "+game.totalDistOfGame, style)
        totalDays = Label("0"+GameStats.TimeInfo.currTime+" Days", style)

        distanceTitleLabel.setFontScale(0.5f)
        distanceLabel.setFontScale(0.3f)
        totalDays.setFontScale(0.3f)

        distanceTable.add(distanceTitleLabel)
        distanceTable.row()
        distanceTable.add(distanceLabel)
        distanceTable.row()
        distanceTable.add(totalDays)
    }

    /**
     * Builds the left side table layout which includes the group and supply table.
     */
    fun buildStatsTable(){
        groupStatsTable.top().left()
        groupStatsTable.add(groupTable).top().padRight(30f)
        groupStatsTable.add(supplyTable).top()

        buildGroupTable()
        buildSupplyTable()
    }

    /**
     * Builds the group table layout.
     */
    fun buildGroupTable(){
        groupTable.clear()

        val innerGroupTable = Table()

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val titleLabel:Label = Label("Group", labelStyle)
        titleLabel.setFontScale(0.5f)
        //groupTable.add(titleLabel)
        //groupTable.row()

        val list:Array<Person> = GameStats.groupManager.getPeopleList()
        for(person:Person in list.iterator()){
            val nameLabel = Label(person.name, labelStyle)
            nameLabel.setFontScale(0.3f)

            val healthLabel:Label = Label(""+person.health, labelStyle)
            healthLabel.setFontScale(0.3f)

            innerGroupTable.add(nameLabel).left().padRight(10f)
            innerGroupTable.add(healthLabel).left()
            innerGroupTable.row()
        }

        groupTable.add(innerGroupTable)
    }

    /**
     * Builds the supply table layout
     */
    fun buildSupplyTable(){
        supplyTable.clear()
        supplyAmountList.clear()

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val title = Label("Supplies", labelStyle)
        title.setFontScale(0.5f)

        //supplyTable.add(title).left()
        //supplyTable.row()

        val innerTable = Table()
        val list = GameStats.supplyManager.getSupplyList()
        for(i in list.indices){
            val value = list[i]
            val nameLabel = Label(value.displayName, labelStyle)
            nameLabel.setFontScale(0.3f)

            val amtLabel = Label(""+value.amt.toInt(), labelStyle)
            amtLabel.setFontScale(0.3f)
            supplyAmountList += amtLabel

            innerTable.add(nameLabel).left().padRight(20f)
            innerTable.add(amtLabel).left().width(40f)

            if((i)%2 == 1)
                innerTable.row()

        }

        supplyTable.add(innerTable)
    }


    fun triggerEventGUI(event: DataManager.EventJson, callbackTask : (choice:String)->Unit){
        game.pauseGame()

        outerTable.clear()
        eventTable.clear()
        eventChoicesTable.clear()

        outerTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("log", Texture::class.java)))

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
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
                    game.resumeGame()
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