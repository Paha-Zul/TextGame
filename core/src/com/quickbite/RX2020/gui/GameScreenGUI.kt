package com.quickbite.rx2020.gui

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
import com.quickbite.rx2020.*
import com.quickbite.rx2020.managers.*
import com.quickbite.rx2020.screens.GameScreen
import com.quickbite.rx2020.screens.MainMenuScreen
import com.quickbite.rx2020.util.GH

/**
 * Created by Paha on 2/5/2016.
 */
class GameScreenGUI(val game : GameScreen) {
    private val normalFontScale = 0.15f
    private val titleFontScale = 0.25f
    private val eventTitleFontScale = 0.18f
    private val buttonFontScale = 0.15f

    private val mainTable: Table = Table()
    private val campTable: Table = Table() //For the camp screen
    private val centerInfoTable: Table = Table()
    private val leftTable: Table = Table()
    private val rightTable: Table = Table()

    private val recentChangeTable: Table = Table()

    private lateinit var timeLabel: Label

    /* GUI elements for trade */
    private val tradeWindowTable: Table = Table()
    private val tradeSliderWindow: Table = Table()

    /* GUI elements for travel info */
    private lateinit var distanceLabel: Label
    private lateinit var totalDaysLabel: Label

    /* GUI elements for people */
    private val groupTable: Table = Table() //For the group
    private val supplyTable: Table = Table() //For the supplies

    private val ROVTable: Table = Table()

    /* Settings Table & stuff */
    private lateinit var settingsButton:ImageButton
    private val settingsTable = Table()

    /* Gui elements for events */

    private val supplyAmountList:MutableList<Label> = arrayListOf()

    /* Tab buttons */
    private lateinit var supplyButton: TextButton
    private lateinit var groupButton: TextButton
    private lateinit var campButton: TextButton

    /* Camp specific stuff */
    private lateinit var activityHourLabel: Label
    private lateinit var activityHourSlider: Slider
    private lateinit var activityButton: TextButton

    private lateinit var selectBox: SelectBox<Label>

    private lateinit var pauseButton: ImageButton

    private lateinit var distProgressBar: ProgressBar

    init{
        instance = this
    }

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
        totalDaysLabel.setText("Day "+ GameStats.TimeInfo.totalDaysTraveled)

        distanceLabel.setText("" + GameStats.TravelInfo.totalDistToGo+" Miles")
        distProgressBar.setValue(GameStats.TravelInfo.totalDistTraveled.toFloat())

        updateSuppliesGUI()
        buildRecentChangeTable()
    }

    private fun updateSuppliesGUI(){
        val list = SupplyManager.getSupplyList()
        for(i in list.indices){
            supplyAmountList[i].setText( list[i].amt.toInt().toString())
        }
    }

    fun addListeners(){
        pauseButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                when{
                    pauseButton.isChecked -> game.pauseGame()
                    else -> game.resumeGame()
                }
            }
        })

        supplyButton.addListener(object: ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                if(supplyTable.parent == null) {
                    leftTable.add(supplyTable)
                }else{
                    supplyTable.remove()
                }
            }
        })

        groupButton.addListener(object: ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
//                rightTable.debugAll()

                if(ROVTable.parent == null && groupTable.parent == null) {
                    buildGroupTable()
                    rightTable.add(groupTable)
                }else if(ROVTable.parent == null){
                    rightTable.clear()
                    addGroupButton()
                    buildROVTable()
                    rightTable.add(ROVTable)

                //TODO This is freaking broken.
                }else{
                    ROVTable.remove()
                    groupTable.remove()
                }
            }
        })

        campButton.addListener(object: ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                campButton.isChecked = false

                if(game.state == GameScreen.State.TRAVELING) {
                    game.changeToCamp()
                    campButton.setText("Travel")
                    applyCampTab()
                }else if(game.state == GameScreen.State.CAMP) {
                    game.changeToTravel()
                    campButton.setText("Camp")
                    applyTravelTab(groupTable)
                }
            }
        })

        activityHourSlider.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                activityHourLabel.setText(activityHourSlider.value.toInt().toString())
            }
        })

        activityButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                game.numHoursToAdvance = activityHourSlider.value.toInt()
                game.searchActivity = DataManager.SearchActivityJSON.getSearchActivity(selectBox.selected.text.toString())
                //If not null, get the action.
                val actionList = game.searchActivity!!.action!! //Get the action list
                game.searchFunc = Array(actionList.size, {i->null}) //Initialize an array to hold the events.

                var i =0
                for(params in actionList.iterator()) {
                    //If not null, set up the search function
                    game.searchFunc!![i] = { EventManager.callEvent(params[0], params.slice(1.rangeTo(params.size - 1))) }
                    i++
                }
            }
        })

        selectBox.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                game.searchActivity = DataManager.SearchActivityJSON.getSearchActivity(selectBox.selected.text.toString())
                val resultList = game.searchActivity!!.restrictions!!.filter {res -> SupplyManager.getSupply(res).amt <= 0 }
                if(resultList.size > 0)
                    disableActivityButton()
                else
                    enableActivityButton()
            }
        })

        settingsButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                openSettings()
            }
        })

        var task: ChainTask? = null
        task = ChainTask(
                { activityHourSlider.value <= 0 || game.searchActivity == null },
                {
                    if (game.searchActivity != null) {
                        val list = game.searchActivity!!.restrictions!!.filter { res -> SupplyManager.getSupply(res).amt <= 0 } //Filter out any restrictions that are less than 0
                        //If our list is 0, that means all the restrictions passed. We are good to proceed.
                        if (list.size == 0) {
                            activityHourSlider.value = activityHourSlider.value - 1
                            game.searchFunc?.forEach { func -> func?.invoke() }

                            //Otherwise, stop such.
                        } else {
                            game.searchActivity = null
                            game.searchFunc = null
                            disableActivityButton()
                        }
                    }
                },
                { game.searchActivity = null; game.searchFunc = null; game.numHoursToAdvance = 0;}
        )

        game.timeTickEventList += task
    }

    private fun disableActivityButton(){
        activityButton.label.color = Color.RED
        activityButton.isDisabled = true
    }

    private fun enableActivityButton(){
        activityButton.label.color = Color.WHITE
        activityButton.isDisabled = false
    }

    fun applyTravelTab(tableToApply: Table){
        mainTable.remove()
        mainTable.clear()
        campTable.remove()

        TextGame.stage.addActor(centerInfoTable)
        TextGame.stage.addActor(leftTable)
        TextGame.stage.addActor(rightTable)
        TextGame.stage.addActor(pauseButton)
        TextGame.stage.addActor(campButton)
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
        TextGame.stage.addActor(campButton)

        //campTable.bottom().left()
        campTable.setFillParent(true)

        TextGame.stage.addActor(campTable)
    }

    fun buildTravelScreenGUI(){
        val barStyle: ProgressBar.ProgressBarStyle = ProgressBar.ProgressBarStyle()
        barStyle.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("bar"))
        barStyle.knobBefore = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pixel"))

//        val settingsButtonStyle = ImageButton.ImageButtonStyle()
//        settingsButtonStyle.imageUp = TextureRegionDrawable(TextureRegion(TextGame.manager.get("gear", Texture::class.java)))

        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE
        textButtonStyle.up = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("darkPixel"))

        val pauseButtonStyle = ImageButton.ImageButtonStyle()
        var drawable = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("play"))
        pauseButtonStyle.imageChecked = drawable
        pauseButtonStyle.imageCheckedOver = drawable

        drawable = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pause"))
        pauseButtonStyle.imageUp =  drawable
        pauseButtonStyle.imageOver =  drawable
        pauseButtonStyle.imageDown =  drawable

        settingsButton = ImageButton(TextureRegionDrawable(TextureRegion(TextGame.manager.get("gear", Texture::class.java))))
        settingsButton.setSize(35f, 35f)
        settingsButton.setPosition(TextGame.viewport.worldWidth - 45f, 10f)

        distProgressBar = ProgressBar(0f, GameStats.TravelInfo.totalDistOfGame.toFloat(), 20f, false, barStyle)

        pauseButton = ImageButton(pauseButtonStyle)
        pauseButton.setSize(40f, 40f)
        pauseButton.setPosition(TextGame.viewport.screenWidth/1.4f, TextGame.viewport.screenHeight - pauseButton.height)

        campButton = TextButton("Camp", textButtonStyle)
        campButton.setSize(100f, 40f)
        campButton.setOrigin(Align.center)
        campButton.setPosition(TextGame.viewport.worldWidth/2f - campButton.width/2f, 0f)
        campButton.label.setFontScale(buttonFontScale)

        //distanceTable.row()
        //distanceTable.add(distProgressBar).height(25f).width(150f)

        TextGame.stage.addActor(settingsButton)

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
        innerTable.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("darkPixel"))

        val style: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        totalDaysLabel = Label("Day " + GameStats.TimeInfo.totalDaysTraveled, style)
        timeLabel = Label("12:00 AM", style)
        distanceLabel = Label("" + GameStats.TravelInfo.totalDistToGo + " Miles", style)

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

        val drawable = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("darkPixel"))

        val buttonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
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

        val drawable = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("darkPixel"))

        val buttonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE
        buttonStyle.up = drawable

        groupButton = TextButton("Exomer751", buttonStyle)
        groupButton.label.setFontScale(buttonFontScale)

        rightTable.setFillParent(true)
        rightTable.top().right()

        addGroupButton()
    }

    /**
     * We need this because apparently the tables are wacky and we have to readd the group button a lot.
     */
    private fun addGroupButton(){
        rightTable.add(groupButton).right().top().size(130f, 40f)
        rightTable.row()
    }

    /**
     * Builds the group table layout.
     */
    fun buildGroupTable(){
        groupTable.clear()

        groupTable.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("darkPixel"))
        groupTable.padRight(10f)

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val list:Array<Person> = GroupManager.getPeopleList()
        for(person: Person in list.iterator()){
            val nameLabel = Label(person.fullName, labelStyle)
            nameLabel.setFontScale(normalFontScale)

            val healthLabel: Label = Label("" + person.healthNormal, labelStyle)
            healthLabel.setFontScale(normalFontScale)

            val healthBar: CustomHealthBar = CustomHealthBar(person, TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("bar"))),
                    TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("pixelWhite"))))

            groupTable.add(nameLabel).right()
            groupTable.row()
            groupTable.add(healthBar).right().height(15f).width(100f)
            groupTable.row().spaceTop(5f)
        }
    }

    /**
     * Builds the group table layout.
     */
    fun buildROVTable(){
        ROVTable.clear()

        ROVTable.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("darkPixel"))
        ROVTable.padRight(10f)

        val bg = TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("bar")))
        val pixel = TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("pixelWhite")))

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val ROVNameLabel = Label("ROV", labelStyle)
        ROVNameLabel.setFontScale(normalFontScale)

        val ROVbar: CustomHealthBar = CustomHealthBar(bg, pixel)

        ROVTable.add(ROVNameLabel).right()
        ROVTable.row()
        ROVTable.add(ROVbar).right().height(15f).width(100f)
        ROVTable.row().spaceTop(5f)

        val list = ROVManager.ROVPartList
        for(supply: SupplyManager.Supply in list.iterator()){
            val nameLabel = Label(supply.displayName, labelStyle)
            nameLabel.setFontScale(normalFontScale)

            val healthLabel: Label = Label("" + supply.currHealth, labelStyle)
            healthLabel.setFontScale(normalFontScale)

            val healthBar: CustomHealthBar = CustomHealthBar(supply, bg, pixel)

            ROVTable.add(nameLabel).right()
            ROVTable.row()
            ROVTable.add(healthBar).right().height(15f).width(100f)
            ROVTable.row().spaceTop(5f)
        }
    }

    /**
     * Builds the supply table layout
     */
    fun buildSupplyTable(){
        supplyTable.clear()
        supplyAmountList.clear()

        supplyTable.padLeft(10f)
        supplyTable.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("darkPixel"))

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

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
            nameLabel.setAlignment(Align.left)

            val amtLabel = Label("" + value.amt.toInt(), labelStyle)
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


    /**
     * Initially starts the event GUI
     */
    fun triggerEventGUI(event: GameEventManager.EventJson?){
        if(event!=null)
            GH.executeEventActions(event)

        if((event == null || !event.hasDescriptions) && Result.hasEventResults){
            showEventResults(Result.eventResultMap.values.toList(), Result.deathResultMap.values.toList(), {closeEvent()})
            return
        }else if(event == null || !event.hasDescriptions){
            closeEvent()
            return
        }

        game.pauseGame()
        EventInfo.eventTable.clear()
        campButton.isDisabled = true;
        EventInfo.eventContainer.remove()
        EventInfo.eventContainer.clear()
        EventInfo.eventContainer.setSize(300f, 350f)
        EventInfo.eventContainer.setPosition(TextGame.viewport.worldWidth/2f - EventInfo.eventContainer.width/2f, TextGame.viewport.worldHeight/2f - EventInfo.eventContainer.height/2f)
        EventInfo.eventContainer.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("eventBackground", Texture::class.java)))

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        EventInfo.titleLabel = Label(event.title, labelStyle)
        EventInfo.titleLabel!!.setAlignment(Align.center)
        EventInfo.titleLabel!!.setFontScale(eventTitleFontScale)
//        EventInfo.titleLabel!!.setWrap(true)

        EventInfo.eventTable.add(EventInfo.titleLabel).padTop(5f).height(35f)
        EventInfo.eventTable.row()
        EventInfo.eventTable.add(EventInfo.eventInnerTable).expand().fill()

        EventInfo.eventContainer.add(EventInfo.eventTable).expand().fill()
        TextGame.stage.addActor(EventInfo.eventContainer)

        showEventPage(event, 0)
    }

    /**
     * Shows an individual event page
     */
    private fun showEventPage(event: GameEventManager.EventJson, pageNumber:Int){
        //Clear the tables
        EventInfo.eventInnerTable.clear()
        EventInfo.eventChoicesTable.clear()

        val drawable = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("nextButtonWhite"))

        //Set some styles
        val scrollPaneStyle = ScrollPane.ScrollPaneStyle()
        scrollPaneStyle.vScrollKnob = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("scrollKnob"))
        scrollPaneStyle.vScroll = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("scrollBar"))

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        val nextPageButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        nextPageButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        nextPageButtonStyle.fontColor = Color.WHITE

        //Make the buttons for the choices (if any)
        for(choice in event.choices!!.iterator()){
            val button = TextButton("($choice)", textButtonStyle)
            button.pad(0f, 10f, 0f, 10f)
            button.label.setFontScale(buttonFontScale)
            EventInfo.eventChoicesTable.add(button).height(50f)
            EventInfo.eventChoicesTable.row()

            //Choose a choice buttons.
            button.addListener(object: ChangeListener(){
                override fun changed(evt: ChangeEvent?, actor: Actor?) {
                    //EventInfo.outerEventTable.remove()
                    val choiceText = button.text.toString().substring(1, button.text.length - 1)
                    triggerEventGUI(GH.getEventFromChoice(event, choiceText))
                }
            })
        }

        //Fix the description
        val desc = event.description[pageNumber].replace("%n", event.randomName)

        //Make the description label
        val descLabel = Label(desc, labelStyle)
        descLabel.setAlignment(Align.top)
        descLabel.setFontScale(normalFontScale)
        descLabel.setWrap(true)

        val cont = Container<Label>()
        cont.actor = descLabel

        //Put it into a scrollpane
        val scrollPane = ScrollPane(descLabel, scrollPaneStyle)
        scrollPane.setFadeScrollBars(false)

        //Make the next page button
        val nextPageButton: TextButton = TextButton("", nextPageButtonStyle)
        nextPageButton.label.setFontScale(0.15f)

        //Add the title and description label
        EventInfo.eventInnerTable.add(scrollPane).expand().fill().pad(10f, 5f, 0f, 10f)
        EventInfo.eventInnerTable.row().expandX().fillX()

        val hasNextPage = event.description.size - 1 > pageNumber || (event.hasChoices && event.choices!!.size > 1)
                event.hasOutcomes || event.hasActions

        //If we have another page, add a next page button.
        if(hasNextPage || event.hasChoices && event.choices!!.size == 1) {
            EventInfo.eventInnerTable.add(nextPageButton).size(50f).padBottom(5f).bottom()
            if(event.choices!!.size == 1)
                nextPageButton.label.setText(event.choices!![0])
            else
                nextPageButton.style.up = drawable
        }

        //Otherwise, add a close button.
        else{
            val closeButton: TextButton = TextButton("- Close -", textButtonStyle)
            closeButton.label.setFontScale(buttonFontScale)
            closeButton.addListener(object: ChangeListener(){
                override fun changed(evt: ChangeEvent?, actor: Actor?) {
                    closeEvent()
                }
            })

            EventInfo.eventInnerTable.add(closeButton).padBottom(5f).bottom().height(50f)
        }

        //Kinda complicated listener for the next page button.
        nextPageButton.addListener(object: ChangeListener(){
            override fun changed(evt: ChangeEvent?, actor: Actor?) {
                val hasOnlyOutcomes = (!event.hasChoices) && event.hasOutcomes

                //If we have another description, simply go to the next page.
                if(event.description.size - 1 > pageNumber)
                    showEventPage(event, pageNumber +1)

                //If we have choices, layout the choices.
                else if(event.hasChoices){
                    //If we have more than one choice
                    if(event.choices!!.size > 1) {
                        EventInfo.eventInnerTable.clear()
                        EventInfo.eventInnerTable.add(EventInfo.eventChoicesTable).expand().fill().padBottom(60f)

                    //If we only have one choice, trigger the event GUI again.
                    }else{
                        triggerEventGUI(GH.getEventFromChoice(event, ""))
                    }
                }

                //Otherwise, we only have outcomes or actions. Deal with it!
                else if (hasOnlyOutcomes || Result.hasEventResults){
                    triggerEventGUI(GH.getEventFromChoice(event, ""))
                }
            }
        })
    }

    /**
     * Shows the event results
     */
    fun showEventResults(list: List<Result>, deathList: List<Result>, onDoneCallback:()->Unit){
        EventInfo.eventResultsTable.clear()
        EventInfo.eventInnerTable.clear()

        /* Styles */
        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        //Close button
        val closeButton: TextButton = TextButton("- Close -", textButtonStyle)
        closeButton.label.setFontScale(buttonFontScale)

        //Display the results of the event.
        for(item in list){
            val nameLabel = Label(item.name + item.desc.toString(), labelStyle)
            var amtLabel: Label?
            amtLabel = Label("", labelStyle)
            if(item.amt != 0f) {
                amtLabel.setText(item.amt.toInt().toString())
                if (item.amt < 0) amtLabel.color = Color.RED
                else amtLabel.color = Color.GREEN
            }

            nameLabel.setFontScale(normalFontScale)
            amtLabel.setFontScale(normalFontScale)

            EventInfo.eventResultsTable.add(amtLabel).padRight(10f)
            EventInfo.eventResultsTable.add(nameLabel)
            EventInfo.eventResultsTable.row()
        }

        for(item in deathList){
            val nameLabel = Label(item.name + item.desc.toString(), labelStyle)
            var amtLabel: Label?
            amtLabel = Label("", labelStyle)
            if(item.amt != 0f) {
                amtLabel.setText(item.amt.toString())
                if (item.amt < 0) amtLabel.color = Color.RED
                else amtLabel.color = Color.GREEN
            }

            nameLabel.setFontScale(normalFontScale)
            amtLabel.setFontScale(normalFontScale)

            EventInfo.eventResultsTable.add(amtLabel).padRight(10f)
            EventInfo.eventResultsTable.add(nameLabel)
            EventInfo.eventResultsTable.row()
        }

        //Arrange it in the table.
        EventInfo.eventInnerTable.add(EventInfo.eventResultsTable).expand().fill()
        EventInfo.eventInnerTable.row()
        EventInfo.eventInnerTable.add(closeButton).bottom().height(50f)

        //Create a listener
        closeButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onDoneCallback()
            }
        })
    }

    /**
     * Closes the event window.
     */
    fun closeEvent(){
        campButton.isDisabled = false;
        EventInfo.eventContainer.remove()
        EventManager.callEvent("eventFinished")
        game.resumeGame()
    }

    fun buildCampTable(){
        campTable.clear()
        campTable.remove()
        campTable.setFillParent(true)

        val slider = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("sliderLight"))
        val knob = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("sliderKnobWhite"))

        val sliderStyle: Slider.SliderStyle = Slider.SliderStyle(slider, knob)
        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val buttonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.up = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("buttonBackground"))

        val campLabel = Label("Camp", labelStyle)
        campLabel.setFontScale((normalFontScale))
        campLabel.setAlignment(Align.center)

        val descLabel = Label("", labelStyle)
        descLabel.setFontScale((normalFontScale))
        descLabel.setAlignment(Align.center)
        descLabel.setWrap(true)

        activityHourLabel = Label("0", labelStyle)
        activityHourLabel.setFontScale(normalFontScale)

        val hourLabel = Label("Hours", labelStyle)
        hourLabel.setFontScale((normalFontScale))
        hourLabel.setAlignment(Align.center)

        activityHourSlider = Slider(0f, 24f, 1f, false, sliderStyle)

        activityButton = TextButton("Accept!", buttonStyle)
        activityButton.label.setFontScale(0.15f)

        val innerTable: Table = Table()
        innerTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("log2", Texture::class.java)))

        val func = {searchAct: DataManager.SearchActivityJSON ->
            descLabel.setText(searchAct.description)
        }

        innerTable.add(campLabel).fillX().padTop(10f).height(40f)
        innerTable.row().spaceTop(25f)
        innerTable.add(buildDropdownList(func)).width(300f).height(25f)
        innerTable.row().padTop(20f)
        innerTable.add(descLabel).width(300f)
        innerTable.row().padTop(20f)
        innerTable.add(hourLabel)
        innerTable.row()
        innerTable.add(activityHourLabel)
        innerTable.row()
        innerTable.add(activityHourSlider).width(150f).height(25f)
        innerTable.row()
        innerTable.add(activityButton).width(85f).height(37.5f).bottom().fill().expand().padBottom(20f)

        innerTable.top()
//        innerTable.debugAll()
        campTable.add(innerTable).top()
    }

    private fun buildDropdownList(func: (DataManager.SearchActivityJSON) -> Unit): Actor {
        val newFont = BitmapFont(Gdx.files.internal("fonts/spaceFont2.fnt"))
        newFont.data.setScale(normalFontScale)

        val labelStyle = Label.LabelStyle(newFont, Color.WHITE)
        labelStyle.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pixelBlack"))

        val scrollStyle: ScrollPane.ScrollPaneStyle = ScrollPane.ScrollPaneStyle()

        val darkPixel = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pixelBlack"))
        val listStyle: com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle = com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle()
        listStyle.font = newFont
        listStyle.fontColorSelected = Color.WHITE
        listStyle.fontColorUnselected = Color.WHITE
        listStyle.selection = darkPixel
        listStyle.background = darkPixel

        val selectBoxStyle: SelectBox.SelectBoxStyle = SelectBox.SelectBoxStyle()
        selectBoxStyle.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("dropdownBackground"))
        selectBoxStyle.listStyle = listStyle
        selectBoxStyle.scrollStyle = scrollStyle
        selectBoxStyle.font = newFont
        selectBoxStyle.fontColor = Color.WHITE

        selectBox = SelectBox(selectBoxStyle)

        val list:com.badlogic.gdx.utils.Array<Label> = com.badlogic.gdx.utils.Array()
        for(sa in DataManager.getSearchActiviesList()){
            val label = CustomLabel(sa.buttonTitle, labelStyle)
            label.setFontScale(normalFontScale)
            list.add(label)
        }

        selectBox.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                func(DataManager.SearchActivityJSON.getSearchActivity(selectBox.selected.text.toString())!!)
            }
        })

        selectBox.items = list
        selectBox.selected = list[0] //This simply triggers the above changelistener to call the function initially

        selectBox.setSelectedAlignment(Align.center)
        selectBox.list.setListAlignment(Align.center)
        return selectBox
    }

    fun buildTradeWindow(){
        tradeWindowTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("TradeWindow2", Texture::class.java)))
        tradeWindowTable.setSize(450f, 400f)

        val labelTable: Table = Table()
        val listTable: Table = Table()
        val offerTable: Table = Table()

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val amtLabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val giveButtonStyle = ImageButton.ImageButtonStyle()
        giveButtonStyle.imageUp = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("nextButtonWhite"))

        val takeButtonStyle = ImageButton.ImageButtonStyle()
        takeButtonStyle.imageUp = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("nextButtonWhiteLeft"))

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

        val spaceX = 4f

        listTable.add(valueLabel).width(60f).spaceRight(spaceX).height(24f)
        listTable.add(nameLabel).width(76f).spaceRight(spaceX)
        listTable.add(amountLabel).width(44f).spaceRight(spaceX)

        listTable.add(tradeTitleLabel).width(58f).spaceRight(spaceX).colspan(3)

        listTable.add(otherAmountLabel).width(44f).spaceRight(spaceX)
        listTable.add(otherNameLabel).width(76f).spaceRight(spaceX)
        listTable.add(otherValueLabel).width(60f)

        listTable.row().spaceTop(5f)

        //The item list.
        val exomerList = TradeManager.exomerList
        val otherList = TradeManager.otherList

        val rightArrow = TextGame.smallGuiAtlas.findRegion("nextButtonWhite")
        val leftArrow = TextGame.smallGuiAtlas.findRegion("nextButtonWhiteLeft")

        //For each item....
        for(i in exomerList!!.indices){
            val exItem = exomerList[i]
            val otherItem = otherList!![i]

            //Exomer item name
            val exomerItemNameLabel = Label(exItem.abbrName, labelStyle)
            exomerItemNameLabel.setFontScale(0.13f)
            exomerItemNameLabel.setAlignment(Align.center)

            //Exomer amount
            val exomerItemAmountLabel = Label(exItem.amt.toInt().toString(), labelStyle)
            exomerItemAmountLabel.setFontScale(0.13f)
            exomerItemAmountLabel.setAlignment(Align.center)
//            exomerItemAmountLabel.color = Color.SKY

            //Exomer worth
            val exomerItemValueLabel = Label(exItem.worth.toString(), labelStyle)
            exomerItemValueLabel.setFontScale(0.13f)
            exomerItemValueLabel.setAlignment(Align.center)
            exomerItemValueLabel.color = Color.GREEN

            //Other name
            val nativeItemNameLabel = Label(otherItem.abbrName, labelStyle)
            nativeItemNameLabel.setFontScale(0.13f)
            nativeItemNameLabel.setAlignment(Align.center)

            //Other amt
            val nativeItemAmountLabel = Label(otherItem.amt.toInt().toString(), labelStyle)
            nativeItemAmountLabel.setFontScale(0.13f)
            nativeItemAmountLabel.setAlignment(Align.center)
//            nativeItemAmountLabel.color = Color.ORANGE

            //Other worth
            val nativeItemValueLabel = Label(otherItem.worth.toString(), labelStyle)
            nativeItemValueLabel.setFontScale(0.13f)
            nativeItemValueLabel.setAlignment(Align.center)
            nativeItemValueLabel.color = Color.RED

            //Amt traded
            val amtLabel = Label("0", amtLabelStyle)
            amtLabel.setFontScale(0.13f)
            amtLabel.setAlignment(Align.center)

            //Add the amount then name to the left table.
            listTable.add(exomerItemValueLabel).left().fillX().spaceRight(spaceX).spaceBottom(spaceX).height(24f)
            listTable.add(exomerItemNameLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)
            listTable.add(exomerItemAmountLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)

            //Add the stuff to the center table.
            val leftArrowCell: Cell<Actor> = listTable.add().size(16f, 16f).spaceBottom(spaceX)
            listTable.add(amtLabel).space(0f, 0f, 0f, 0f).width(26f).spaceBottom(spaceX)
            val rightArrowCell: Cell<Actor> = listTable.add().size(16f, 16f).spaceRight(spaceX).spaceBottom(spaceX)

            //Add the name then amount to the right table.
            listTable.add(nativeItemAmountLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)
            listTable.add(nativeItemNameLabel).fillX().spaceRight(spaceX).spaceBottom(spaceX)
            listTable.add(nativeItemValueLabel).right().fillX().spaceBottom(spaceX)

            val rightArrowImage: Image = Image(rightArrow)
//            rightArrowImage.color = Color.ORANGE

            val leftArrowImage: Image = Image(leftArrow)
//            leftArrowImage.color = Color.SKY

            val func = { newAmt: Int ->
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
                    tradeAmt > 0 -> {amtLabel.color = Color.GREEN; leftArrowCell.setActor(leftArrowImage); rightArrowCell.clearActor()}
                    tradeAmt < 0 -> {amtLabel.color = Color.RED; rightArrowCell.setActor(rightArrowImage); leftArrowCell.clearActor()}
                    else -> {amtLabel.color = Color.WHITE; leftArrowCell.clearActor(); rightArrowCell.clearActor()}
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

            amtLabel.addListener(object: ClickListener(){
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    openTradeSlider(exomerList[i], otherList[i], func)
                    super.clicked(event, x, y)
                }
            })

            listTable.row()
        }

        acceptButton.addListener(object: ChangeListener(){
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
        tradeSliderWindow.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pixelBlack"))
        tradeSliderWindow.setSize(300f, 100f)
        tradeSliderWindow.setPosition(TextGame.viewport.worldWidth/2f - 300f/2f, TextGame.viewport.worldHeight/2f - 100f/2f)

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE
//        buttonStyle.up = TextureRegionDrawable(TextureRegion(TextGame.manager.get("", Texture::class.java)))

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val sliderStyle = Slider.SliderStyle()
        sliderStyle.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("sliderWhite"))
        sliderStyle.knob = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("sliderKnob"))

        val tradeSlider = Slider(-exItem.amt, oItem.amt, 1f, false, sliderStyle)
        tradeSlider.value = 0f

        val currLabel = Label("0", labelStyle)
        currLabel.setFontScale(0.15f)
        currLabel.setAlignment(Align.center)

        val minLabel = Label((-exItem.amt).toInt().toString(), labelStyle)
        minLabel.setFontScale(0.15f)
        minLabel.setAlignment(Align.center)

        val maxLabel = Label(oItem.amt.toInt().toString(), labelStyle)
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

        val cancelButton = TextButton("Cancel", buttonStyle)
        cancelButton.label.setFontScale(0.15f)
        cancelButton.label.setAlignment(Align.center)

        val buttonTable = Table()
        buttonTable.add(acceptButton).spaceRight(20f)
        buttonTable.add(cancelButton)

        tradeSliderWindow.add(lessButton)
        tradeSliderWindow.add(currLabel)
        tradeSliderWindow.add(moreButton)

        tradeSliderWindow.row()

        tradeSliderWindow.add(minLabel).spaceRight(5f)
        tradeSliderWindow.add(tradeSlider).spaceRight(5f)
        tradeSliderWindow.add(maxLabel)

        tradeSliderWindow.row()

        tradeSliderWindow.add(buttonTable).colspan(3).spaceBottom(10f)

//        tradeSlider.addListener(object:ClickListener(){
//            override fun clicked(event: InputEvent?, x: Float, y: Float) {
//                currLabel.setText(tradeSlider.value.toInt().toString())
//                callback(tradeSlider.value.toInt())
//                super.clicked(event, x, y)
//            }
//        })

        tradeSlider.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                currLabel.setText(tradeSlider.value.toInt().toString())
                callback(tradeSlider.value.toInt())
            }
        })

        moreButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                tradeSlider.value += 1
            }
        })

        lessButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                tradeSlider.value -= 1
            }
        })

        acceptButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                closeTradeSlider()
            }
        })

        cancelButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                callback(0)
                closeTradeSlider()
            }
        })

        TextGame.stage.addActor(tradeSliderWindow)
    }

    fun closeTradeSlider() = tradeSliderWindow.remove()

    fun buildRecentChangeTable(){
        val list = Result.recentResultMap.values.toList()
        recentChangeTable.clear()
        recentChangeTable.remove()
//        recentChangeTable.debugAll()

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        for(result in list){
            if(result.amt < 1 && result.amt > -1)
                continue

            val nameLabel = Label(result.name, labelStyle)
            nameLabel.setFontScale(0.15f)

            val amtLabel = Label(result.amt.toInt().toString(), labelStyle)
            amtLabel.setFontScale(0.15f)
            if(result.amt < 0) amtLabel.color = Color.RED
            if(result.amt > 0) amtLabel.color = Color.GREEN

            recentChangeTable.add(amtLabel).spaceRight(10f)
            recentChangeTable.add(nameLabel)
            recentChangeTable.row()
        }

        val deathList = Result.recentDeathResultMap.values.toList()
        for(result in deathList){
            val nameLabel = Label("${result.name} has died", labelStyle)
            nameLabel.setFontScale(0.15f)

            recentChangeTable.add(nameLabel).colspan(2)
            recentChangeTable.row()
        }

        recentChangeTable.setPosition(75f, TextGame.viewport.worldHeight/2f - recentChangeTable.height/2f)
        TextGame.stage.addActor(recentChangeTable)
    }

    fun openSettings(){
        game.pauseGame()
        settingsTable.clear()

        settingsTable.setSize(200f, 200f)
        settingsTable.setOrigin(Align.center)
        settingsTable.setPosition(TextGame.viewport.worldWidth/2f - 100f, TextGame.viewport.worldHeight/2f - 100f)
        settingsTable.background = TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("pixelBlack")))

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE

        val saveAndQuitButton = TextButton("Save and Quit", buttonStyle)
        saveAndQuitButton.label.setFontScale(0.15f)

        val saveAndExitButton = TextButton("Save and Exit", buttonStyle)
        saveAndExitButton.label.setFontScale(0.15f)

        val returnButton = TextButton("Return", buttonStyle)
        returnButton.label.setFontScale(0.15f)

        settingsTable.add(saveAndQuitButton).height(60f)
        settingsTable.row()
        settingsTable.add(saveAndExitButton).height(60f)
        settingsTable.row()
        settingsTable.add(returnButton).height(60f)

        saveAndQuitButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                SaveLoad.saveGame(false)
                TextGame.stage.clear()
                game.game.screen = MainMenuScreen(game.game)
            }
        })

        saveAndExitButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                SaveLoad.saveGame(false)
                TextGame.stage.clear()
                game.game.screen = MainMenuScreen(game.game)
                Gdx.app.exit()
            }
        })

        returnButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                closeSettings()
            }
        })

        TextGame.stage.addActor(settingsTable)
    }

    fun closeSettings(){
        settingsTable.remove()
        game.resumeGame()
    }


    private object EventInfo{
        val eventTable: Table = Table()
        val eventInnerTable: Table = Table()
        val eventChoicesTable: Table = Table()
        val eventContainer: Table = Table()
        val eventResultsTable: Table = Table()
        var titleLabel: Label? = null
    }

    private class CustomLabel(text: CharSequence?, style: LabelStyle?): Label(text, style) {
        override fun toString(): String {
            return this.text.toString()
        }
    }

    companion object{
        lateinit var instance: GameScreenGUI
    }
}