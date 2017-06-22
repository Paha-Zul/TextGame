package com.quickbite.rx2020.gui

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
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.managers.EventManager
import com.quickbite.rx2020.managers.GameStats
import com.quickbite.rx2020.util.GH

/**
 * Created by Paha on 6/20/2017.
 *
 * The camp menu GUI
 */
class CampMenuGUI {
    lateinit var activityHourLabel: Label
    lateinit var activityHourSlider: Slider
    lateinit var acceptButton: TextButton
    lateinit var uncampButton: TextButton
    lateinit var selectBox: SelectBox<Label>
    lateinit var denyReason: Label
    val descriptionTable:Table = Table()

    val mainTable:Table = Table()

    init{

    }

    fun setupTable():Table{
        mainTable.clear()
        mainTable.setFillParent(true)

        val hourGroup = HorizontalGroup()

        val slider = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("sliderLight"))
        val knob = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("sliderKnobWhite"))

        val sliderStyle: Slider.SliderStyle = Slider.SliderStyle(slider, knob)
        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val buttonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.up = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("buttonBackground"))

        val campLabel = Label("Camp", labelStyle)
        campLabel.setFontScale((GameScreenGUI.normalFontScale))
        campLabel.setAlignment(Align.center)

        denyReason = Label("", labelStyle)
        denyReason.setFontScale(0.1f)
        denyReason.color = Color.RED
        denyReason.setAlignment(Align.bottom)

        activityHourLabel = Label("0", labelStyle)
        activityHourLabel.setFontScale(GameScreenGUI.normalFontScale)

        val hourLabel = Label("Hours", labelStyle)
        hourLabel.setFontScale((GameScreenGUI.normalFontScale))
        hourLabel.setAlignment(Align.center)

        activityHourSlider = Slider(0f, 24f, 1f, false, sliderStyle)

        acceptButton = TextButton("Accept!", buttonStyle)
        acceptButton.label.setFontScale(0.15f)

        uncampButton = TextButton("Uncamp", buttonStyle)
        uncampButton.label.setFontScale(0.15f)

        val innerTable: Table = Table()
        innerTable.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("log2", Texture::class.java)))

        hourGroup.addActor(activityHourLabel)
        hourGroup.addActor(hourLabel)
        hourGroup.space(10f)

        val buttonTable = Table()
        buttonTable.add(acceptButton).width(85f).height(37.5f).spaceRight(20f)
        buttonTable.add(uncampButton).width(85f).height(37.5f)

        innerTable.add(campLabel).fillX().padTop(10f).height(40f)
        innerTable.row().spaceTop(25f)
        innerTable.add(buildDropdownList()).width(300f).height(25f)
        innerTable.row().padTop(10f)
        innerTable.add(descriptionTable).width(300f).fillY().expandY().top()
        innerTable.row().padTop(20f)
        innerTable.add(hourGroup)
        innerTable.row()
        innerTable.add(activityHourSlider).width(150f).height(25f)
        innerTable.row()
        innerTable.add(denyReason).bottom().fillX().expandX().padBottom(5f)
        innerTable.row()
        innerTable.add(buttonTable).bottom().padBottom(10f)

        innerTable.top()
        mainTable.add(innerTable).top()

        setupListeners()

        //When we first set up the table, disable the accept button since we are on 0 hours initially
        disableButton(acceptButton)

        return mainTable
    }

    private fun setupListeners(){
        acceptButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if(GameScreenGUI.campMenu.activityHourSlider.value <= 0f)
                    return

                GameStats.game.numHoursToAdvance = GameScreenGUI.campMenu.activityHourSlider.value.toInt()
                GameStats.game.searchActivity = DataManager.SearchActivityJSON.getSearchActivity(GameScreenGUI.campMenu.selectBox.selected.text.toString())
                //If not null, get the action.
                val actionList = GameStats.game.searchActivity!!.action!! //Get the action list
                GameStats.game.searchFunc = Array(actionList.size, { i->null}) //Initialize an array to hold the events.

                ChainTask.addTaskToHourlyList(sliderTask())
                disableButton(acceptButton)

                var i =0
                for(params in actionList.iterator()) {
                    //If not null, set up the search function
                    GameStats.game.searchFunc!![i] = { EventManager.callEvent(params[0], params.slice(1.rangeTo(params.size - 1))) }
                    i++
                }
            }
        })

        uncampButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                if(!GH.checkCantTravel()) {
                    GameStats.game.changeToTravel()
                    GameScreenGUI.applyTravelTab()
                }
                GameScreenGUI.closeCampMenu()
            }
        })

        activityHourSlider.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                activityHourLabel.setText(activityHourSlider.value.toInt().toString())
                //If our hour is at or below 0 -OR- our search activity is not null, disable the accept button
                if(activityHourSlider.value.toInt() <= 0 || GameStats.game.numHoursToAdvance > 0)
                    disableButton(acceptButton)

                //Otherwise, enable it
                else
                    enableButton(acceptButton)
            }
        })

        selectBox.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                GameStats.game.searchActivity = DataManager.SearchActivityJSON.getSearchActivity(GameScreenGUI.campMenu.selectBox.selected.text.toString())
                val ResultManager = GH.parseAndCheckRestrictions(GameStats.game.searchActivity!!.restrictions!!)
                if(!ResultManager.first)
                    disableAcceptButtonError(GH.getRestrictionFailReason(ResultManager.second, ResultManager.third))
                else
                    enableAcceptButton()
            }
        })
    }

    private fun enableButton(button:TextButton){
        button.label.color.a = 1f
        button.isDisabled = false
    }

    private fun disableButton(button:TextButton){
        button.label.color.a = 0.5f
        button.isDisabled = true
    }

    private fun enableAcceptButton(){
        acceptButton.label.color = Color.WHITE
        acceptButton.isDisabled = false
        denyReason.setText("")
    }

    private fun disableAcceptButtonError(reason:String){
        acceptButton.label.color = Color.RED
        acceptButton.isDisabled = true
        denyReason.setText(reason)
    }

    private fun setupDescriptionTable(searchAct: DataManager.SearchActivityJSON){
        descriptionTable.clear()

        val titleLabel = Label("Per Hour", GameScreenGUI.defaultLabelStyle)
        titleLabel.setFontScale(0.2f)
        titleLabel.setAlignment(Align.center)

        //White pixel for a divider
        val whitePixel = TextGame.smallGuiAtlas.findRegion("pixelWhite")

        //Add the title of the description
        descriptionTable.add(titleLabel).colspan(3).spaceBottom(10f)
        descriptionTable.row()

        //Get the list of descriptions
        val descList = searchAct.description
        //For each description, lay it out on the description table
        descList.forEachIndexed { i, params ->
            if(params.size >= 3) {
                val nameLabel = Label(params[0], GameScreenGUI.defaultLabelStyle)
                nameLabel.setFontScale((0.15f))
                nameLabel.setAlignment(Align.center)

                val chanceLabel = Label(params[1], GameScreenGUI.defaultLabelStyle)
                chanceLabel.setFontScale((0.15f))
                chanceLabel.setAlignment(Align.center)

                val amountLabel = Label(params[2], GameScreenGUI.defaultLabelStyle)
                amountLabel.setFontScale((0.15f))
                amountLabel.setAlignment(Align.center)

                val divider = Image(whitePixel)
                divider.color = Color.GRAY

                descriptionTable.add(nameLabel).width(100f)
                descriptionTable.add(chanceLabel).fillX().width(100f)
                descriptionTable.add(amountLabel).fillX().width(100f)
                descriptionTable.row()
                if(i < descList.size-1) {
                    descriptionTable.add(divider).fillX().colspan(3).pad(5f, 0f, 5f, 0f)
                    descriptionTable.row()
                }
            }else{
                val label = Label(params[0], GameScreenGUI.defaultLabelStyle)
                label.setFontScale((GameScreenGUI.normalFontScale))
                label.setAlignment(Align.center)

                descriptionTable.add(label).colspan(3)
                descriptionTable.row()
            }
        }
    }

    private fun buildDropdownList(): Actor {
        val newFont = BitmapFont(Gdx.files.internal("fonts/spaceFont2.fnt"))
        newFont.data.setScale(GameScreenGUI.normalFontScale)

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

        GameScreenGUI.campMenu.selectBox = SelectBox(selectBoxStyle)

        val list:com.badlogic.gdx.utils.Array<Label> = com.badlogic.gdx.utils.Array()
        for(sa in DataManager.getSearchActiviesList()){
            val label = GameScreenGUI.CustomLabel(sa.buttonTitle, labelStyle)
            label.setFontScale(GameScreenGUI.normalFontScale)
            list.add(label)
        }

        selectBox.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                setupDescriptionTable(DataManager.SearchActivityJSON.getSearchActivity(GameScreenGUI.campMenu.selectBox.selected.text.toString())!!)
            }
        })

        GameScreenGUI.campMenu.selectBox.items = list
        GameScreenGUI.campMenu.selectBox.selected = list[0] //This simply triggers the above changelistener to call the function initially

        GameScreenGUI.campMenu.selectBox.setSelectedAlignment(Align.center)
        GameScreenGUI.campMenu.selectBox.list.setListAlignment(Align.center)
        return GameScreenGUI.campMenu.selectBox
    }

    private fun sliderTask():ChainTask{
        var task:ChainTask? = null
        task = ChainTask(
                //First, we run this task while our numHoursToAdvance is 0 and above and our searchActivity is not null
                {
                    GameStats.game.numHoursToAdvance >= 0 && GameStats.game.searchActivity != null
                },

                //Next, we check the restrictions and try to execute our search activity
                {
                    val restrictionResult = GH.parseAndCheckRestrictions(GameStats.game.searchActivity!!.restrictions!!)

                    //If our first value is true, our check passed. Proceed!
                    if (restrictionResult.first) {
                        activityHourSlider.value = GameStats.game.numHoursToAdvance.toFloat() - 1
                        GameStats.game.searchFunc?.forEach { func -> func?.invoke() }

                    //Otherwise, stop searching everything
                    } else {
                        GameStats.game.searchActivity = null
                        GameStats.game.searchFunc = null
                        GameStats.game.numHoursToAdvance = 0
                        disableAcceptButtonError(GH.getRestrictionFailReason(restrictionResult.second, restrictionResult.third))
                        task!!.setDone()
                    }

                    //If the slider value made it to 0, set this task as done and enable the button if able.
                    if(activityHourSlider.value.toInt() == 0) {
                        GameStats.game.searchActivity = null
                        GameStats.game.searchFunc = null
                        GameStats.game.numHoursToAdvance = 0
                        task!!.setDone()
//                        if(restrictionResult.first) //Enable the button as long as the restrictions were passed.
//                            enableAcceptButton()
                    }
                }
        )

        return task
    }
}