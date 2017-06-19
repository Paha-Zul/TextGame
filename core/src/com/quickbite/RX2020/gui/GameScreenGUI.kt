package com.quickbite.rx2020.gui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.*
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Queue
import com.quickbite.rx2020.*
import com.quickbite.rx2020.managers.*
import com.quickbite.rx2020.screens.GameScreen
import com.quickbite.rx2020.screens.MainMenuScreen
import com.quickbite.rx2020.util.GH
import com.quickbite.rx2020.util.Logger
import com.quickbite.rx2020.util.SaveLoad

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
    private val supplyChangeList:MutableList<Label> = arrayListOf()

    /* Tab buttons */
    private lateinit var supplyButton: TextButton
    private lateinit var groupButton: TextButton
    private lateinit var campButton: TextButton


    private lateinit var distProgressBar: ProgressBar

    private var gameEventGUIActive = false
    private val guiQueue:Queue<Triple<GameEventManager.EventJson, Int, Boolean>> = Queue()


    private lateinit var transparentBackground: Drawable

    init{
        instance = this
        transparentBackground = NinePatchDrawable(NinePatch(TextGame.manager.get("GUIBackground", Texture::class.java), 4, 4, 4, 4))
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
        distProgressBar.value = GameStats.TravelInfo.totalDistTraveled.toFloat()

        if(supplyTable.parent != null) updateSuppliesGUI()
        if(groupTable.parent != null) buildGroupTable()
        if(ROVTable.parent != null) buildROVTable()
    }

    fun updateSuppliesGUI(){
        val list = SupplyManager.getSupplyList()
        for(i in list.indices){
            supplyAmountList[i].setText( list[i].amt.toInt().toString())

            //Update the change list
            supplyChangeList[i].setText("")
            val supplyChanged = ResultManager.recentChangeMap[list[i].displayName]
            if(supplyChanged != null) {
                supplyChangeList[i].setText(supplyChanged.amt.toInt().toString())
                if(supplyChanged.amt > 0)  supplyChangeList[i].color = Color.GREEN
                else if(supplyChanged.amt < 0)  supplyChangeList[i].color = Color.RED
                else  supplyChangeList[i].color = Color.WHITE
            }
        }
    }

    fun addListeners(){
        supplyButton.addListener(object: ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                if(supplyTable.parent == null) {
                    leftTable.add(supplyTable)
                }else{
                    //Clear the table and re-add the supply button. Don't forget the row!
                    leftTable.clear()
                    leftTable.add(supplyButton).left().size(130f, 40f)
                    leftTable.row()
                    supplyTable.remove()
                }
            }
        })

        groupButton.addListener(object: ClickListener(){
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)

                //If neither the ROVTable or groupTable are showing (null parent), add the group table
                if(ROVTable.parent == null && groupTable.parent == null) {
                    rightTable.clear() //Clear the table
                    addGroupButton() //Add the group button back in
                    buildGroupTable() //Build the group table
                    rightTable.add(groupTable) //Add it to the table

                //If are not on the ROVTable, switch to the ROVTable
                }else if(ROVTable.parent == null){
                    rightTable.clear() //Clear the table
                    addGroupButton() //Add the group button back in
                    buildROVTable() //Build the ROV table
                    rightTable.add(ROVTable) //Add the table

                //Otherwise if we are on the ROVTable (has a parent), remove both
                }else{
                    ROVTable.remove()
                    groupTable.remove()
                }
            }
        })

        campButton.addListener(object: ChangeListener(){
            //TODO change text when travel to camp and vice versa
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                if(game.state == GameScreen.State.TRAVELING) {
                    game.changeToCamp()
                    campButton.setText("Travel")
                }else if(game.state == GameScreen.State.CAMP) {
                    if(!GH.checkCantTravel()) {
                        game.changeToTravel()
                        campButton.setText("Camp")
                        applyTravelTab(groupTable)
                    }
                }
            }
        })

        settingsButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                openSettings()
            }
        })

        CampMenuInfo.activityHourSlider.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                CampMenuInfo.activityHourLabel.setText(CampMenuInfo.activityHourSlider.value.toInt().toString())
            }
        })

        CampMenuInfo.selectBox.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                game.searchActivity = DataManager.SearchActivityJSON.getSearchActivity(CampMenuInfo.selectBox.selected.text.toString())
                val ResultManager = GH.parseAndCheckRestrictions(game.searchActivity!!.restrictions!!)
                if(!ResultManager.first)
                    disableActivityButtonError(GH.getRestrictionFailReason(ResultManager.second, ResultManager.third))
                else
                    enableActivityButton()
            }
        })

        CampMenuInfo.activityButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if(CampMenuInfo.activityHourSlider.value <= 0f)
                    return

                game.numHoursToAdvance = CampMenuInfo.activityHourSlider.value.toInt()
                game.searchActivity = DataManager.SearchActivityJSON.getSearchActivity(CampMenuInfo.selectBox.selected.text.toString())
                //If not null, get the action.
                val actionList = game.searchActivity!!.action!! //Get the action list
                game.searchFunc = Array(actionList.size, {i->null}) //Initialize an array to hold the events.

                ChainTask.addTaskToHourlyList(sliderTask())
                disableActivityButtonActive()

                var i =0
                for(params in actionList.iterator()) {
                    //If not null, set up the search function
                    game.searchFunc!![i] = { EventManager.callEvent(params[0], params.slice(1.rangeTo(params.size - 1))) }
                    i++
                }
            }
        })
    }

    private fun sliderTask():ChainTask{
        var task:ChainTask? = null
        task = ChainTask(
                {
                    game.numHoursToAdvance >= 0 && game.searchActivity != null
                },
                {
                    val ResultManager = GH.parseAndCheckRestrictions(game.searchActivity!!.restrictions!!)

                    //If our list is 0, that means all the restrictions ResultManager. We are good to proceed.
                    if (ResultManager.first) {
                        CampMenuInfo.activityHourSlider.value = game.numHoursToAdvance.toFloat() - 1
                        game.searchFunc?.forEach { func -> func?.invoke() }

                    //Otherwise, stop such.
                    } else {
                        game.searchActivity = null
                        game.searchFunc = null
                        game.numHoursToAdvance = 0
                        disableActivityButtonError(GH.getRestrictionFailReason(ResultManager.second, ResultManager.third))
                        task!!.setDone()
                    }

                    //If the slider value made it to 0, set this task as done and enable the button if able.
                    if(CampMenuInfo.activityHourSlider.value.toInt() == 0) {
                        task!!.setDone()
                        if(ResultManager.first) //Enable the button as long as the restrictions were passed.
                            enableActivityButton()
                    }
                },
                { game.searchActivity = null; game.searchFunc = null; game.numHoursToAdvance = 0; task!!.setDone()}
        )

        return task
    }

    private fun disableActivityButtonError(reason:String){
        CampMenuInfo.activityButton.label.color = Color.RED
        CampMenuInfo.activityButton.isDisabled = true
        CampMenuInfo.denyReason.setText(reason)
    }

    private fun disableActivityButtonActive(){
        CampMenuInfo.activityButton.label.color.a = 0.5f
        CampMenuInfo.activityButton.isDisabled = true
    }

    private fun enableActivityButton(){
        CampMenuInfo.activityButton.label.color = Color.WHITE
        CampMenuInfo.activityButton.isDisabled = false
        CampMenuInfo.denyReason.setText("")
    }

    fun applyTravelTab(tableToApply: Table){
        mainTable.remove()
        mainTable.clear()
        campTable.remove()

        TextGame.stage.addActor(centerInfoTable)
        TextGame.stage.addActor(leftTable)
        TextGame.stage.addActor(rightTable)
        TextGame.stage.addActor(campButton)
        TextGame.stage.addActor(settingsButton)
    }

    /**
     * Applies the travel screen GUI stuff, which is initially only the group stats
     * and supplies info.
     */
    fun openCampMenu(){
        TextGame.stage.clear()
        mainTable.clear()

        TextGame.stage.addActor(centerInfoTable)
        TextGame.stage.addActor(leftTable)
        TextGame.stage.addActor(rightTable)
        TextGame.stage.addActor(campButton)

        //campTable.bottom().left()
        campTable.setFillParent(true)

        campButton.isDisabled = false //In the rare case that an event opens immediately before the camp.

        TextGame.stage.addActor(campTable)
    }

    fun buildTravelScreenGUI(){
        val barStyle: ProgressBar.ProgressBarStyle = ProgressBar.ProgressBarStyle()
        barStyle.background = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("bar"))
        barStyle.knobBefore = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pixel"))

        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE
        textButtonStyle.up = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("darkPixel"))

        val pauseButtonStyle = ImageButton.ImageButtonStyle()
        var drawable = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("play"))
        pauseButtonStyle.imageDisabled = drawable

        drawable = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pause"))
        pauseButtonStyle.imageUp =  drawable

        settingsButton = ImageButton(TextureRegionDrawable(TextureRegion(TextGame.manager.get("gear", Texture::class.java))))
        settingsButton.setSize(35f, 35f)
        settingsButton.setPosition(5f, 5f)

        distProgressBar = ProgressBar(0f, GameStats.TravelInfo.totalDistOfGame.toFloat(), 20f, false, barStyle)

        campButton = TextButton("Camp", TextButton.TextButtonStyle(transparentBackground, transparentBackground, transparentBackground, TextGame.manager.get("spaceFont2", BitmapFont::class.java)))
        campButton.setSize(100f, 40f)
        campButton.setOrigin(Align.center)
        campButton.setPosition(TextGame.viewport.worldWidth/2f - campButton.width/2f, 0f)
        campButton.label.setFontScale(buttonFontScale)

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
        innerTable.background = transparentBackground

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

        val drawable = transparentBackground

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

        val drawable = transparentBackground

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

        groupTable.background = transparentBackground
        groupTable.padRight(10f)

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val imageButtonStyle:ImageButton.ImageButtonStyle = ImageButton.ImageButtonStyle()
        imageButtonStyle.up = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("medkit"))
        imageButtonStyle.disabled = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("medkitDisabled"))

        val hasMedkits = SupplyManager.getSupply("medkits").amt.toInt() > 0
        val maleGenderSymbol = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("maleGender"))
        val femaleGenderSymbol = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("femaleGender"))

        val list:Array<Person> = GroupManager.getPeopleList()
        for(person: Person in list.iterator()){
            val pairTable = Table()

            val nameLabel = Label(person.fullName, labelStyle)
            nameLabel.setFontScale(normalFontScale)
            nameLabel.setAlignment(Align.center)

            val medkitButton = ImageButton(imageButtonStyle)
            medkitButton.isDisabled = !hasMedkits || (!person.hasInjury && person.healthNormal >= person.maxHealth)

            val recentChangeLabel = Label("", labelStyle)
            recentChangeLabel.setFontScale(normalFontScale)
            recentChangeLabel.setAlignment(Align.right)

            val genderImage:Image = if(person.male) Image(maleGenderSymbol) else Image(femaleGenderSymbol)

            val change = ResultManager.recentChangeMap[person.firstName]
            if(change != null){
                var modifier = ""
                if(change.amt > 0) {
                    recentChangeLabel.color = Color.GREEN
                    modifier = "+"
                }else if(change.amt < 0) {
                    recentChangeLabel.color = Color.RED
                }
                recentChangeLabel.setText("$modifier${change.amt.toInt().toString()}")
            }

            val healthBar: CustomHealthBar = CustomHealthBar(person, TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("bar"))),
                    TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("pixelWhite"))))

            val nameTable = Table()
            nameTable.add(genderImage).size(26f).right()
            nameTable.add(nameLabel).expandX().fillX().right()

            val healthTable = Table()
            healthTable.add(recentChangeLabel).spaceRight(5f).expandX().fillX().right()
            healthTable.add(medkitButton).size(20f).center().spaceRight(5f)
            healthTable.add(healthBar).fillX().width(100f).height(15f).right()

            pairTable.add(nameTable).expandX().fillX().right()
            pairTable.row()
            pairTable.add(healthTable).expandX().fillX().right()

            groupTable.add(pairTable).expandX().fillX().right()
            groupTable.row().spaceTop(5f).right()

            medkitButton.addListener(object:ChangeListener(){
                override fun changed(p0: ChangeEvent?, p1: Actor?) {
                    if(person.hasInjury || person.healthNormal < person.maxHealth){
                        EventManager.callEvent("removeAilment", person.firstName, "worst")
                        EventManager.callEvent("addHealth", person.firstName, "100")
                        EventManager.callEvent("addRndAmt", "-1", "-1", "medkits")
                        buildGroupTable()
                    }
                }
            })
        }
    }

    /**
     * Builds the group table layout.
     */
    fun buildROVTable(){
        ROVTable.clear()

        ROVTable.background = transparentBackground
        ROVTable.padRight(10f)

        val medkit = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("repair"))
        val medkitDis = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("repairDisabled"))

        val bg = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("bar"))
        val pixel = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pixelWhite"))

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)
        val healthLabelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2Small", BitmapFont::class.java), Color.FOREST)

        val ROVNameLabel = Label("ROV", labelStyle)
        ROVNameLabel.setFontScale(normalFontScale)

        val list = ROVManager.ROVPartMap
        for(supply in list.values){
            val nameLabel = Label(supply.displayName, labelStyle)
            nameLabel.setFontScale(normalFontScale)

            var repairButton:ImageButton? = null
            if(supply.name != "ROV") {
                repairButton = ImageButton(medkit)
                repairButton.style.imageDisabled = medkitDis
                if (supply.amt <= 0 || supply.currHealth >= supply.maxHealth)
                    repairButton.isDisabled = true
            }

            val changeLabel: Label = Label("", labelStyle)
            changeLabel.setFontScale(normalFontScale)

            val ResultManager = ResultManager.recentChangeMap["${supply.name} health"]
            if(ResultManager != null){
                var modifier = ""
                if(ResultManager.amt > 0) {
                    changeLabel.color = Color.GREEN
                    modifier = "+"
                }else if(ResultManager.amt < 0) {
                    changeLabel.color = Color.RED
                    modifier = "-"
                }
                changeLabel.setText("$modifier${ResultManager.amt.toInt().toString()}")
            }

            val healthBar: CustomHealthBar = CustomHealthBar(supply, bg, pixel)

            ROVTable.add(nameLabel).right().colspan(3)
            ROVTable.row()
            ROVTable.add(changeLabel).right()
            ROVTable.add(repairButton).space(0f, 10f, 0f, 10f).size(16f).right()
            ROVTable.add(healthBar).right().height(15f).width(100f)
            ROVTable.row().spaceTop(5f)

            if(repairButton != null) {
                repairButton.addListener(object : ChangeListener() {
                    override fun changed(p0: ChangeEvent?, p1: Actor?) {
                        SupplyManager.addHealthToSupply(supply, supply.maxHealth)
                        SupplyManager.addToSupply(supply.name, -1f)
                        buildROVTable()
                    }
                })
            }
        }
    }

    /**
     * Builds the supply table layout
     */
    fun buildSupplyTable(){
        supplyTable.clear()
        supplyAmountList.clear()
        supplyChangeList.clear()

        supplyTable.padLeft(10f)
        supplyTable.background = transparentBackground

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val title = Label("Supplies", labelStyle)
        title.setFontScale(titleFontScale)

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

            val changeLabel = Label("", labelStyle)
            changeLabel.setFontScale(normalFontScale)
            changeLabel.setAlignment(Align.center)

            val supply = ResultManager.recentChangeMap[list[i].displayName]
            if(supply!=null){
                changeLabel.setText(supply.amt.toInt().toString())
                if(supply.amt > 0) changeLabel.color = Color.GREEN
                else if(supply.amt < 0) changeLabel.color = Color.RED
            }

            supplyAmountList += amtLabel
            supplyChangeList += changeLabel

            innerTable.add(nameLabel).left().padRight(5f)
            innerTable.add(amtLabel).left().width(40f)
            innerTable.add(changeLabel).pad(0f, 5f, 0f, 5f).left().fillX().expandX()

//            innerTable.debugAll()

            innerTable.row()
        }

        supplyTable.add(innerTable)
    }

    /**
     * Initially starts the event GUI
     * @param event The EventJson object to start the event
     * @param startPage The page to start the event, or recursively call the triggerEventGUI function
     * @param eraseResultManagers True to erase the accumulated results of the event, false to keep them
     */
    fun triggerEventGUI(event: GameEventManager.EventJson, startPage:Int = 0, eraseResultManagers:Boolean = true){
        //If the GUI is already active, lets add it to the queue instead of going further.
        if(gameEventGUIActive) {
            guiQueue.addLast(Triple(event, startPage, eraseResultManagers))
            return
        }

        //We do this to ensure that the currActiveEvent is not null. Sometimes when overlapping events happen
        //the currActiveEvent can become null after being set.
        GameEventManager.currActiveEvent = event

        gameEventGUIActive = true
        ResultManager.clearResultLists()

        EventManager.callEvent("eventStarted", event.name)

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

        EventInfo.eventTable.add(EventInfo.titleLabel).padTop(5f).height(40f)
        EventInfo.eventTable.row()
        EventInfo.eventTable.add(EventInfo.eventInnerTable).expand().fill()

        EventInfo.eventContainer.add(EventInfo.eventTable).expand().fill()
        TextGame.stage.addActor(EventInfo.eventContainer)

        handleEvent(event, startPage)
    }

    /**
     * Where the end of an event gets handled. This is where we decide to show results, show outcomes, or continue onto another event piece
     * @param event The EventJson object to handle
     * @param startPage The starting page. This is useful for recursively handling multiple paged events
     */
    private fun handleEvent(event:GameEventManager.EventJson?, startPage:Int = 0){
        //Some debug info
        if(event != null)
            Logger.log("GameScreenGUI", "Handling event ${event.name} starting at page $startPage", Logger.LogLevel.Debug)

        //If the event has no description and no outcomes, we are on the action part. Execute the event actions!
        if(event != null && !event.hasDescriptions && !event.hasOutcomes)
            GH.executeEventActions(event)

        //If the event is null or has no description and we have event ResultManagers to show (stuff accumulated during the event), show the ResultManagers!
        if((event == null || !event.hasDescriptions) && ResultManager.hasEventResults){
            //Using some trickery for the recent death.
            showEventResultManagers(ResultManager.eventChangeMap.values.toList(), listOf(ResultManager.recentDeathResult), {closeEvent()})
            ResultManager.recentDeathResult = null
            return
        }else if(event == null || !event.hasDescriptions){
            closeEvent()
            return
        }

        showEventPage(event, startPage)
    }

    /**
     * Shows an individual event page
     */
    private fun showEventPage(event: GameEventManager.EventJson, pageNumber:Int){
        Logger.log("GameScreenGUI", "Handling page $pageNumber of event ${event.name}", Logger.LogLevel.Debug)

        //Clear the tables
        EventInfo.eventInnerTable.clear()
        EventInfo.eventChoicesTable.clear()

        //Let's check if we are within the page number boundaries
        //If we are over the description size....
        if(pageNumber >= event.description.size && event.hasChoices){
            EventInfo.eventInnerTable.add(EventInfo.eventChoicesTable).expand().fill()

            //Make a button for each choice.
            for(i in 0.rangeTo(event.choices!!.size-1)){
                val choice = event.choices!![i]

                //Need a style specifically for each button since we may be changing colors.
                val buttonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
                buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
                buttonStyle.fontColor = Color.WHITE

                val modifiedText = GH.replaceChoiceForEvent(choice, event)
                val button = TextButton("($modifiedText)", buttonStyle)
                button.pad(0f, 10f, 0f, 10f)
                button.label.setFontScale(buttonFontScale)
                button.label.setWrap(true)

                EventInfo.eventChoicesTable.add(button).minHeight(40f).expandX().fillX()
                EventInfo.eventChoicesTable.row()

                //If we don't pass the restrictions, disable this button
                if(event.hasRestrictions && !GH.parseAndCheckRestrictions(event.restrictions!![i]).first){
                    button.isDisabled = true
                    button.style.fontColor = Color.GRAY
                }

                //Choose a choice buttons.
                button.addListener(object: ChangeListener(){
                    override fun changed(evt: ChangeEvent?, actor: Actor?) {
                        //EventInfo.outerEventTable.remove()
                        handleEvent(GH.getEventFromChoice(event, choice))
                    }
                })
            }

        //Otherwise, we are not showing possible choices. Let's show the descriptions!
        }else {
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

            //Make the description label
            val descLabel = Label(event.modifiedDescription[pageNumber], labelStyle)
            descLabel.setAlignment(Align.center)
            descLabel.setFontScale(normalFontScale)
            descLabel.setWrap(true)

            //This is to add some extra padding to the label. Without this, the text gets cut off a bit
            //by the scrollpane.
            val container = Container<Label>(descLabel)
            container.pad(5f, 5f, 5f, 5f)
            container.fillX().fillY()

            //Put it into a scrollpane
            val scrollPane = ScrollPane(container, scrollPaneStyle)
            scrollPane.setFadeScrollBars(false)

            //Make the next page button
            val nextPageButton: TextButton = TextButton("", nextPageButtonStyle)
            nextPageButton.label.setFontScale(0.15f)

            //Add the title and description label
            EventInfo.eventInnerTable.add(scrollPane).expand().fill().pad(10f, 10f, 0f, 10f).center()
            EventInfo.eventInnerTable.row()

            val hasAnotherPage = event.description.size - 1 > pageNumber
            val hasAnotherSomething = event.description.size - 1 > pageNumber || (event.hasChoices && event.choices!!.size > 1)
                    || event.hasOutcomes || event.hasActions || ResultManager.hasEventResults
            var toNext = hasAnotherSomething || event.hasChoices && event.choices!!.size == 1

            val setNextPageButton = {
                nextPageButton.isDisabled = false
                nextPageButton.setText("")

                //If we have another page, add a next page button.
                if (toNext) {
                    if (event.choices!!.size == 1 && !hasAnotherPage)
                        nextPageButton.label.setText(GH.replaceChoiceForEvent(event.choices!![0], event))
                    else
                        nextPageButton.style.up = drawable

                    //Otherwise, add a close button.
                } else {
                    nextPageButton.setText("- Close -")
                }
            }

            setNextPageButton()
            EventInfo.eventInnerTable.add(nextPageButton).size(40f).padBottom(5f).bottom()

            //Kinda complicated listener for the next page button.
            nextPageButton.addListener(object : ChangeListener() {
                override fun changed(evt: ChangeEvent?, actor: Actor?) {
                    val hasOnlyOutcomes = (!event.hasChoices) && event.hasOutcomes

                    //If we have another description, simply go to the next page.
                    if (event.description.size - 1 > pageNumber)
                        showEventPage(event, pageNumber + 1)

                    //If we have choices, layout the choices.
                    else if (event.hasChoices) {
                        //If we have more than one choice
                        if (event.choices!!.size > 1) {
                            showEventPage(event, pageNumber + 1)

                        //If we only have one choice, trigger the event GUI again.
                        } else {
                            GH.executeEventActions(event)   //TODO Watch this. Might fire twice if I'm wrong.
                            handleEvent(GH.getEventFromChoice(event, ""))
                        }
                    }

                    //Otherwise, we only have outcomes or actions. Deal with it!
                    else if (hasOnlyOutcomes || ResultManager.hasEventResults) {
                        GH.executeEventActions(event)
                        handleEvent(GH.getEventFromChoice(event, ""))

                    //Otherwise, end the event.
                    } else {
                        GH.executeEventActions(event)
                        handleEvent(null) //End the event.
                    }
                }
            })

            //Invalidate all this crap so that the scroll panel show correctly, otherwise it'll say 'scroll down' when
            //it shouldn't
            EventInfo.eventTable.invalidateHierarchy()
            EventInfo.eventTable.act(0.016f)
            EventInfo.eventTable.validate()

            EventInfo.eventContainer.invalidateHierarchy()
            EventInfo.eventContainer.act(0.016f)
            EventInfo.eventContainer.validate()

            EventInfo.eventInnerTable.invalidateHierarchy()
            EventInfo.eventInnerTable.act(0.016f)
            EventInfo.eventInnerTable.validate()

            scrollPane.invalidateHierarchy()
            scrollPane.act(0.016f)
            scrollPane.validate()

            TextGame.stage.act(0.016f)

            //If we are not at the bottom, say scroll down!
            if (!scrollPane.isBottomEdge) {
                nextPageButton.isDisabled = true
                nextPageButton.style.up = null
                nextPageButton.setText("Scroll Down")
            }

            //If we are at the bottom, allow the button to go to the next page.
            scrollPane.addListener {
                if (scrollPane.isBottomEdge)
                    setNextPageButton()
                false
            }
        }
    }

    /**
     * Shows the event ResultManagers
     * @param list The list of ResultManagers to show
     * @param deathList The list of recent deaths (during the event) to show
     * @param onDoneCallback The function to call when finished
     */
    fun showEventResultManagers(list: List<Result>, deathList: List<Result?>, onDoneCallback:()->Unit){
        EventInfo.eventInnerTable.clear()

        /* Styles */
        val textButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        //Set some styles
        val scrollPaneStyle = ScrollPane.ScrollPaneStyle()
        scrollPaneStyle.vScrollKnob = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("scrollKnob"))
        scrollPaneStyle.vScroll = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("scrollBar"))

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        //Close button
        val closeButton: TextButton = TextButton("- Close -", textButtonStyle)
        closeButton.label.setFontScale(buttonFontScale)

        val ResultManagersTable = Table()

        //Display the ResultManagers of the event.
        for (item in list) {
            val nameLabel = Label(item.name + item.desc, labelStyle)
            var amtLabel: Label?
            amtLabel = Label("", labelStyle)
            if (item.amt != 0f) {
                if (item.amt < 0) {
                    amtLabel.setText("${item.amt.toInt()}")
                    amtLabel.color = Color.RED
                } else {
                    amtLabel.setText("+${item.amt.toInt()}")
                    amtLabel.color = Color.GREEN
                }
            }

            nameLabel.setFontScale(normalFontScale)
            amtLabel.setFontScale(normalFontScale)

            ResultManagersTable.add(amtLabel).padRight(10f)
            ResultManagersTable.add(nameLabel)
            ResultManagersTable.row()
        }

        for (death in deathList) {
            if(death == null) continue

            val label = Label(death.name + death.desc.toString(), labelStyle)
            label.setFontScale(normalFontScale)
            label.setWrap(true)
            label.setAlignment(Align.center)

            ResultManagersTable.add(label).expand().fill()
            ResultManagersTable.row()
        }

        val container = Container<Table>(ResultManagersTable).fill().pad(10f, 10f, 0f, 10f)
        container.padTop(10f)

        //Put it into a scrollpane
        val scrollPane = ScrollPane(container, scrollPaneStyle)
        scrollPane.setFadeScrollBars(false)

        //Arrange it in the table.
        EventInfo.eventInnerTable.add(scrollPane).expand().fill()
        EventInfo.eventInnerTable.row()
        EventInfo.eventInnerTable.add(closeButton).bottom().height(50f)

        //Create a listener
        closeButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onDoneCallback()
            }
        })

        EventInfo.eventInnerTable.validate()
        EventInfo.eventInnerTable.act(0.016f)

        scrollPane.validate()
        scrollPane.act(0.016f)

        if(!scrollPane.isBottomEdge) {
            closeButton.isDisabled = true
            closeButton.style.up = null
            closeButton.setText("Scroll Down")
        }

        scrollPane.addListener {
            if(scrollPane.isBottomEdge) {
                closeButton.isDisabled = false
                closeButton.setText("- Close -")
            }
            false
        }
    }

    /**
     * Closes the event window.
     */
    fun closeEvent(){
        if(GameEventManager.currActiveEvent != null) {
            campButton.isDisabled = false;
            gameEventGUIActive = false
            EventManager.callEvent("eventFinished", GameEventManager.currActiveEvent!!.name)
            GameEventManager.lastCurrEvent = GameEventManager.currActiveEvent
            GameEventManager.currActiveEvent = null
            EventInfo.eventContainer.remove()

            //If the queue is not empty, lets call the event gui again
            if (guiQueue.size != 0) {
                val last = guiQueue.removeLast()
                triggerEventGUI(last.first, last.second, last.third)
            } else if (tradeWindowTable.parent == null)
                game.resumeGame()
        }
    }

    fun buildCampTable(){
        campTable.clear()
        campTable.remove()
        campTable.setFillParent(true)

        val descTable = Table()
        val hourGroup = HorizontalGroup()

        descTable.top()

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

        CampMenuInfo.denyReason = Label("", labelStyle)
        CampMenuInfo.denyReason.setFontScale(0.1f)
        CampMenuInfo.denyReason.color = Color.RED
        CampMenuInfo.denyReason.setAlignment(Align.bottom)

        CampMenuInfo.activityHourLabel = Label("0", labelStyle)
        CampMenuInfo.activityHourLabel.setFontScale(normalFontScale)

        val hourLabel = Label("Hours", labelStyle)
        hourLabel.setFontScale((normalFontScale))
        hourLabel.setAlignment(Align.center)

        CampMenuInfo.activityHourSlider = Slider(0f, 24f, 1f, false, sliderStyle)

        CampMenuInfo.activityButton = TextButton("Accept!", buttonStyle)
        CampMenuInfo.activityButton.label.setFontScale(0.15f)

        val innerTable: Table = Table()
        innerTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("log2", Texture::class.java)))

        val func = {searchAct: DataManager.SearchActivityJSON ->
            descTable.clear()
            val titleLabel = Label("Per Hour", labelStyle)
            titleLabel.setFontScale(0.2f)
            titleLabel.setAlignment(Align.center)

            val whitePixel = TextGame.smallGuiAtlas.findRegion("pixelWhite")

            descTable.add(titleLabel).colspan(3).spaceBottom(10f)
            descTable.row()

            val descList = searchAct.description
            descList.forEachIndexed { i, params ->
                if(params.size >= 3) {

                    val nameLabel = Label(params[0], labelStyle)
                    nameLabel.setFontScale((0.15f))
                    nameLabel.setAlignment(Align.center)

                    val chanceLabel = Label(params[1], labelStyle)
                    chanceLabel.setFontScale((0.15f))
                    chanceLabel.setAlignment(Align.center)

                    val amountLabel = Label(params[2], labelStyle)
                    amountLabel.setFontScale((0.15f))
                    amountLabel.setAlignment(Align.center)

                    val divider = Image(whitePixel)
                    divider.color = Color.GRAY

                    descTable.add(nameLabel).width(100f)
                    descTable.add(chanceLabel).fillX().width(100f)
                    descTable.add(amountLabel).fillX().width(100f)
                    descTable.row()
                    if(i < descList.size-1) {
                        descTable.add(divider).fillX().colspan(3).pad(5f, 0f, 5f, 0f)
                        descTable.row()
                    }
                }else{
                    val label = Label(params[0], labelStyle)
                    label.setFontScale((normalFontScale))
                    label.setAlignment(Align.center)

                    descTable.add(label).colspan(3)
                    descTable.row()
                }


            }

        }

        hourGroup.addActor(CampMenuInfo.activityHourLabel)
        hourGroup.addActor(hourLabel)
        hourGroup.space(10f)

        innerTable.add(campLabel).fillX().padTop(10f).height(40f)
        innerTable.row().spaceTop(25f)
        innerTable.add(buildDropdownList(func)).width(300f).height(25f)
        innerTable.row().padTop(10f)
        innerTable.add(descTable).width(300f).fillY().expandY().top()
        innerTable.row().padTop(20f)
        innerTable.add(hourGroup)
        innerTable.row()
        innerTable.add(CampMenuInfo.activityHourSlider).width(150f).height(25f)
        innerTable.row()
        innerTable.add(CampMenuInfo.denyReason).bottom().fillX().expandX().padBottom(5f)
        innerTable.row()
        innerTable.add(CampMenuInfo.activityButton).width(85f).height(37.5f).bottom().padBottom(10f)

        innerTable.top()
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

        CampMenuInfo.selectBox = SelectBox(selectBoxStyle)

        val list:com.badlogic.gdx.utils.Array<Label> = com.badlogic.gdx.utils.Array()
        for(sa in DataManager.getSearchActiviesList()){
            val label = CustomLabel(sa.buttonTitle, labelStyle)
            label.setFontScale(normalFontScale)
            list.add(label)
        }

        CampMenuInfo.selectBox.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                func(DataManager.SearchActivityJSON.getSearchActivity(CampMenuInfo.selectBox.selected.text.toString())!!)
            }
        })

        CampMenuInfo.selectBox.items = list
        CampMenuInfo.selectBox.selected = list[0] //This simply triggers the above changelistener to call the function initially

        CampMenuInfo.selectBox.setSelectedAlignment(Align.center)
        CampMenuInfo.selectBox.list.setListAlignment(Align.center)
        return CampMenuInfo.selectBox
    }

    fun buildTradeWindow(){
        tradeWindowTable.clear()
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
                exomerItemAmountLabel.setText(exItem.amt.toInt().toString())
                nativeItemAmountLabel.setText(otherItem.amt.toInt().toString())

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
                        SupplyManager.setSupplyAmount(item.name, item.currAmt.toFloat())

                    //TODO Supplies don't update?
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
    fun openTradeWindow() {
        TextGame.stage.addActor(tradeWindowTable)
        game.pauseGame()
    }

    /**
     * Closes the trade window.
     */
    fun closeTradeWindow() {
        tradeWindowTable.remove()
        game.resumeGame()
        //TODO For now took out resumeGame()
    }

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

        tradeSlider.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                currLabel.setText(tradeSlider.value.toInt().toString())
                callback(tradeSlider.value.toInt())
                exItem.currAmt = exItem.amt + tradeSlider.value.toInt()
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
                SaveLoad.saveGame(false, game)
                TextGame.stage.clear()
                game.game.screen = MainMenuScreen(game.game)
            }
        })

        saveAndExitButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                SaveLoad.saveGame(false, game)
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
        var titleLabel: Label? = null
    }

    private object CampMenuInfo{
        /* Camp specific stuff */
        lateinit var activityHourLabel: Label
        lateinit var activityHourSlider: Slider
        lateinit var activityButton: TextButton
        lateinit var selectBox: SelectBox<Label>
        lateinit var denyReason: Label
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