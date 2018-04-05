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
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.*
import com.quickbite.rx2020.managers.GameStats.game
import com.quickbite.rx2020.screens.GameScreen
import com.quickbite.rx2020.screens.MainMenuScreen
import com.quickbite.rx2020.util.SaveLoad

/**
 * Created by Paha on 2/5/2016.
 */
object GameScreenGUIOld{
    val defaultLabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)
    val titleFontScale = 0.25f
    val eventTitleFontScale = 0.18f
    val buttonFontScale = 0.15f
    
    lateinit var eventGUI:EventGUI

    private val mainTable: Table = Table()
    private var campTable: Table = Table() //For the camp screen
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

    var gameEventGUIActive = false
        get
        private set

    private val guiQueue:Queue<Triple<GameEventManager.EventJson, Int, Boolean>> = Queue()

    private lateinit var transparentBackground: Drawable

    private lateinit var gameScreen:GameScreen

    fun init(gameScreen: GameScreen){
        this.gameScreen = gameScreen
        CampMenuGUI.setupTable()

//        eventGUI = EventGUI()

        transparentBackground = NinePatchDrawable(NinePatch(TextGame.manager.get("GUIBackground", Texture::class.java), 4, 4, 4, 4))
        buildTravelScreenGUI()
        applyTravelTab()
        
    }

    fun update(delta:Float){

    }

    /**
     * An update that happens on every tick (which is every in game hour)
     */
    fun updateOnTimeTick(delta:Float){
        var time:String

        val t = GameStats.TimeInfo.timeOfDay
        time = "$t:00 "
        time += if(GameStats.TimeInfo.currTime >= 12) "PM"
        else "AM"

        timeLabel.setText(time)
        totalDaysLabel.setText("Day "+ GameStats.TimeInfo.totalDaysTraveled)

        distanceLabel.setText("" + GameStats.TravelInfo.totalDistToGo+" Miles")
        distProgressBar.value = GameStats.TravelInfo.totalDistTraveled.toFloat()

//        if(supplyTable.parent != null) SupplyGUI.update()
//        if(groupTable.parent != null) buildGroupTable()
//        if(ROVTable.parent != null) buildROVTable()

        GroupGUI.update(delta)
        ROVPartsGUI.update(delta)
        SupplyGUI.update(delta)
    }

    fun updateSuppliesGUI(){
        val list = SupplyManager.getSupplyList() //Get the supply list

        //For each item
        for(i in list.indices){
            supplyAmountList[i].setText( list[i].amt.toInt().toString()) //Set the text. Easy part!

            //Update the change list. This is for the recent changes to supplies that shows in red or green
            supplyChangeList[i].setText("")
            val supplyChanged = ResultManager.recentChangeMap[list[i].displayName] //Get the supply changed
            if(supplyChanged != null) {
                supplyChangeList[i].setText(supplyChanged.amt.toInt().toString()) //Change it to the text of the recent change amount
                when { //Change colors
                    supplyChanged.amt > 0 -> supplyChangeList[i].color = Color.GREEN
                    supplyChanged.amt < 0 -> supplyChangeList[i].color = Color.RED
                    else -> supplyChangeList[i].color = Color.WHITE
                }
            }
        }
    }

    private fun addListeners(){
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
                game.changeToCamp()
            }
        })

        settingsButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                openSettings()
            }
        })
    }

    fun applyTravelTab(){
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

        //campTable.bottom().left()
        campTable.setFillParent(true)

        campButton.isDisabled = false //In the rare case that an event opens immediately before the camp.
        campButton.remove()

        TextGame.stage.addActor(campTable)
    }

    fun closeCampMenu(){
        CampMenuGUI.closeTable()
        campTable.remove()
        addCampButton()
    }

    private fun addCampButton(){
        TextGame.stage.addActor(campButton)
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
        totalDaysLabel.setFontScale(GUIScale.Normal.fontScale)
        timeLabel.setFontScale(GUIScale.Normal.fontScale)
        distanceLabel.setFontScale(GUIScale.Normal.fontScale)

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
            nameLabel.setFontScale(GUIScale.Normal.fontScale)
            nameLabel.setAlignment(Align.center)

            val medkitButton = ImageButton(imageButtonStyle)
            medkitButton.isDisabled = !hasMedkits || (!person.hasInjury && person.healthNormal >= person.totalMaxHealth)

            val recentChangeLabel = Label("", labelStyle)
            recentChangeLabel.setFontScale(GUIScale.Normal.fontScale)
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
            groupTable.row().spaceTop(2f).right()

            medkitButton.addListener(object:ChangeListener(){
                override fun changed(p0: ChangeEvent?, p1: Actor?) {
                    if(person.hasInjury || person.healthNormal < person.totalMaxHealth){
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
        ROVNameLabel.setFontScale(GUIScale.Normal.fontScale)

        val list = ROVManager.ROVPartMap
        for(supply in list.values){
            val nameLabel = Label(supply.displayName, labelStyle)
            nameLabel.setFontScale(GUIScale.Normal.fontScale)

            var repairButton:ImageButton? = null
            if(supply.name != "ROV") {
                repairButton = ImageButton(medkit)
                repairButton.style.imageDisabled = medkitDis
                if (supply.amt <= 0 || supply.currHealth >= supply.maxHealth)
                    repairButton.isDisabled = true
            }

            val changeLabel: Label = Label("", labelStyle)
            changeLabel.setFontScale(GUIScale.Normal.fontScale)

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
            nameLabel.setFontScale(GUIScale.Normal.fontScale)
            nameLabel.setAlignment(Align.left)

            val amtLabel = Label("" + value.amt.toInt(), labelStyle)
            amtLabel.setFontScale(GUIScale.Normal.fontScale)
            amtLabel.setAlignment(Align.left)

            val changeLabel = Label("", labelStyle)
            changeLabel.setFontScale(GUIScale.Normal.fontScale)
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

    fun openEventGUI(event: GameEventManager.EventJson, startPage:Int = 0, eraseResultManagers:Boolean = true){
        disableButton(campButton)
        settingsButton.remove()
        if(eventGUI.beginEventGUI(event, startPage, eraseResultManagers)){
            gameEventGUIActive = true
        }
    }

    fun closeEventGUI(enableCampButton:Boolean, addSettingsButton:Boolean){
        if(enableCampButton) enableButton(campButton)
        if(addSettingsButton) TextGame.stage.addActor(settingsButton)
        gameEventGUIActive = false
    }

    fun tradeWindowOpen():Boolean = tradeWindowTable.parent != null

    fun buildCampTable(){
        campTable = CampMenuGUI.setupTable()
    }

    fun buildTradeWindow(){
        //TODO Maybe this should be in its own class?
        tradeWindowTable.clear()
        tradeWindowTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("TradeWindow2", Texture::class.java)))
        tradeWindowTable.setSize(450f, 400f)

        val labelTable = Table()
        val listTable = Table()
        val offerTable = Table()

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
                clear()
                game.game.screen = MainMenuScreen(game.game)
            }
        })

        saveAndExitButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                SaveLoad.saveGame(false, game)
                clear()
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

    internal fun enableButton(button:TextButton){
        button.label.color.a = 1f
        button.isDisabled = false
    }

    internal fun disableButton(button:TextButton){
        button.label.color.a = 0.5f
        button.isDisabled = true
    }

    fun clear(){
        TextGame.stage.clear()
        this.leftTable.clear()
        this.rightTable.clear()
        this.centerInfoTable.clear()
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

}