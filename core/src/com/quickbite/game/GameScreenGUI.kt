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
    private val eventTitleFontScale = 0.18f
    private val buttonFontScale = 0.15f

    private val mainTable: Table = Table()
    private val travelInfoTable:Table = Table() //For time and distance traveled
    private val tabTable:Table = Table() //For the button tabs
    private val campTable:Table = Table() //For the camp screen
    private val centerInfoTable:Table = Table()
    private val leftTable:Table = Table()
    private val rightTable:Table = Table()

    private val timeInfoTable:Table = Table()
    private lateinit var timeTitleLabel:Label
    private lateinit var timeLabel:Label

    /* GUI elements for travel info */
    private val distanceTable:Table = Table()
    private lateinit var distanceTitleLabel:Label
    private lateinit var distanceLabel:Label
    private lateinit var totalDaysLabel:Label

    /* GUI elements for people */
    private val groupStatsTable:Table = Table() //Holds the group and supply tables
    private val groupTable:Table = Table() //For the group
    private val supplyTable:Table = Table() //For the supplies

    /* Gui elements for events */

    private val supplyAmountList:MutableList<Label> = arrayListOf()

    /* Tab buttons */
    private lateinit var supplyButton:TextButton
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

    private val defLabelStyle = Label.LabelStyle(TextGame.font, Color.WHITE)

    private lateinit var distProgressBar:ProgressBar

    fun init(){
        buildTravelScreenGUI()
        applyTravelTab(groupTable)
        //applyTravelScreenGUI()
    }

    fun update(delta:Float){

    }

    fun updateOnTimeTick(delta:Float){
        var time:String

        val t = GameStats.TimeInfo.timeOfDay
        time = ""+t+":00 "
        if(GameStats.TimeInfo.currTime >= 12) time += "PM"
        else time += "AM"

        timeLabel.setText(time)
        totalDaysLabel.setText("Day "+GameStats.TimeInfo.totalDaysTraveled)

        distanceLabel.setText("" + GameStats.TravelInfo.totalDistToGo+" Miles")
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

        supplyButton.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                if(supplyTable.parent == null) {
                    leftTable.add(supplyTable)
                }else{
                    supplyTable.remove()
                }
            }
        })

        groupButtonTab.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                if(groupTable.parent == null) {
                    buildGroupTable()
                    rightTable.add(groupTable)
                }else{
                    groupTable.remove()
                }
            }
        })

//        campButtonTab.addListener(object:ClickListener(){
//            override fun clicked(event: InputEvent?, x: Float, y: Float) {
//                super.clicked(event, x, y)
//                campButtonTab.isChecked = false
//
//                if(game.state == GameScreen.State.TRAVELING) {
//                    game.state = GameScreen.State.CAMP
//                    campButtonTab.setText("Travel")
//                    applyCampTab()
//                }else if(game.state == GameScreen.State.CAMP) {
//                    game.state = GameScreen.State.TRAVELING
//                    campButtonTab.setText("Camp")
//                    applyTravelTab(groupTable)
//                }
//            }
//        })

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

        game.timeTickEventList += ChainTask({scavengeHourSlider.value <= 0}, {scavengeHourSlider.setValue(scavengeHourSlider.value-1)})
        game.timeTickEventList += ChainTask({restHourSlider.value <= 0}, {restHourSlider.setValue(restHourSlider.value-1)})
    }

    fun applyTravelTab(tableToApply:Table){
        mainTable.remove()
        mainTable.clear()
        campTable.remove()

        //mainTable.add(tabTable).top()
        //mainTable.add(tableToApply).left().top()

        //mainTable.top().left()
        //mainTable.setFillParent(true)

        TextGame.stage.addActor(centerInfoTable)
    }

    /**
     * Applies the travel screen GUI stuff, which is initially only the group stats
     * and supplies info.
     */
    fun applyCampTab(){
        TextGame.stage.clear()
        mainTable.clear()

        mainTable.add(tabTable).top()
        mainTable.add(groupStatsTable).left().top().padRight(30f)

        mainTable.top().left()
        mainTable.setFillParent(true)
        campTable.bottom().left()
        campTable.setFillParent(true)

        TextGame.stage.addActor(mainTable)
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
        pauseButton.setPosition(Gdx.graphics.width.toFloat()/1.5f - pauseButton.width, Gdx.graphics.height.toFloat() - pauseButton.height)

        //distanceTable.row()
        //distanceTable.add(distProgressBar).height(25f).width(150f)

        TextGame.stage.addActor(pauseButton)

        buildCenterInfoTable()
        buildLeftTable()
        buildRightTable()

        buildCampTable()

        addListeners()
    }

    private fun buildCenterInfoTable(){
        centerInfoTable.top()
        centerInfoTable.setFillParent(true)

        val innerTable = Table()
        innerTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))

        val style:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        totalDaysLabel = Label("Day "+GameStats.TimeInfo.totalDaysTraveled, style)
        timeLabel = Label("12:00 AM", style)
        distanceLabel = Label(""+GameStats.TravelInfo.totalDistToGo+" Miles", style)

        /* Time related stuff */
        totalDaysLabel.setFontScale(normalFontScale)
        timeLabel.setFontScale(normalFontScale)
        distanceLabel.setFontScale(normalFontScale)

        totalDaysLabel.setAlignment(Align.center)
        timeLabel.setAlignment(Align.center)
        distanceLabel.setAlignment(Align.center)

        innerTable.add(totalDaysLabel).width(150f).fillX().expandX()
        innerTable.row()
        innerTable.add(timeLabel).width(150f).fillX().expandX()
        innerTable.row()
        innerTable.add(distanceLabel).width(150f).fillX().expandX().padBottom(20f)

        centerInfoTable.add(innerTable)
    }

    fun buildLeftTable(){
        buildSupplyTable()

        val drawable = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))

        val buttonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE
        buttonStyle.up = drawable

        supplyButton = TextButton("Storage", buttonStyle)
        supplyButton.label.setFontScale(buttonFontScale)

        leftTable.add(supplyButton).left().size(130f, 40f)
        leftTable.row()

        leftTable.top().left()
        leftTable.setFillParent(true)
        TextGame.stage.addActor(leftTable)
    }

    fun buildRightTable(){
        rightTable.clear()

        val drawable = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))

        val buttonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE
        buttonStyle.up = drawable

        groupButtonTab = TextButton("Exomer751", buttonStyle)
        groupButtonTab.label.setFontScale(buttonFontScale)

        rightTable.setFillParent(true)
        rightTable.top().right()

        rightTable.add(groupButtonTab).right().size(130f, 40f)
        rightTable.row()
        TextGame.stage.addActor(rightTable)
    }

    /**
     * Builds the group table layout.
     */
    fun buildGroupTable(){
        groupTable.clear()

        groupTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))
        groupTable.padRight(10f)

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val list:Array<Person> = GroupManager.getPeopleList()
        for(person:Person in list.iterator()){
            val nameLabel = Label(person.fullName, labelStyle)
            nameLabel.setFontScale(normalFontScale)

            val healthLabel:Label = Label(""+person.health, labelStyle)
            healthLabel.setFontScale(normalFontScale)

            groupTable.add(nameLabel).right()
            groupTable.row()
            groupTable.add(healthLabel).right()
            groupTable.row()
        }
    }

    /**
     * Builds the supply table layout
     */
    fun buildSupplyTable(){
        supplyTable.clear()
        supplyAmountList.clear()

        supplyTable.padLeft(10f)
        supplyTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val title = Label("Supplies", labelStyle)
        title.setFontScale(titleFontScale)

        //supplyTable.add(title).left()
        //supplyTable.row()

        val innerTable = Table()
        val list = SupplyManager.getSupplyList()
        for(i in list.indices){
            val rowTable:Table = Table()
            val value = list[i]
            val nameLabel = Label(value.displayName, labelStyle)
            nameLabel.setFontScale(normalFontScale)
            nameLabel.setAlignment(Align.left)

            val amtLabel = Label(""+value.amt.toInt(), labelStyle)
            amtLabel.setFontScale(normalFontScale)
            amtLabel.setAlignment(Align.left)

            supplyAmountList += amtLabel

            innerTable.add(nameLabel).left().padRight(5f)
            innerTable.add(amtLabel).left().width(40f)

            //rowTable.left()

            //innerTable.add(rowTable).fillX().expandX()
            innerTable.row()

        }

        supplyTable.add(innerTable)
    }


    fun triggerEventGUI(event: DataManager.EventJson, callbackTask : (choice:String)->Unit){
        game.pauseGame()

//        EventInfo.outerEventTable.debugAll()
//        EventInfo.eventTable.debugAll()
//        EventInfo.eventChoicesTable.debugAll()

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        EventInfo.titleLabel = Label(event.title, labelStyle)
        EventInfo.titleLabel!!.setAlignment(Align.center)
        EventInfo.titleLabel!!.setFontScale(eventTitleFontScale)
        EventInfo.titleLabel!!.setWrap(true)

        EventInfo.outerEventTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("log2", Texture::class.java)))
        EventInfo.outerEventTable.setSize(400f, 400f)
        EventInfo.outerEventTable.setPosition(Gdx.graphics.width/2f - 200, Gdx.graphics.height/2f - 200)

        TextGame.stage.addActor(EventInfo.outerEventTable)

        showEventPage(event, callbackTask, 0)

    }

    private fun showEventPage(event: DataManager.EventJson, callbackTask : (choice:String)->Unit, page:Int){
        //Clear the tables
        EventInfo.outerEventTable.clear()
        EventInfo.eventTable.clear()
        EventInfo.eventChoicesTable.clear()

        //Set some styles
        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val imageButtonStyle:ImageButton.ImageButtonStyle = ImageButton.ImageButtonStyle()
        val drawable = TextureRegionDrawable(TextureRegion(TextGame.manager.get("nextButtonWhite", Texture::class.java)))

        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        //val padding:Int = 400/(event.choices!!.size+1)/2

        //Make the buttons for the choices (if any)
        for(choice in event.choices?.iterator()){
            val button = TextButton("- $choice -", textButtonStyle)
            button.pad(0f, 10f, 0f, 10f)
            button.label.setFontScale(buttonFontScale)
            EventInfo.eventChoicesTable.add(button)
            EventInfo.eventChoicesTable.row()

            button.addListener(object:ChangeListener(){
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    //EventInfo.outerEventTable.remove()
                    System.out.println(button.text.toString().substring(2, button.text.length - 2))
                    callbackTask(button.text.toString().substring(2, button.text.length - 2))
                }
            })
        }

        //Fix the description
        val desc = event.description[page].replace("%n", event.randomName)

        //Make the description label
        val descLabel = Label(desc, labelStyle)
        descLabel.setAlignment(Align.top)
        descLabel.setFontScale(normalFontScale)
        descLabel.setWrap(true)

        //Make the next page button
        val nextPageButton:ImageButton = ImageButton(drawable)

        //Add the title and description label
        EventInfo.eventTable.add(EventInfo.titleLabel).width(250f).height(45f).padTop(15f)
        EventInfo.eventTable.row().expand()
        EventInfo.eventTable.add(descLabel).width(310f).padTop(10f).expand().fill()
        EventInfo.eventTable.row().expand()

        //If some things, add the next page button.
        if(event.description.size - 1 > page || (event.outcomes != null && event.outcomes!!.size > 0) ||
                (event.choices != null && event.choices!!.size > 0) || (event.resultingAction != null && event.resultingAction!!.size > 0))

            EventInfo.eventTable.add(nextPageButton).size(32f).padBottom(60f).bottom()

        //Otherwise, add a close button.
        else{
            val closeButton:TextButton = TextButton("- Close -", textButtonStyle)
            closeButton.label.setFontScale(buttonFontScale)
            closeButton.addListener(object:ChangeListener(){
                override fun changed(evt: ChangeEvent?, actor: Actor?) {
                    callbackTask("")
                }
            })

            EventInfo.eventTable.add(closeButton).padBottom(60f).bottom()
        }

        //Add all the stuff to the outer table.
        EventInfo.outerEventTable.add(EventInfo.eventTable).expand().fill()


        //Kinda complicated listener for the next page button.
        nextPageButton.addListener(object:ChangeListener(){
            override fun changed(evt: ChangeEvent?, actor: Actor?) {
                val hasOnlyOutcomes = (event.choices == null || (event.choices != null && event.choices!!.size == 0)) && (event.outcomes != null && event.outcomes!!.size > 0)
                val hasActions = event.resultingAction != null && event.resultingAction!!.size > 0

                if(event.description.size - 1 > page)
                    showEventPage(event, callbackTask, page+1)
                else if(hasOnlyOutcomes || (!hasOnlyOutcomes && hasActions)){
                    callbackTask("")
                }else{
                    EventInfo.eventTable.clear()
                    EventInfo.eventTable.add(EventInfo.titleLabel).width(250f).height(45f).padTop(15f)
                    EventInfo.eventTable.row()
                    EventInfo.eventTable.add(EventInfo.eventChoicesTable).expand().fill().padBottom(60f)
                }
            }
        })
    }

    fun showEventResults(list: List<Pair<Int, String>>){
        EventInfo.eventResultsTable.clear()
        EventInfo.eventTable.clear()

        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)
        val redLabelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.RED)
        val greenLabelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.GREEN)

        val okayButton:TextButton = TextButton("Okay", textButtonStyle)
        okayButton.label.setFontScale(buttonFontScale)

        for(item in list){
            val nameLabel = Label(item.second, labelStyle)
            var amtLabel:Label? = null
            if(item.first < 0) amtLabel = Label(item.first.toString(), redLabelStyle)
            else amtLabel = Label("+${item.first}", greenLabelStyle)

            nameLabel.setFontScale(normalFontScale)
            amtLabel.setFontScale(normalFontScale)

            EventInfo.eventResultsTable.add(amtLabel).padRight(10f)
            EventInfo.eventResultsTable.add(nameLabel)
            EventInfo.eventResultsTable.row()
        }

        EventInfo.eventTable.add(EventInfo.titleLabel).height(45f).width(250f).padTop(15f)
        EventInfo.eventTable.row()
        EventInfo.eventTable.add(EventInfo.eventResultsTable).expand().fill()
        EventInfo.eventTable.row()
        EventInfo.eventTable.add(okayButton).padBottom(60f).bottom()

        okayButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                game.resumeGame()
                EventInfo.outerEventTable.remove()
            }
        })
    }

    fun closeEvent(){
        EventInfo.outerEventTable.remove()
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

    private object EventInfo{
        val eventTable:Table = Table()
        val eventChoicesTable:Table = Table()
        val outerEventTable:Table = Table()
        val eventResultsTable:Table = Table()
        var titleLabel:Label? = null
    }
}