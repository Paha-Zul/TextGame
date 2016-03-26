package com.quickbite.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.quickbite.game.managers.*
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
    private val campTable:Table = Table() //For the camp screen
    private val centerInfoTable:Table = Table()
    private val leftTable:Table = Table()
    private val rightTable:Table = Table()

    private val numPadTable:Table = Table()

    private lateinit var timeLabel:Label

    /* GUI elements for trade */
    private val tradeWindowTable:Table = Table()
    private val tradeSliderWindow:Table = Table()


    /* GUI elements for travel info */
    private lateinit var distanceLabel:Label
    private lateinit var totalDaysLabel:Label

    /* GUI elements for people */
    private val groupTable:Table = Table() //For the group
    private val supplyTable:Table = Table() //For the supplies

    private val ROVTable:Table = Table()

    /* Gui elements for events */

    private val supplyAmountList:MutableList<Label> = arrayListOf()

    /* Tab buttons */
    private lateinit var supplyButton:TextButton
    private lateinit var groupButtonTab:TextButton
    private lateinit var campButtonTab:TextButton

    /* Camp specific stuff */
    private lateinit var activityHourLabel:Label
    private lateinit var activityHourSlider:Slider
    private lateinit var activityButton:TextButton

    private lateinit var selectBox:SelectBox<Label>


    private lateinit var pauseButton:ImageButton

    private lateinit var distProgressBar:ProgressBar

    fun init(){
        buildTravelScreenGUI()
        applyTravelTab(groupTable)
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
                if(ROVTable.parent == null && groupTable.parent == null) {
                    buildGroupTable()
                    rightTable.add(groupTable)
                }else if(ROVTable.parent == null){
                    groupTable.remove()
                    rightTable.debugAll()
                    buildROVTable()
                    rightTable.add(ROVTable)
                }else{
                    ROVTable.remove()
                    groupTable.remove()
                }
            }
        })

        campButtonTab.addListener(object:ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                campButtonTab.isChecked = false

                if(game.state == GameScreen.State.TRAVELING) {
                    game.changeToCamp()
                    campButtonTab.setText("Travel")
                    applyCampTab()
                }else if(game.state == GameScreen.State.CAMP) {
                    game.changeToTravel()
                    campButtonTab.setText("Camp")
                    applyTravelTab(groupTable)
                }
            }
        })

        activityHourSlider.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                activityHourLabel.setText(activityHourSlider.value.toInt().toString() + " hours")
            }
        })

        activityButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                game.numHoursToAdvance = activityHourSlider.value.toInt()

                //Get the activity.
                game.searchActivity = DataManager.SearchActivityJSON.getSearchActivity(selectBox.selected.text.toString())

                //If not null, get the action.
                if(game.searchActivity != null) {
                    val l = game.searchActivity!!.action

                    //If not null, set up the search function
                    if(l != null)
                        game.searchFunc = { EventManager.callEvent(l[0], l.slice(1.rangeTo(l.size))) }
                }
            }
        })

        game.timeTickEventList += ChainTask({ activityHourSlider.value <= 0},
            {
                activityHourSlider.value = activityHourSlider.value-1
                game.searchFunc?.invoke()
            },
            {game.searchActivity = null; game.searchFunc = null})
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
        TextGame.stage.addActor(leftTable)
        TextGame.stage.addActor(rightTable)
        TextGame.stage.addActor(pauseButton)
        TextGame.stage.addActor(campButtonTab)
    }

    /**
     * Applies the travel screen GUI stuff, which is initially only the group stats
     * and supplies info.
     */
    fun applyCampTab(){
        TextGame.stage.clear()
        mainTable.clear()

        TextGame.stage.addActor(centerInfoTable)
        TextGame.stage.addActor(leftTable)
        TextGame.stage.addActor(rightTable)
        TextGame.stage.addActor(pauseButton)
        TextGame.stage.addActor(campButtonTab)

        //campTable.bottom().left()
        campTable.setFillParent(true)

        TextGame.stage.addActor(campTable)
    }

    fun buildTravelScreenGUI(){
        val barStyle:ProgressBar.ProgressBarStyle = ProgressBar.ProgressBarStyle()
        barStyle.background = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("art/bar.png"))))
        barStyle.knobBefore = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("art/pixel.png"))))

        val textButtonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

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
        pauseButton.setPosition(TextGame.viewport.screenWidth/1.4f, TextGame.viewport.screenHeight - pauseButton.height)

        campButtonTab = TextButton("Camp", textButtonStyle)
        campButtonTab.setSize(40f, 40f)
        campButtonTab.setOrigin(Align.center)
        campButtonTab.setPosition(TextGame.viewport.screenWidth/4f, TextGame.viewport.screenHeight - campButtonTab.height)
        campButtonTab.label.setFontScale(buttonFontScale)

        //distanceTable.row()
        //distanceTable.add(distProgressBar).height(25f).width(150f)

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

    }

    /**
     * Builds the right table of the main screen which includes the "Exomer" button and backgrounds.
     */
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

        rightTable.add(groupButtonTab).right().top().size(130f, 40f)
        rightTable.row()
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
     * Builds the group table layout.
     */
    fun buildROVTable(){
        ROVTable.clear()

        ROVTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))
        ROVTable.padRight(10f)

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val ROVNameLabel = Label("ROV", labelStyle)
        ROVNameLabel.setFontScale(normalFontScale)

        val ROVHealthLabel:Label = Label(""+ROVManager.ROVHealth, labelStyle)
        ROVHealthLabel.setFontScale(normalFontScale)

        val batteryNameLabel = Label("Battery", labelStyle)
        batteryNameLabel.setFontScale(normalFontScale)

        val batteryHealthLabel:Label = Label(""+ROVManager.batteryHealth, labelStyle)
        batteryHealthLabel.setFontScale(normalFontScale)

        val storageNameLabel = Label("Storage", labelStyle)
        storageNameLabel.setFontScale(normalFontScale)

        val storageHealthLabel:Label = Label(""+ROVManager.storageHealth, labelStyle)
        storageHealthLabel.setFontScale(normalFontScale)

        val panelNameLabel = Label("Solar Panel", labelStyle)
        panelNameLabel.setFontScale(normalFontScale)

        val panelHealthLabel:Label = Label(""+ROVManager.solarPanelHealth, labelStyle)
        panelHealthLabel.setFontScale(normalFontScale)

        val trackNameLabel = Label("Tracks", labelStyle)
        trackNameLabel.setFontScale(normalFontScale)

        val trackHealthLabel:Label = Label(""+ROVManager.trackHealth, labelStyle)
        trackHealthLabel.setFontScale(normalFontScale)

        ROVTable.add(ROVNameLabel).right()
        ROVTable.row()
        ROVTable.add(ROVHealthLabel).right()
        ROVTable.row()
        ROVTable.add(batteryNameLabel).right()
        ROVTable.row()
        ROVTable.add(batteryHealthLabel).right()
        ROVTable.row()
        ROVTable.add(storageNameLabel).right()
        ROVTable.row()
        ROVTable.add(storageHealthLabel).right()
        ROVTable.row()
        ROVTable.add(panelNameLabel).right()
        ROVTable.row()
        ROVTable.add(panelHealthLabel).right()
        ROVTable.row()
        ROVTable.add(trackNameLabel).right()
        ROVTable.row()
        ROVTable.add(trackHealthLabel).right()
        ROVTable.row()
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
        campButtonTab.isDisabled = true;
        EventInfo.outerEventTable.clear()

        //        EventInfo.outerEventTable.debugAll()
//        EventInfo.eventTable.debugAll()
//        EventInfo.eventChoicesTable.debugAll()

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        EventInfo.titleLabel = Label(event.title, labelStyle)
        EventInfo.titleLabel!!.setAlignment(Align.center)
        EventInfo.titleLabel!!.setFontScale(eventTitleFontScale)
        EventInfo.titleLabel!!.setWrap(true)

        EventInfo.eventBackgroundTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("log2", Texture::class.java)))
        EventInfo.eventBackgroundTable.setSize(400f, 400f)

        EventInfo.outerEventTable.setFillParent(true)
        EventInfo.outerEventTable.add(EventInfo.eventBackgroundTable)
        TextGame.stage.addActor(EventInfo.outerEventTable)

        showEventPage(event, callbackTask, 0)
    }

    /**
     * Shows an individual event page
     */
    private fun showEventPage(event: DataManager.EventJson, nextEventName: (choice:String)->Unit, page:Int){
        //Clear the tables
        EventInfo.eventBackgroundTable.clear()
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
        for(choice in event.choices!!.iterator()){
            val button = TextButton("($choice)", textButtonStyle)
            button.pad(0f, 10f, 0f, 10f)
            button.label.setFontScale(buttonFontScale)
            EventInfo.eventChoicesTable.add(button).height(50f)
            EventInfo.eventChoicesTable.row()

            button.addListener(object:ChangeListener(){
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    //EventInfo.outerEventTable.remove()
                    nextEventName(button.text.toString().substring(1, button.text.length - 1))
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

            EventInfo.eventTable.add(nextPageButton).size(50f).padBottom(60f).bottom()

        //Otherwise, add a close button.
        else{
            val closeButton:TextButton = TextButton("- Close -", textButtonStyle)
            closeButton.label.setFontScale(buttonFontScale)
            closeButton.addListener(object:ChangeListener(){
                override fun changed(evt: ChangeEvent?, actor: Actor?) {
                    nextEventName("") //This will basically end the event.
                }
            })

            EventInfo.eventTable.add(closeButton).padBottom(60f).bottom().height(50f)
        }

        //Add all the stuff to the outer table.
        EventInfo.eventBackgroundTable.add(EventInfo.eventTable).expand().fill()


        //Kinda complicated listener for the next page button.
        nextPageButton.addListener(object:ChangeListener(){
            override fun changed(evt: ChangeEvent?, actor: Actor?) {
                val hasOnlyOutcomes = (event.choices == null || (event.choices != null && event.choices!!.size == 0)) && (event.outcomes != null && event.outcomes!!.size > 0)
                val hasActions = event.resultingAction != null && event.resultingAction!!.size > 0

                //If we have another description, simply go to the next page.
                if(event.description.size - 1 > page)
                    showEventPage(event, nextEventName, page+1)

                //If we have only outcomes or only actions, trigger the end of the event. This will probably result in something being gained or lossed
                else if(hasOnlyOutcomes || (!hasOnlyOutcomes && hasActions)){
                    nextEventName("")

                //Otherwise, we have a choice to make! Layout the choices!
                }else{
                    EventInfo.eventTable.clear()
                    EventInfo.eventTable.add(EventInfo.titleLabel).width(250f).height(45f).padTop(15f)
                    EventInfo.eventTable.row()
                    EventInfo.eventTable.add(EventInfo.eventChoicesTable).expand().fill().padBottom(60f)
                }
            }
        })
    }

    /**
     * Shows the event results
     */
    fun showEventResults(list: List<Pair<Int, String>>){
        EventInfo.eventResultsTable.clear()
        EventInfo.eventTable.clear()

        /* Styles */
        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)
        val redLabelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.RED)
        val greenLabelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.GREEN)

        //Close button
        val closeButton:TextButton = TextButton("- Close -", textButtonStyle)
        closeButton.label.setFontScale(buttonFontScale)

        //Generate all the button choices.
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

        //Arrange it in the table.
        EventInfo.eventTable.add(EventInfo.titleLabel).height(45f).width(250f).padTop(15f)
        EventInfo.eventTable.row()
        EventInfo.eventTable.add(EventInfo.eventResultsTable).expand().fill()
        EventInfo.eventTable.row()
        EventInfo.eventTable.add(closeButton).padBottom(60f).bottom().height(50f)

        //Create a listener
        closeButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                game.resumeGame()
                closeEvent()
            }
        })
    }

    /**
     * Closes the event window.
     */
    fun closeEvent(){
        campButtonTab.isDisabled = false;
        EventInfo.eventBackgroundTable.remove()
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

        activityHourLabel = Label("0 hours", labelStyle)
        activityHourLabel.setFontScale(normalFontScale)

        activityHourSlider = Slider(0f, 24f, 1f, false, sliderStyle)

        activityButton = TextButton("Activity!", buttonStyle)
        activityButton.label.setFontScale(buttonFontScale)

        val innerTable:Table = Table()
        innerTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))

        innerTable.add(buildDropdownList()).width(300f).height(25f)
        innerTable.row().padTop(20f)
        innerTable.add(activityHourLabel)
        innerTable.row()
        innerTable.add(activityHourSlider).width(150f).height(25f)
        innerTable.row()
        innerTable.add(activityButton).width(100f).height(25f)

        campTable.add(innerTable)
    }

    private fun buildDropdownList():Actor{
        val newFont = BitmapFont(Gdx.files.internal("fonts/spaceFont2.fnt"))
        newFont.data.setScale(normalFontScale)

        val labelStyle = Label.LabelStyle(newFont, Color.WHITE)
        labelStyle.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))

        val scrollStyle:ScrollPane.ScrollPaneStyle = ScrollPane.ScrollPaneStyle()

        val listStyle:com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle = com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle()
        listStyle.font = newFont
        listStyle.fontColorSelected = Color.WHITE
        listStyle.fontColorUnselected = Color.WHITE
        listStyle.selection = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))
        listStyle.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))

        val selectBoxStyle:SelectBox.SelectBoxStyle = SelectBox.SelectBoxStyle()
        selectBoxStyle.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("darkPixel", Texture::class.java)))
        selectBoxStyle.listStyle = listStyle
        selectBoxStyle.scrollStyle = scrollStyle
        selectBoxStyle.font = newFont
        selectBoxStyle.fontColor = Color.WHITE

        selectBox = SelectBox(selectBoxStyle)
        //selectBox.setScale(normalFontScale)

        val list:com.badlogic.gdx.utils.Array<Label> = com.badlogic.gdx.utils.Array()
        for(sa in DataManager.getSearchActiviesList()){
            val label = CustomLabel(sa.buttonTitle, labelStyle)
            label.setFontScale(normalFontScale)
            list.add(label)
        }

        selectBox.items = list

//        selectBox.setSelectedAlignment(Align.center)
//        selectBox.setListAlignment(Align.center)
        return selectBox
    }

    fun applyCampTable(){
        TextGame.stage.addActor(campTable)
    }

    /**
     * Makes the trade window. Needs to be called before openTradeWindow()
     */
    fun buildTradeWindow(){
        tradeWindowTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("TradeWindow2", Texture::class.java)))
        tradeWindowTable.setSize(450f, 400f)

        val gridPatchDrawable = NinePatchDrawable(NinePatch(TextGame.manager.get("tradeGridBackground", Texture::class.java), 4, 4, 4, 4))

        val labelTable:Table = Table()
        val listTable:Table = Table()
        val offerTable:Table = Table()

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val amtLabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val giveButtonStyle = ImageButton.ImageButtonStyle()
        giveButtonStyle.imageUp = TextureRegionDrawable(TextureRegion(TextGame.manager.get("nextButtonWhite", Texture::class.java)))

        val takeButtonStyle = ImageButton.ImageButtonStyle()
        takeButtonStyle.imageUp = TextureRegionDrawable(TextureRegion(TextGame.manager.get("nextButtonWhiteLeft", Texture::class.java)))

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        val exomerLabel = Label("Exomer751", labelStyle)
        exomerLabel.setFontScale(0.15f)
        exomerLabel.setAlignment(Align.center)

        val nativeLabel = Label("Natives", labelStyle)
        nativeLabel.setFontScale(0.15f)
        nativeLabel.setAlignment(Align.center)

        val yourOfferLabel = Label("Your Offer:", labelStyle)
        val yourOfferAmtLabel = Label("0", labelStyle)
        yourOfferLabel.setFontScale(0.15f)
        yourOfferAmtLabel.setFontScale(0.15f)
        yourOfferAmtLabel.setAlignment(Align.center)
        yourOfferAmtLabel.color = Color.GREEN

        val otherOfferAmtLabel = Label("0", labelStyle)
        val otherOfferLabel = Label(":Their Offer", labelStyle)
        otherOfferAmtLabel.setFontScale(0.15f)
        otherOfferAmtLabel.setAlignment(Align.center)
        otherOfferAmtLabel.color = Color.RED
        otherOfferLabel.setFontScale(0.15f)

        TradeManager.generateLists()

        val amountLabel = Label("Amt", labelStyle)
        amountLabel.setFontScale(0.15f)
        amountLabel.setAlignment(Align.center)

        val otherAmountLabel = Label("Amt", labelStyle)
        otherAmountLabel.setFontScale(0.15f)
        otherAmountLabel.setAlignment(Align.center)

        val nameLabel = Label("Name", labelStyle)
        nameLabel.setFontScale(0.15f)
        nameLabel.setAlignment(Align.center)

        val otherNameLabel = Label("Name", labelStyle)
        otherNameLabel.setFontScale(0.15f)
        otherNameLabel.setAlignment(Align.center)

        val valueLabel = Label("Worth", labelStyle)
        valueLabel.setFontScale(0.15f)
        valueLabel.setAlignment(Align.center)

        val otherValueLabel = Label("Cost", labelStyle)
        otherValueLabel.setFontScale(0.15f)
        otherValueLabel.setAlignment(Align.center)

        val tradeTitleLabel = Label("Trade", labelStyle)
        tradeTitleLabel.setFontScale(0.15f)
        tradeTitleLabel.setAlignment(Align.center)

        val acceptButton = TextButton("Accept", textButtonStyle)
        acceptButton.label.setFontScale(0.15f)

        //The item list.
        val exomerList = TradeManager.exomerList
        val otherList = TradeManager.otherList

        val spaceX = 4f

        //For each item....
        for(i in exomerList!!.indices){
            val exItem = exomerList[i]
            val otherItem = otherList!![i]

            val exomerItemNameLabel = Label(exItem.abbrName, labelStyle)
            exomerItemNameLabel.setFontScale(0.13f)
            exomerItemNameLabel.setAlignment(Align.center)

            val exomerItemAmountLabel = Label(exItem.amt.toInt().toString(), labelStyle)
            exomerItemAmountLabel.setFontScale(0.13f)
            exomerItemAmountLabel.setAlignment(Align.center)

            val exomerItemValueLabel = Label(exItem.worth.toString(), labelStyle)
            exomerItemValueLabel.setFontScale(0.13f)
            exomerItemValueLabel.setAlignment(Align.center)
            exomerItemValueLabel.color = Color.GREEN

            val nativeItemNameLabel = Label(otherItem.abbrName, labelStyle)
            nativeItemNameLabel.setFontScale(0.13f)
            nativeItemNameLabel.setAlignment(Align.center)

            val nativeItemAmountLabel = Label(otherItem.amt.toInt().toString(), labelStyle)
            nativeItemAmountLabel.setFontScale(0.13f)
            nativeItemAmountLabel.setAlignment(Align.center)

            val nativeItemValueLabel = Label(otherItem.worth.toString(), labelStyle)
            nativeItemValueLabel.setFontScale(0.13f)
            nativeItemValueLabel.setAlignment(Align.center)
            nativeItemValueLabel.color = Color.RED

            if(i == 0){
                listTable.add(valueLabel).width(60f).spaceRight(spaceX).height(24f)
                listTable.add(nameLabel).width(76f).spaceRight(spaceX)
                listTable.add(amountLabel).width(44f).spaceRight(spaceX)

                listTable.add(tradeTitleLabel).width(58f).spaceRight(spaceX)

                listTable.add(otherAmountLabel).width(44f).spaceRight(spaceX)
                listTable.add(otherNameLabel).width(76f).spaceRight(spaceX)
                listTable.add(otherValueLabel).width(60f)

                listTable.row().spaceTop(5f)
            }

            val amtLabel = Label("0", amtLabelStyle)
            amtLabel.setFontScale(0.13f)
            amtLabel.setAlignment(Align.center)

            val takeButton = ImageButton(takeButtonStyle)
            val giveButton = ImageButton(giveButtonStyle)

            //Add the amount then name to the left table.
            listTable.add(exomerItemValueLabel).left().fillX().spaceRight(spaceX).spaceBottom(spaceX).height(24f)
            listTable.add(exomerItemNameLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)
            listTable.add(exomerItemAmountLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)

            //Add the stuff to the center table.
            listTable.add(amtLabel).space(0f, 5f, 0f, 5f).fillX().spaceRight(spaceX).spaceBottom(spaceX)

            //Add the name then amount to the right table.
            listTable.add(nativeItemAmountLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)
            listTable.add(nativeItemNameLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)
            listTable.add(nativeItemValueLabel).right().fillX().spaceBottom(spaceX)

            val func = { newAmt: Int ->

                val absChangeAmt:Int = Math.abs(newAmt)

                var tradeAmt = amtLabel.text.toString().toInt()
                var yourOffer = yourOfferAmtLabel.text.toString().toInt()
                var otherOffer = otherOfferAmtLabel.text.toString().toInt()

                //If the trade amount is positive and change amount is negative.
                if(tradeAmt > 0 && newAmt < 0){
                    val toZero = tradeAmt
                    val toChange = newAmt

                    yourOffer -= exItem.worth*toChange
                    otherOffer -= otherItem.worth*toZero
                    if(otherOffer <= 0) otherOffer = 0

                //If trade amount is negative and change amount is positive.
                }else if(tradeAmt < 0 && newAmt > 0){
                    val toZero = tradeAmt
                    val toChange = newAmt

                    yourOffer += exItem.worth*toZero
                    otherOffer += otherItem.worth*toChange
                    if(otherOffer <= 0) otherOffer = 0

                }else if(tradeAmt >= 0 && newAmt >= 0){
                    otherOffer += otherItem.worth*(newAmt - tradeAmt)
                    if(otherOffer <=0) otherOffer = 0
                    //otherOffer -= otherItem.worth*(changeAmt - tradeAmt)
                }else if(tradeAmt <= 0 && newAmt <= 0){
                    //yourOffer -= exItem.worth*(changeAmt - tradeAmt)
                    yourOffer -= exItem.worth*(newAmt - tradeAmt)
                    if(yourOffer <= 0) yourOffer = 0
                }

                //Change the amt that we got from the text.
                tradeAmt = newAmt

                //Set the exomer and other item amount label.
                exomerItemAmountLabel.setText(exItem.currAmt.toInt().toString())
                nativeItemAmountLabel.setText(otherItem.currAmt.toInt().toString())

                //If amt is positive, make it green. Negative, make it red. 0, make it white.
                when {
                    tradeAmt > 0 -> amtLabel.color = Color.GREEN
                    tradeAmt < 0 -> amtLabel.color = Color.RED
                    else -> amtLabel.color = Color.WHITE
                }

                when {
                    yourOffer < otherOffer -> acceptButton.label.color = Color.RED
                    else -> acceptButton.label.color = Color.WHITE
                }

                //Change the offer amounts.
                amtLabel.setText(tradeAmt.toString())
                yourOfferAmtLabel.setText(yourOffer.toString())
                otherOfferAmtLabel.setText(otherOffer.toString())
            }

            amtLabel.addListener(object:ClickListener(){
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    openTradeSlider(exomerList[i], otherList[i], func)
                    super.clicked(event, x, y)
                }
            })

            takeButton.addListener(object:ChangeListener(){
                override fun changed(p0: ChangeEvent?, p1: Actor?) {
                    func(amtLabel.text.toString().toInt() + 1)
                }
            })

            giveButton.addListener(object:ChangeListener(){
                override fun changed(p0: ChangeEvent?, p1: Actor?) {
                    func(amtLabel.text.toString().toInt() - 1)
                }
            })

            listTable.row()
        }

        acceptButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                val yourOffer = yourOfferAmtLabel.text.toString().toInt()
                val theirOffer = otherOfferAmtLabel.text.toString().toInt()
                if(yourOffer >= theirOffer){
                    for(item in exomerList)
                        SupplyManager.setSupply(item.name, item.amt.toFloat())

                    updateSuppliesGUI()
                    closeTradeWindow()
                }
            }
        })

        //Add stuff to the offer (your/their offer) table
        offerTable.add(yourOfferLabel).right().padRight(5f)
        offerTable.add(yourOfferAmtLabel).right().spaceRight(30f).width(25f).height(25f)
        offerTable.add()
        offerTable.add(otherOfferAmtLabel).left().padRight(5f).spaceLeft(30f).width(25f).height(25f)
        offerTable.add(otherOfferLabel).left()

        //The titles table
        labelTable.add(exomerLabel).width(215f).height(30f)
        labelTable.add(nativeLabel).width(215f).height(30f)
        labelTable.top()

        // Add all the tables to the main table.
        tradeWindowTable.add(labelTable).fillX().expandX().pad(0f, 10f, 4f, 10f).top().height(30f)
        tradeWindowTable.row()
        tradeWindowTable.add(listTable).pad(0f, 5f, 5f, 5f).top()
        tradeWindowTable.row()
        tradeWindowTable.add(offerTable).fillX().expandX().top().spaceBottom(0f)
        tradeWindowTable.row()
        tradeWindowTable.add(acceptButton).fill().expand().padBottom(4f)

        tradeWindowTable.setPosition(TextGame.viewport.worldWidth/2f - tradeWindowTable.width/2f, TextGame.viewport.worldHeight/2f - tradeWindowTable.height/2f)

//        tradeWindowTable.debugAll()
//        listTable.debugAll()

//        mainTradeWindowTable.add(tradeWindowTable).left()

//        mainTradeWindowTable.setFillParent(true)
    }

    /**
     * Opens the trade window. buildTradeWindow() needs to be called before.
     */
    fun openTradeWindow() = TextGame.stage.addActor(tradeWindowTable)

    /**
     * Closes the trade window.
     */
    fun closeTradeWindow() = tradeWindowTable.remove()

    /**
     * Opens the slider for a particular trade item.
     */
    private fun openTradeSlider(exItem: TradeManager.TradeSupply, oItem: TradeManager.TradeSupply, callback:(Int)->Unit){
        tradeSliderWindow.clear()
        tradeSliderWindow.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("pixelBlack", Texture::class.java)))
        tradeSliderWindow.setSize(300f, 100f)
        tradeSliderWindow.setPosition(TextGame.viewport.worldWidth/2f - 300f/2f, TextGame.viewport.worldHeight/2f - 100f/2f)

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE
//        buttonStyle.up = TextureRegionDrawable(TextureRegion(TextGame.manager.get("", Texture::class.java)))

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val sliderStyle = Slider.SliderStyle()
        sliderStyle.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("sliderWhite", Texture::class.java)))
        sliderStyle.knob = TextureRegionDrawable(TextureRegion(TextGame.manager.get("sliderKnob", Texture::class.java)))

        val tradeSlider = Slider(-oItem.amt, exItem.amt, 1f, false, sliderStyle)
        tradeSlider.value = 0f

        val currLabel = Label("0", labelStyle)
        currLabel.setFontScale(0.15f)
        currLabel.setAlignment(Align.center)

        val minLabel = Label((-oItem.amt).toInt().toString(), labelStyle)
        minLabel.setFontScale(0.15f)
        minLabel.setAlignment(Align.center)

        val maxLabel = Label(exItem.amt.toInt().toString(), labelStyle)
        maxLabel.setFontScale(0.15f)
        maxLabel.setAlignment(Align.center)

        val lessButton = TextButton("-", buttonStyle)
        lessButton.label.setFontScale(0.17f)
        lessButton.label.setAlignment(Align.center)

        val moreButton = TextButton("+", buttonStyle)
        moreButton.label.setFontScale(0.17f)
        moreButton.label.setAlignment(Align.center)

        val acceptButton = TextButton("Accept", buttonStyle)
        acceptButton.label.setFontScale(0.15f)
        acceptButton.label.setAlignment(Align.center)

        tradeSliderWindow.add(lessButton)
        tradeSliderWindow.add(currLabel)
        tradeSliderWindow.add(moreButton)

        tradeSliderWindow.row()

        tradeSliderWindow.add(minLabel).spaceRight(5f)
        tradeSliderWindow.add(tradeSlider).spaceRight(5f)
        tradeSliderWindow.add(maxLabel)

        tradeSliderWindow.row()

        tradeSliderWindow.add(acceptButton).colspan(3)

//        tradeSlider.addListener(object:ClickListener(){
//            override fun clicked(event: InputEvent?, x: Float, y: Float) {
//                currLabel.setText(tradeSlider.value.toInt().toString())
//                callback(tradeSlider.value.toInt())
//                super.clicked(event, x, y)
//            }
//        })

        tradeSlider.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                currLabel.setText(tradeSlider.value.toInt().toString())
                callback(tradeSlider.value.toInt())
            }
        })

        moreButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                tradeSlider.value += 1
            }
        })

        lessButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                tradeSlider.value -= 1
            }
        })

        acceptButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                closeTradeSlider()
            }
        })


        TextGame.stage.addActor(tradeSliderWindow)
    }

    fun closeTradeSlider() = tradeSliderWindow.remove()

    private object EventInfo{
        val eventTable:Table = Table()
        val eventChoicesTable:Table = Table()
        val outerEventTable:Table = Table()
        val eventBackgroundTable:Table = Table()
        val eventResultsTable:Table = Table()
        var titleLabel:Label? = null
    }

    private class CustomLabel(text: CharSequence?, style: LabelStyle?): Label(text, style) {
        override fun toString(): String {
            return this.text.toString()
        }
    }
}