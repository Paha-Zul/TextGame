package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.getFloat
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.managers.GameEventManager
import com.quickbite.rx2020.managers.GameStats

object GameScreenGUIManager {
    val mainTable = Table()

    //The button tabs (if we decide to use them)
    private lateinit var supplyButton: TextButton
    private lateinit var groupButton: TextButton

    private lateinit var campButton: TextButton
    private lateinit var settingsButton: ImageButton

    var gameEventGUIActive = false

    val defaultLabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

    private val transparentBackground = NinePatchDrawable(NinePatch(TextGame.manager.get("GUIBackground", Texture::class.java), 4, 4, 4, 4))

    val tradeWindowOpen:Boolean
        get() = TradeWindowGUI.isOpen

    fun init(){
        val groupTable = GroupGUI.init()
        val partsTable = ROVPartsGUI.init()
        val supplyTable = SupplyGUI.init()

        val leftTable = Table()
        leftTable.add(supplyTable)

        val rightTable = Table()
        rightTable.add(groupTable)
        rightTable.row()
        rightTable.add().expandY().fillY()
        rightTable.row()
        rightTable.add(partsTable)

        val mainTable = Table()
        mainTable.add(leftTable)
        mainTable.add().expandX().fillX()
        mainTable.add(rightTable).expandY().fillY()

        mainTable.setFillParent(true)
        TextGame.stage.addActor(mainTable)

        setupButtons()
        addListeners()
    }

    private fun setupButtons(){
        settingsButton = ImageButton(TextureRegionDrawable(TextureRegion(TextGame.manager.get("gear", Texture::class.java))))
        settingsButton.setSize(35f, 35f)
        settingsButton.setPosition(5f, 5f)

//        GameScreenGUI.distProgressBar = ProgressBar(0f, GameStats.TravelInfo.totalDistOfGame.toFloat(), 20f, false, barStyle)

        campButton = TextButton("Camp", TextButton.TextButtonStyle(transparentBackground, transparentBackground, transparentBackground, TextGame.manager.get("spaceFont2", BitmapFont::class.java)))
        campButton.setSize(100f, 40f)
        campButton.setOrigin(Align.center)
        campButton.setPosition(TextGame.viewport.worldWidth/2f - campButton.width/2f, 0f)
//        campButton.label.setFontScale(DataManager.guiData.getTable("gameScreenGUI").getTable("tiny").getDouble("buttonFontScale").toFloat())
        campButton.label.setFontScale(DataManager.guiData.getFloat("gameScreenGUI", "tiny", "buttonFontScale"))

        TextGame.stage.addActor(campButton)
        TextGame.stage.addActor(settingsButton)
    }

    private fun addListeners(){
//        supplyButton.addListener(object: ClickListener(){
//            override fun clicked(event: InputEvent?, x: Float, y: Float) {
//                super.clicked(event, x, y)
//                if(supplyTable.parent == null) {
//                    leftTable.add(GameScreenGUI.supplyTable)
//                }else{
//                    //Clear the table and re-add the supply button. Don't forget the row!
//                    leftTable.clear()
//                    leftTable.add(GameScreenGUI.supplyButton).left().size(130f, 40f)
//                    leftTable.row()
//                    supplyTable.remove()
//                }
//            }
//        })

//        groupButton.addListener(object: ClickListener(){
//            override fun clicked(event: InputEvent?, x: Float, y: Float) {
//                super.clicked(event, x, y)
//
//                //If neither the ROVTable or groupTable are showing (null parent), add the group table
//                if(ROVTable.parent == null && GameScreenGUI.groupTable.parent == null) {
//                    rightTable.clear() //Clear the table
//                    addGroupButton() //Add the group button back in
//                    buildGroupTable() //Build the group table
//                    rightTable.add(GameScreenGUI.groupTable) //Add it to the table
//
//                    //If are not on the ROVTable, switch to the ROVTable
//                }else if(GameScreenGUI.ROVTable.parent == null){
//                    rightTable.clear() //Clear the table
//                    addGroupButton() //Add the group button back in
//                    buildROVTable() //Build the ROV table
//                    rightTable.add(GameScreenGUI.ROVTable) //Add the table
//
//                    //Otherwise if we are on the ROVTable (has a parent), remove both
//                }else{
//                    ROVTable.remove()
//                    groupTable.remove()
//                }
//            }
//        })

        campButton.addListener(object: ChangeListener(){
            //TODO change text when travel to camp and vice versa
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                GameStats.game.changeToCamp()
                openCampMenu()
            }
        })

        settingsButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                openSettings()
            }
        })
    }

    /**
     * Applies the travel screen GUI stuff, which is initially only the group stats
     * and supplies info.
     */
    fun openCampMenu(){
        val table = CampMenuGUI.setupTable()
        //campTable.bottom().left()
        table.setFillParent(true)

        campButton.isDisabled = false //In the rare case that an event opens immediately before the camp.
        campButton.remove()

        TextGame.stage.addActor(table)
    }

    /**
     * Opens the settings menu
     */
    fun openSettings() = TextGame.stage.addActor(SettingsGUI.buildSettingsTable())

    /**
     * Closes the settings menu and resumes the game
     */
    fun closeSettings(){
        SettingsGUI.close()
        GameStats.game.resumeGame()
    }

    fun openEventGUI(event: GameEventManager.EventJson, startPage:Int = 0, eraseResultManagers:Boolean = true){
        disableButton(campButton)
        settingsButton.remove()
        if(EventGUI.beginEventGUI(event, startPage, eraseResultManagers)){
            gameEventGUIActive = true
        }
    }

    fun closeEventGUI(enableCampButton:Boolean, addSettingsButton:Boolean){
        if(enableCampButton) enableButton(campButton)
        if(addSettingsButton) TextGame.stage.addActor(settingsButton)
        gameEventGUIActive = false
    }

    fun closeCampMenu(){
        CampMenuGUI.closeTable()
        addCampButton()
    }

    private fun addCampButton(){
        TextGame.stage.addActor(campButton)
    }

    /**
     * Opens the trade window. buildTradeWindow() needs to be called before.
     */
    fun openTradeWindow() {
        TextGame.stage.addActor(TradeWindowGUI.buildTradeWindow())
        GameStats.game.pauseGame()
    }

    /**
     * Closes the trade window.
     */
    fun closeTradeWindow() {
        TradeWindowGUI.closeTradeWindow()
        GameStats.game.resumeGame()
        //TODO For now took out resumeGame()
    }

    internal fun enableButton(button:TextButton){
        button.label.color.a = 1f
        button.isDisabled = false
    }

    internal fun disableButton(button:TextButton){
        button.label.color.a = 0.5f
        button.isDisabled = true
    }

    /**
     * Simply rebuilds the group table
     */
    fun rebuildGroupTable(){
        GroupGUI.buildGroupTable()
    }

    /**
     * Simply rebuilds the trade table
     */
    fun rebuildTradeWindow(){
        TradeWindowGUI.buildTradeWindow()
    }

    /**
     * Simply updates the supplies GUI
     */
    fun updateSuppliesGUI() = SupplyGUI.updateSuppliesGUI()


    fun clear(){
        TextGame.stage.clear()
        //TODO Maybe stuff is needed here?
//        this.leftTable.clear()
//        this.rightTable.clear()
//        this.centerInfoTable.clear()
    }

    fun update(delta:Float){
//        SupplyGUI.update(delta)

    }
}