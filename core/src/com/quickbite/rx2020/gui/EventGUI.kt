package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Queue
import com.quickbite.rx2020.Result
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.getFloat
import com.quickbite.rx2020.managers.*
import com.quickbite.rx2020.util.GH
import com.quickbite.rx2020.util.Logger

/**
 * Created by Paha on 6/28/2017.
 */
object EventGUI {
    private val guiQueue: Queue<Triple<GameEventManager.EventJson, Int, Boolean>> = Queue()

    private val eventTable = Table()
    private val eventContainer = Table()
    private val eventInnerTable = Table()
    private val eventChoicesTable = Table()

    lateinit var titleLabel:Label
    
    /**
     * Initially starts the event GUI
     * @param event The EventJson object to start the event
     * @param startPage The page to start the event, or recursively call the beginEventGUI function
     * @param eraseResultManagers True to erase the accumulated results of the event, false to keep them
     * @return True if the event was able to be started, false otherwise
     */
    fun beginEventGUI(event: GameEventManager.EventJson, startPage:Int = 0, eraseResultManagers:Boolean = true):Boolean{
        //If the GUI is already active, lets add it to the queue instead of going further.
        if(GameScreenGUIManager.gameEventGUIActive) {
            guiQueue.addLast(Triple(event, startPage, eraseResultManagers))
            println("[EventGUI:beingEventGUI] Do we ever already have an event gui active?")
            return false
        }

        //We do this to ensure that the currActiveEvent is not null. Sometimes when overlapping events happen
        //the currActiveEvent can become null after being set.
        GameEventManager.currActiveEvent = event

        ResultManager.clearResultLists()

        EventManager.callEvent("eventStarted", event.name)

        GameStats.game.pauseGame()
        eventTable.clear()
        eventContainer.remove()
        eventContainer.clear()
        eventContainer.setSize(300f, 350f)
        eventContainer.setPosition(TextGame.viewport.worldWidth/2f - eventContainer.width/2f, TextGame.viewport.worldHeight/2f - eventContainer.height/2f)
        eventContainer.background = TextureRegionDrawable(TextureRegion(TextGame.manager.get("eventBackground", Texture::class.java)))

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        titleLabel = Label(event.title, labelStyle)
        titleLabel.setAlignment(Align.center)
        titleLabel.setFontScale(DataManager.guiData.getFloat("eventGUI", "tiny", "eventTitleScale"))
//        titleLabel!!.setWrap(true)

        eventTable.add(titleLabel).padTop(5f).height(40f)
        eventTable.row()
        eventTable.add(eventInnerTable).expand().fill()

        eventContainer.add(eventTable).expand().fill()
        TextGame.stage.addActor(eventContainer)

        handleEvent(event, startPage)

        return true
    }

    /**
     * Where the end of an event gets handled. This is where we decide to show results, show outcomes, or continue onto another event piece
     * @param event The EventJson object to handle
     * @param startPage The starting page. This is useful for recursively handling multiple paged events
     */
    private fun handleEvent(event: GameEventManager.EventJson?, startPage:Int = 0){
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
        eventInnerTable.clear()
        eventChoicesTable.clear()

        //Let's check if we are within the page number boundaries
        //If we are over the description size....
        if(pageNumber >= event.description.size && event.hasChoices){
            eventInnerTable.add(eventChoicesTable).expand().fill()

            //Make a button for each choice.
            for(i in 0 until event.choices!!.size){
                val choice = event.choices!![i]

                //Need a style specifically for each button since we may be changing colors.
                val buttonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
                buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
                buttonStyle.fontColor = Color.WHITE

                val modifiedText = GH.replaceChoiceForEvent(choice, event)
                val button = TextButton("($modifiedText)", buttonStyle)
                button.pad(0f, 10f, 0f, 10f)
                button.label.setFontScale(DataManager.guiData.getFloat("eventGUI", "tiny", "buttonFontScale"))
                button.label.setWrap(true)

                eventChoicesTable.add(button).minHeight(40f).expandX().fillX()
                eventChoicesTable.row()

                //If we don't pass the restrictions, disable this button
                if(event.hasRestrictions && !GH.parseAndCheckRestrictions(event.restrictions!![i]).first){
                    button.isDisabled = true
                    button.style.fontColor = Color.GRAY
                }

                //Choose a choice buttons.
                button.addListener(object: ChangeListener(){
                    override fun changed(evt: ChangeEvent?, actor: Actor?) {
                        //outerEventTable.remove()
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

            val nextPageButtonStyle: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
            nextPageButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
            nextPageButtonStyle.fontColor = Color.WHITE

            //Make the description label
            val descLabel = Label(event.modifiedDescription[pageNumber], labelStyle)
            descLabel.setAlignment(Align.center)
            descLabel.setFontScale(DataManager.guiData.getFloat("eventGUI", "tiny", "eventTextFontScale"))
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
            val nextPageButton = TextButton("", nextPageButtonStyle)
            nextPageButton.label.setFontScale(0.15f)

            //Add the title and description label
            eventInnerTable.add(scrollPane).expand().fill().pad(10f, 10f, 0f, 10f).center()
            eventInnerTable.row()

            val hasAnotherPage = event.description.size - 1 > pageNumber
            val hasAnotherSomething = event.description.size - 1 > pageNumber || (event.hasChoices && event.choices!!.size > 1)
                    || event.hasOutcomes || event.hasActions || ResultManager.hasEventResults
            val toNext = hasAnotherSomething || event.hasChoices && event.choices!!.size == 1

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
            eventInnerTable.add(nextPageButton).size(40f).padBottom(5f).bottom()

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
            eventTable.invalidateHierarchy()
            eventTable.act(0.016f)
            eventTable.validate()

            eventContainer.invalidateHierarchy()
            eventContainer.act(0.016f)
            eventContainer.validate()

            eventInnerTable.invalidateHierarchy()
            eventInnerTable.act(0.016f)
            eventInnerTable.validate()

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
        eventInnerTable.clear()

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
        val closeButton = TextButton("- Close -", textButtonStyle)
        closeButton.label.setFontScale(DataManager.guiData.getFloat("eventGUI", "tiny", "buttonFontScale"))

        val resultManagerTable = Table()

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

            nameLabel.setFontScale(GUIScale.Normal.fontScale)
            amtLabel.setFontScale(GUIScale.Normal.fontScale)

            resultManagerTable.add(amtLabel).padRight(10f)
            resultManagerTable.add(nameLabel)
            resultManagerTable.row()
        }

        for (death in deathList) {
            if(death == null) continue

            val label = Label(death.name + death.desc.toString(), labelStyle)
            label.setFontScale(GUIScale.Normal.fontScale)
            label.setWrap(true)
            label.setAlignment(Align.center)

            resultManagerTable.add(label).expand().fill()
            resultManagerTable.row()
        }

        val container = Container<Table>(resultManagerTable).fill().pad(10f, 10f, 0f, 10f)
        container.padTop(10f)

        //Put it into a scrollpane
        val scrollPane = ScrollPane(container, scrollPaneStyle)
        scrollPane.setFadeScrollBars(false)

        //Arrange it in the table.
        eventInnerTable.add(scrollPane).expand().fill()
        eventInnerTable.row()
        eventInnerTable.add(closeButton).bottom().height(50f)

        //Create a listener
        closeButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                onDoneCallback()
            }
        })

        eventInnerTable.validate()
        eventInnerTable.act(0.016f)

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
            val enableCamp = true
            var enableSettings = false
            EventManager.callEvent("eventFinished", GameEventManager.currActiveEvent!!.name)
            GameEventManager.lastCurrEvent = GameEventManager.currActiveEvent
            GameEventManager.currActiveEvent = null
            eventContainer.remove()

            //If the queue is not empty, lets call the event gui again
            if (guiQueue.size != 0) {
                val last = guiQueue.removeLast()
                beginEventGUI(last.first, last.second, last.third)

            //Otherwise, if the trade window table is not open, we want to resume the game!
            } else if (!GameScreenGUIManager.tradeWindowOpen) {
                GameStats.game.resumeGame()
                enableSettings = true
            }

            GameScreenGUIManager.closeEventGUI(enableCamp, enableSettings)
        }
    }
}