package com.quickbite.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.quickbite.game.managers.DataManager
import com.quickbite.game.managers.EventManager
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
    private val campTable:Table = Table() //For the camp screen
    private val centerInfoTable:Table = Table()
    private val leftTable:Table = Table()
    private val rightTable:Table = Table()

    private lateinit var timeLabel:Label

    /* GUI elements for trade */
    private val mainTradeWindowTable:Table = Table()
    private val tradeWindowTable:Table = Table()
    private val leftTradeTable:Table = Table()
    private val rightTradeTable:Table = Table()
    private val middleTradeTable:Table = Table()


    /* GUI elements for travel info */
    private lateinit var distanceLabel:Label
    private lateinit var totalDaysLabel:Label

    /* GUI elements for people */
    private val groupTable:Table = Table() //For the group
    private val supplyTable:Table = Table() //For the supplies

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
                if(groupTable.parent == null) {
                    buildGroupTable()
                    rightTable.add(groupTable)
                }else{
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

        Game.stage.addActor(centerInfoTable)
        Game.stage.addActor(leftTable)
        Game.stage.addActor(rightTable)
        Game.stage.addActor(pauseButton)
        Game.stage.addActor(campButtonTab)
    }

    /**
     * Applies the travel screen GUI stuff, which is initially only the group stats
     * and supplies info.
     */
    fun applyCampTab(){
        Game.stage.clear()
        mainTable.clear()

        Game.stage.addActor(centerInfoTable)
        Game.stage.addActor(leftTable)
        Game.stage.addActor(rightTable)
        Game.stage.addActor(pauseButton)
        Game.stage.addActor(campButtonTab)

        //campTable.bottom().left()
        campTable.setFillParent(true)

        Game.stage.addActor(campTable)
    }

    fun buildTravelScreenGUI(){
        val barStyle:ProgressBar.ProgressBarStyle = ProgressBar.ProgressBarStyle()
        barStyle.background = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("art/bar.png"))))
        barStyle.knobBefore = TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("art/pixel.png"))))

        val textButtonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Game.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        val pauseButtonStyle = ImageButton.ImageButtonStyle()
        var drawable = TextureRegionDrawable(TextureRegion(Game.manager.get("play", Texture::class.java)))
        pauseButtonStyle.imageChecked = drawable
        pauseButtonStyle.imageCheckedOver = drawable

        drawable = TextureRegionDrawable(TextureRegion(Game.manager.get("pause", Texture::class.java)))
        pauseButtonStyle.imageUp =  drawable
        pauseButtonStyle.imageOver =  drawable
        pauseButtonStyle.imageDown =  drawable

        distProgressBar = ProgressBar(0f, GameStats.TravelInfo.totalDistOfGame.toFloat(), 20f, false, barStyle)

        pauseButton = ImageButton(pauseButtonStyle)
        pauseButton.setSize(40f, 40f)
        pauseButton.setPosition(Game.viewport.screenWidth/1.4f, Game.viewport.screenHeight - pauseButton.height)

        campButtonTab = TextButton("Camp", textButtonStyle)
        campButtonTab.setSize(40f, 40f)
        campButtonTab.setOrigin(Align.center)
        campButtonTab.setPosition(Game.viewport.screenWidth/4f, Game.viewport.screenHeight - campButtonTab.height)
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
        innerTable.background = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))

        val style:Label.LabelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

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

        val drawable = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))

        val buttonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Game.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE
        buttonStyle.up = drawable

        supplyButton = TextButton("Storage", buttonStyle)
        supplyButton.label.setFontScale(buttonFontScale)

        leftTable.add(supplyButton).left().size(130f, 40f)
        leftTable.row()

        leftTable.top().left()
        leftTable.setFillParent(true)

    }

    fun buildRightTable(){
        rightTable.clear()

        val drawable = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))

        val buttonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Game.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE
        buttonStyle.up = drawable

        groupButtonTab = TextButton("Exomer751", buttonStyle)
        groupButtonTab.label.setFontScale(buttonFontScale)

        rightTable.setFillParent(true)
        rightTable.top().right()

        rightTable.add(groupButtonTab).right().size(130f, 40f)
        rightTable.row()

    }

    /**
     * Builds the group table layout.
     */
    fun buildGroupTable(){
        groupTable.clear()

        groupTable.background = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))
        groupTable.padRight(10f)

        val labelStyle:Label.LabelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

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
        supplyTable.background = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))

        val labelStyle:Label.LabelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

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

        val labelStyle:Label.LabelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        EventInfo.titleLabel = Label(event.title, labelStyle)
        EventInfo.titleLabel!!.setAlignment(Align.center)
        EventInfo.titleLabel!!.setFontScale(eventTitleFontScale)
        EventInfo.titleLabel!!.setWrap(true)

        EventInfo.eventBackgroundTable.background = TextureRegionDrawable(TextureRegion(Game.manager.get("log2", Texture::class.java)))
        EventInfo.eventBackgroundTable.setSize(400f, 400f)

        EventInfo.outerEventTable.setFillParent(true)
        EventInfo.outerEventTable.add(EventInfo.eventBackgroundTable)
        Game.stage.addActor(EventInfo.outerEventTable)

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
        val labelStyle:Label.LabelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val imageButtonStyle:ImageButton.ImageButtonStyle = ImageButton.ImageButtonStyle()
        val drawable = TextureRegionDrawable(TextureRegion(Game.manager.get("nextButtonWhite", Texture::class.java)))

        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Game.manager.get("spaceFont2", BitmapFont::class.java)
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
        textButtonStyle.font = Game.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        val labelStyle:Label.LabelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)
        val redLabelStyle:Label.LabelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.RED)
        val greenLabelStyle:Label.LabelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.GREEN)

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

        val slider = TextureRegionDrawable(TextureRegion(Game.manager.get("slider", Texture::class.java)))
        val knob = TextureRegionDrawable(TextureRegion(Game.manager.get("sliderKnob", Texture::class.java)))

        val sliderStyle:Slider.SliderStyle = Slider.SliderStyle(slider, knob)
        val labelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val buttonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Game.manager.get("spaceFont2", BitmapFont::class.java)

        activityHourLabel = Label("0 hours", labelStyle)
        activityHourLabel.setFontScale(normalFontScale)

        activityHourSlider = Slider(0f, 24f, 1f, false, sliderStyle)

        activityButton = TextButton("Activity!", buttonStyle)
        activityButton.label.setFontScale(buttonFontScale)

        val innerTable:Table = Table()
        innerTable.background = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))

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
        labelStyle.background = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))

        val scrollStyle:ScrollPane.ScrollPaneStyle = ScrollPane.ScrollPaneStyle()

        val listStyle:com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle = com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle()
        listStyle.font = newFont
        listStyle.fontColorSelected = Color.WHITE
        listStyle.fontColorUnselected = Color.WHITE
        listStyle.selection = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))
        listStyle.background = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))

        val selectBoxStyle:SelectBox.SelectBoxStyle = SelectBox.SelectBoxStyle()
        selectBoxStyle.background = TextureRegionDrawable(TextureRegion(Game.manager.get("darkPixel", Texture::class.java)))
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
        Game.stage.addActor(campTable)
    }

    fun buildTradeWindow(){
        tradeWindowTable.background = TextureRegionDrawable(TextureRegion(Game.manager.get("TradeWindow", Texture::class.java)))
        tradeWindowTable.setSize(400f, 400f)

        val labelTable:Table = Table()
        val listTable:Table = Table()
        val offerTable:Table = Table()

        val labelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val giveButtonStyle = ImageButton.ImageButtonStyle()
        giveButtonStyle.imageUp = TextureRegionDrawable(TextureRegion(Game.manager.get("nextButtonWhite", Texture::class.java)))

        val takeButtonStyle = ImageButton.ImageButtonStyle()
        takeButtonStyle.imageUp = TextureRegionDrawable(TextureRegion(Game.manager.get("nextButtonWhiteLeft", Texture::class.java)))

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Game.manager.get("spaceFont2", BitmapFont::class.java)
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

        val otherOfferAmtLabel = Label("0", labelStyle)
        val otherOfferLabel = Label(":Their Offer", labelStyle)
        otherOfferAmtLabel.setFontScale(0.15f)
        otherOfferLabel.setFontScale(0.15f)

        TradeManager.generateLists()

        val exomerList = TradeManager.exomerList
        val otherList = TradeManager.otherList

        for(i in exomerList!!.indices){
            val exItem = exomerList[i]
            val otherItem = otherList!![i]

            val _leftTable:Table = Table()
            val _centerTable:Table = Table()
            val _rightTable:Table = Table()

            val exomerItemNameLabel = Label(exItem.displayName, labelStyle)
            exomerItemNameLabel.setFontScale(0.13f)
            exomerItemNameLabel.setAlignment(Align.left)

            val exomerItemAmountLabel = Label(exItem.amt.toInt().toString(), labelStyle)
            exomerItemAmountLabel.setFontScale(0.13f)
            exomerItemAmountLabel.setAlignment(Align.center)

            val nativeItemNameLabel = Label(otherItem.displayName, labelStyle)
            nativeItemNameLabel.setFontScale(0.13f)
            nativeItemNameLabel.setAlignment(Align.right)

            val nativeItemAmountLabel = Label(otherItem.amt.toInt().toString(), labelStyle)
            nativeItemAmountLabel.setFontScale(0.13f)
            nativeItemAmountLabel.setAlignment(Align.center)


            _leftTable.add(exomerItemNameLabel).left()
            _leftTable.add(exomerItemAmountLabel).left().padLeft(3f).size(25f)

            _rightTable.add(nativeItemAmountLabel).right().padRight((3f)).size(25f)
            _rightTable.add(nativeItemNameLabel).right()

            _leftTable.left()
            _rightTable.right()

            val takeButton = ImageButton(takeButtonStyle)
            val giveButton = ImageButton(giveButtonStyle)

            val amtLabel = Label("0", labelStyle)
            amtLabel.setFontScale(0.13f)
            amtLabel.setAlignment(Align.center)

            val func = {take:Boolean ->
                var amt = amtLabel.text.toString().toInt()
                var yourOffer = yourOfferAmtLabel.text.toString().toInt()
                var otherOffer = otherOfferAmtLabel.text.toString().toInt()

                //If we are taking an item (buying it)
                if(take && otherItem.amt > 0){
                    exItem.amt++
                    otherItem.amt--

                    when{
                        amt < 0 -> yourOffer -= exItem.worth
                        else -> otherOffer += otherItem.worth
                    }

                    amt++

                //If we are giving the item (selling it)
                }else if(!take && exItem.amt > 0){
                    exItem.amt--
                    otherItem.amt++

                    when{
                        amt <= 0 -> yourOffer += exItem.worth
                        else -> otherOffer -= otherItem.worth
                    }

                    amt--
                }

                exomerItemAmountLabel.setText(exItem.amt.toInt().toString())
                nativeItemAmountLabel.setText(otherItem.amt.toInt().toString())

                when{
                    amt > 0 -> amtLabel.color = Color.GREEN
                    amt < 0 -> amtLabel.color = Color.RED
                    else -> amtLabel.color = Color.WHITE
                }

                amtLabel.setText(amt.toString())
                yourOfferAmtLabel.setText(yourOffer.toString())
                otherOfferAmtLabel.setText(otherOffer.toString())
            }

            takeButton.addListener(object:ChangeListener(){
                override fun changed(p0: ChangeEvent?, p1: Actor?) {
                    func(true)
                }
            })

            giveButton.addListener(object:ChangeListener(){
                override fun changed(p0: ChangeEvent?, p1: Actor?) {
                    func(false)
                }
            })

            _centerTable.add(takeButton).size(24f).right()
            _centerTable.add(amtLabel).pad(0f, 5f, 0f, 5f).width(30f).center()
            _centerTable.add(giveButton).size(24f).left()

            listTable.add(_leftTable).left()
            listTable.add(_centerTable).fillX().expandX().center()
            listTable.add(_rightTable).right()
            listTable.row()
        }

//        tradeWindowLeft()
//        tradeWindowCenter()
//        tradeWindowRight()

        val acceptButton = TextButton("Accept", textButtonStyle)
        acceptButton.label.setFontScale(0.2f)

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

        offerTable.add(yourOfferLabel).left().padLeft(20f).padRight(5f)
        offerTable.add(yourOfferAmtLabel).left()
        offerTable.add().fillX().expandX()
        offerTable.add(otherOfferAmtLabel).right().padRight(5f)
        offerTable.add(otherOfferLabel).right().padRight(20f)

        labelTable.add(exomerLabel).fillX().expandX().left().height(30f).width(125f)
        labelTable.add().fillX().expandX()
        labelTable.add(nativeLabel).fillX().expandX().right().height(30f).width(125f)

        tradeWindowTable.add(labelTable).fillX().expandX().pad(20f, 20f, 0f, 20f)
        tradeWindowTable.row()
        tradeWindowTable.add(listTable).fill().expand().pad(10f, 20f, 0f, 20f).top()
        tradeWindowTable.row()
        tradeWindowTable.add(offerTable).fillX().expandX().padBottom(15f)
        tradeWindowTable.row()
        tradeWindowTable.add(acceptButton).padBottom(20f)

        mainTradeWindowTable.add(tradeWindowTable)

//        mainTradeWindowTable.debugAll()
        mainTradeWindowTable.setFillParent(true)
    }

    fun openTradeWindow(){
        Game.stage.addActor(mainTradeWindowTable)
    }

    fun closeTradeWindow(){
        mainTradeWindowTable.remove()
    }

    private fun tradeWindowLeft(){
        val labelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val exomerLabel = Label("Exomer751", labelStyle)
        exomerLabel.setFontScale(0.15f)
        exomerLabel.setAlignment(Align.center)

        val listTable = Table()
        val list = SupplyManager.getSupplyList()
        for((name, amt) in list){
            val t = Table()

            val nLabel = Label(name, labelStyle)
            nLabel.setFontScale(0.15f)
            nLabel.setAlignment(Align.left)
            val aLabel = Label(amt.toInt().toString(), labelStyle)
            aLabel.setFontScale(0.15f)

            t.add(nLabel).left()
            t.add(aLabel).left().padLeft(10f)
            listTable.add(t).left()
            listTable.row()
        }

        //Add the label and list to the left trade table.
        leftTradeTable.add(exomerLabel).width(120f).height(30f).left()
        leftTradeTable.row().padTop(15f)
        leftTradeTable.add(listTable)

        //Add the left trade table to the trade window table and position it!
        leftTradeTable.left().top()
        tradeWindowTable.add(leftTradeTable).left().top().padLeft(25f).padTop(20f).width(170f)
        tradeWindowTable.debugAll()
    }

    private fun tradeWindowRight(){
        val labelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val nativeLabel = Label("Native", labelStyle)
        nativeLabel.setFontScale(0.15f)
        nativeLabel.setAlignment(Align.center)

        val listTable = Table()
        val list = SupplyManager.getSupplyList()
        for((name, amt, max) in list){
            val t = Table()

            val nLabel = Label(name, labelStyle)
            nLabel.setFontScale(0.15f)
            nLabel.setAlignment(Align.right)
            val aLabel = Label(MathUtils.random(0, max).toString(), labelStyle)
            aLabel.setFontScale(0.15f)
            aLabel.setAlignment(Align.right)

            t.add(aLabel).right().padRight(10f)
            t.add(nLabel).right()
            listTable.add(t).right()
            listTable.row()
        }

        rightTradeTable.add(nativeLabel).width(120f).height(30f).right()
        rightTradeTable.row().padTop(15f)
        rightTradeTable.add(listTable)

        rightTradeTable.right().top()
        tradeWindowTable.add(rightTradeTable).right().top().padRight(25f).padTop(20f).width(170f)
        tradeWindowTable.debugAll()
    }

    private fun tradeWindowCenter(){
        val drawable:TextureRegionDrawable = TextureRegionDrawable(TextureRegion(Game.manager.get("nextButtonWhite", Texture::class.java)))

        val labelStyle = Label.LabelStyle(Game.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)
        val imageButtonStyle:ImageButton.ImageButtonStyle = ImageButton.ImageButtonStyle()
        imageButtonStyle.up = drawable
        imageButtonStyle.over = drawable
        imageButtonStyle.down = drawable

        val nativeLabel = Label("Native", labelStyle)
        nativeLabel.setFontScale(0.15f)
        nativeLabel.setAlignment(Align.center)

        val listTable = Table()
        val list = SupplyManager.getSupplyList()
        for((name, amt, max) in list){
            val t = Table()

            val leftArrows = ImageButton(imageButtonStyle)
            val rightArrows = ImageButton(imageButtonStyle)
            rightArrows.rotation = 180f

            t.add(leftArrows).size(16f)
            t.add(rightArrows).size(16f)
            listTable.add(t).fillX().expandX()
            listTable.row()
        }

        middleTradeTable.add().height(30f)
        middleTradeTable.row().padTop(15f)
        middleTradeTable.add(listTable).center().fillX().expandX()

        middleTradeTable.center()
        tradeWindowTable.add(middleTradeTable).right().top().padRight(25f).padTop(20f).fill().expand().center()
        tradeWindowTable.debugAll()
    }

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