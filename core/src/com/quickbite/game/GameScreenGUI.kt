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
import com.quickbite.game.managers.DataManager
import com.quickbite.game.managers.GroupManager
import com.quickbite.game.managers.SupplyManager
import com.quickbite.game.screens.GameScreen

/**
 * Created by Paha on 2/5/2016.
 */
class GameScreenGUI(val game : GameScreen) {
    var displaying:Int = 0

    private val normalFontScale = 0.15f
    private val titleFontScale = 0.25f
    private val buttonFontScale = 0.15f

    private val table: Table = Table()
    private val travelInfoTable:Table = Table()
    private val tabTable:Table = Table()
    private val campTable:Table = Table()

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

    /* Tab buttons */
    private lateinit var supplyButtonTab:TextButton
    private lateinit var travelInfoButtonTab:TextButton
    private lateinit var groupButtonTab:TextButton
    private lateinit var campButtonTab:TextButton

    /* Camp specific stuff */
    private lateinit var restButton:TextButton
    private lateinit var scavengeButton:TextButton
    private lateinit var restHourLabel:Label
    private lateinit var scavengeHourLabel:Label
    private lateinit var restHourSlider:Slider
    private lateinit var scavengeHourSlider:Slider

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

        var t:Int = ((GameStats.TimeInfo.currTime.toInt())%12)

        if(t == 0) t = 12
        time = ""+t+":00 "
        if(GameStats.TimeInfo.currTime >= 12) time += "PM"
        else time += "AM"

        timeLabel.setText(time)
        totalDays.setText(""+(GameStats.TimeInfo.totalTimeCounter/GameStats.TimeInfo.timeScale).toInt()+" Days")

        distanceLabel.setText("" + GameStats.TravelInfo.totalDistTraveled + " / " + GameStats.TravelInfo.totalDistOfGame)
        distProgressBar.setValue(GameStats.TravelInfo.totalDistTraveled.toFloat())

        updateSuppliesGUI()
    }

    private fun updateSuppliesGUI(){
        val list = SupplyManager.getSupplyList()
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
                campButtonTab.isChecked = false
            }
        })

        travelInfoButtonTab.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                applyTravelTab(travelInfoTable)
                travelInfoButtonTab.isChecked = true
                supplyButtonTab.isChecked = false
                groupButtonTab.isChecked = false
                campButtonTab.isChecked = false
            }
        })

        groupButtonTab.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                applyTravelTab(groupTable)
                groupButtonTab.isChecked = true
                travelInfoButtonTab.isChecked = false
                supplyButtonTab.isChecked = false
                campButtonTab.isChecked = false
            }
        })

        campButtonTab.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                campButtonTab.isChecked = false

                if(game.state == GameScreen.State.TRAVELING) {
                    game.state = GameScreen.State.CAMP
                    campButtonTab.setText("Travel")
                    applyCampTab()
                }else if(game.state == GameScreen.State.CAMP) {
                    game.state = GameScreen.State.TRAVELING
                    campButtonTab.setText("Camp")
                }
            }
        })

        restButton.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)

                game.numHoursToAdvance = restHourSlider.value.toInt()
            }
        })

        scavengeButton.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)

                game.numHoursToAdvance = scavengeHourSlider.value.toInt()
            }
        })

        restHourSlider.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                restHourLabel.setText(restHourSlider.value.toInt().toString() + " hours")
            }
        })

        scavengeHourSlider.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                scavengeHourLabel.setText(scavengeHourSlider.value.toInt().toString() + " hours")
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
    fun applyCampTab(){
        TextGame.stage.clear()
        table.clear()

        table.add(tabTable).top()
        table.add(groupStatsTable).left().top().padRight(30f)

        table.top().left()
        table.setFillParent(true)
        campTable.bottom().left()
        campTable.setFillParent(true)

        TextGame.stage.addActor(table)
        TextGame.stage.addActor(campTable)
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

        distProgressBar = ProgressBar(0f, GameStats.TravelInfo.totalDistOfGame.toFloat(), 20f, false, barStyle)

        pauseButton = ImageButton(pauseButtonStyle)
        pauseButton.setSize(40f, 40f)
        pauseButton.setPosition(Gdx.graphics.width.toFloat() - pauseButton.width, Gdx.graphics.height.toFloat() - pauseButton.height)

        //distanceTable.row()
        //distanceTable.add(distProgressBar).height(25f).width(150f)

        TextGame.stage.addActor(pauseButton)

        buildCampTable()
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
        travelInfoButtonTab.label.setFontScale(buttonFontScale)
        travelInfoButtonTab.label.setAlignment(Align.left)

        supplyButtonTab = TextButton("Supplies", textButtonStyle)
        supplyButtonTab.label.setFontScale(buttonFontScale)
        supplyButtonTab.label.setAlignment(Align.left)

        groupButtonTab = TextButton("Group", textButtonStyle)
        groupButtonTab.label.setFontScale(buttonFontScale)
        groupButtonTab.label.setAlignment(Align.left)

        campButtonTab = TextButton("Camp", textButtonStyle)
        campButtonTab.label.setFontScale(buttonFontScale)
        campButtonTab.label.setAlignment(Align.left)

        tabTable.add(travelInfoButtonTab).width(100f).padRight(10f)
        tabTable.row()
        tabTable.add(supplyButtonTab).width(100f).padRight(10f)
        tabTable.row()
        tabTable.add(groupButtonTab).width(100f).padRight(10f)
        tabTable.row()
        tabTable.add(campButtonTab).width(100f).padRight(10f)
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

        timeTitleLabel.setFontScale(titleFontScale)
        timeLabel.setFontScale(normalFontScale)

        timeInfoTable.add(timeTitleLabel)
        timeInfoTable.row()
        timeInfoTable.add(timeLabel)
    }

    private fun buildTravelInfo(){
        val style:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        /* Travel info related stuff */
        distanceTitleLabel = Label("Travel Info", style)
        distanceLabel = Label("0 / "+GameStats.TravelInfo.totalDistOfGame, style)
        totalDays = Label("0"+GameStats.TimeInfo.currTime+" Days", style)

        distanceTitleLabel.setFontScale(titleFontScale)
        distanceLabel.setFontScale(normalFontScale)
        totalDays.setFontScale(normalFontScale)

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
        titleLabel.setFontScale(titleFontScale)
        //groupTable.add(titleLabel)
        //groupTable.row()

        val list:Array<Person> = GroupManager.getPeopleList()
        for(person:Person in list.iterator()){
            val nameLabel = Label(person.name, labelStyle)
            nameLabel.setFontScale(normalFontScale)

            val healthLabel:Label = Label(""+person.health, labelStyle)
            healthLabel.setFontScale(normalFontScale)

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
        title.setFontScale(titleFontScale)

        //supplyTable.add(title).left()
        //supplyTable.row()

        val innerTable = Table()
        val list = SupplyManager.getSupplyList()
        for(i in list.indices){
            val value = list[i]
            val nameLabel = Label(value.displayName, labelStyle)
            nameLabel.setFontScale(normalFontScale)

            val amtLabel = Label(""+value.amt.toInt(), labelStyle)
            amtLabel.setFontScale(normalFontScale)
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
            button.label.setFontScale(buttonFontScale)
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
        titleLabel.setFontScale(titleFontScale)

        val desc = event.description.replace("%n", event.randomName)

        val descLabel = Label(desc, labelStyle)
        descLabel.setAlignment(Align.top)
        descLabel.setFontScale(normalFontScale)
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

    fun buildCampTable(){
        campTable.clear()
        campTable.remove()
        campTable.setFillParent(true)

        val slider = TextureRegionDrawable(TextureRegion(TextGame.manager.get("slider", Texture::class.java)))
        val knob = TextureRegionDrawable(TextureRegion(TextGame.manager.get("sliderKnob", Texture::class.java)))

        val sliderStyle:Slider.SliderStyle = Slider.SliderStyle(slider, knob)
        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val buttonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)

        restButton = TextButton("Rest", buttonStyle)
        restButton.label.setFontScale(0.2f)

        scavengeButton = TextButton("Scavenge", buttonStyle)
        scavengeButton.label.setFontScale(0.2f)

        restHourLabel = Label("0 hours", labelStyle)
        restHourLabel.setFontScale(normalFontScale)

        scavengeHourLabel = Label("0 hours", labelStyle)
        scavengeHourLabel.setFontScale(normalFontScale)

        restHourSlider = Slider(0f, 24f, 1f, false, sliderStyle)
        scavengeHourSlider = Slider(0f, 24f, 1f, false, sliderStyle)

        campTable.add(restButton).width(130f).left()
        campTable.add(restHourSlider).width(140f).left().padLeft(20f)
        campTable.add(restHourLabel).padLeft(20f)
        campTable.row()
        campTable.add(scavengeButton).width(130f).left()
        campTable.add(scavengeHourSlider).width(140f).left().padLeft(20f)
        campTable.add(scavengeHourLabel).padLeft(20f)

        campTable.bottom().left()
    }

    fun applyCampTable(){
        TextGame.stage.addActor(campTable)
    }
}