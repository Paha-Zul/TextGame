package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.SaveLoad
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.managers.ROVManager
import com.quickbite.rx2020.managers.SupplyManager
import com.quickbite.rx2020.screens.GameIntroScreen
import com.quickbite.rx2020.screens.GameScreen
import com.quickbite.rx2020.screens.MainMenuScreen
import com.quickbite.rx2020.util.GH

/**
 * Created by Paha on 5/16/2016.
 */
class MainMenuGUI(val mainMenu:MainMenuScreen) {

    fun showMainMenu(){
        TextGame.stage.clear()

        val mainTable:Table = Table()
        val titleTable:Table = Table()
        val buttonTable:Table = Table()
        val rightTable = Table()

        rightTable.right()
        rightTable.bottom()

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val style: TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        style.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        style.fontColor = Color.WHITE

        val continueButton = TextButton("Continue", style);
        continueButton.label.setFontScale(0.4f)
        if(!SaveLoad.saveExists()) {
            continueButton.setColor(0f, 0f, 0f, 0.3f)
            continueButton.isDisabled = true
        }

        val startButton = TextButton("Start", style);
        startButton.label.setFontScale(0.4f)

        val titleLabel = Label("RX-2020", labelStyle)
        titleLabel.setFontScale(0.8f)

        val redditButton = TextButton("Reddit", style);
        redditButton.label.setFontScale(0.2f)

        val donateButton = TextButton("Donate", style);
        donateButton.label.setFontScale(0.2f)

        rightTable.add(redditButton).minHeight(40f)
        rightTable.row()

        //For now, only add the donate button on tester devices.
        if(TextGame.GPGServices.isTestDevice) rightTable.add(donateButton).minHeight(40f)

        rightTable.pad(0f, 0f, 20f, 20f)

        startButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                ChainTask.addTaskToEveryFrameList(crazyFade())
                GroupManager.init()
                SupplyManager.init()
                ROVManager.init()
            }
        })

        continueButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                ChainTask.addTaskToEveryFrameList(continueGameFade(mainTable, rightTable))
                GroupManager.init()
                SupplyManager.init()
                ROVManager.init()
            }
        })

        redditButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                TextGame.GPGServices.openURL("https://www.reddit.com/r/rx2020")
            }
        })

        donateButton.addListener(object: ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
//                TextGame.GPGServices.donate()
                showDonationPage()
            }
        })

        titleTable.add(titleLabel).pad(20f)

        buttonTable.add(continueButton).width(150f).height(50f)
        buttonTable.row().padTop(20f)
        buttonTable.add(startButton).width(150f).height(50f).padBottom(50f)

        buttonTable.bottom()

        titleTable.top()

        mainTable.add(titleTable).fill().expand()
        mainTable.row()
        mainTable.add(buttonTable).fill().expand()

        mainTable.color.a = 1f
        rightTable.color.a = 1f
        mainTable.setFillParent(true)
        rightTable.setFillParent(true)

        TextGame.stage.addActor(mainTable)
        TextGame.stage.addActor(rightTable)

        //I like to fade almost everything
        mainTable.addAction(Actions.fadeIn(1f))
        rightTable.addAction(Actions.fadeIn(1f))
    }

    private fun showDonationPage(){
        TextGame.stage.clear()

        val boxTable = Table()

        val textButtonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        textButtonStyle.fontColor = Color.WHITE

        val purchaseButton = TextButton("Purchase!", textButtonStyle)
        purchaseButton.label.setFontScale(0.4f)
        purchaseButton.setSize(100f, 50f)
        purchaseButton.setPosition(TextGame.viewport.worldWidth.toFloat()/2f - 50f, TextGame.viewport.worldHeight.toFloat() - 50f)

        val homeButton = TextButton("Home", textButtonStyle)
        homeButton.label.setFontScale(0.2f)
        homeButton.setSize(100f, 50f)
        homeButton.setPosition(TextGame.viewport.worldWidth.toFloat() - 100f, 0f)

        val box = TextureRegionDrawable(TextureRegion(TextGame.manager.get("donateBox", Texture::class.java)))
        val boxSelected = TextureRegionDrawable(TextureRegion(TextGame.manager.get("donateBoxSelected", Texture::class.java)))

        val amounts = listOf(1, 5, 10, 20)
        val buttonList:MutableList<ImageTextButton> = mutableListOf()

        var selected:Int = 0
        for(i in 0.rangeTo(amounts.size-1)){
            val amount = amounts[i]
            val buttonStyle:ImageTextButton.ImageTextButtonStyle = ImageTextButton.ImageTextButtonStyle(box, box, boxSelected, TextGame.manager.get("spaceFont2", BitmapFont::class.java))
            val button = ImageTextButton("$$amount", buttonStyle)
            button.label.setFontScale(0.4f)
            button.labelCell.padBottom(50f)
            buttonList += button

            if(i == 0) boxTable.add().expand().fill()
            boxTable.add(button)
            if(i <= amounts.size-1) boxTable.add().expand().fill()

            button.addListener(object:ClickListener(){
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    buttonList.forEach { b -> if(b !== button) b.isChecked = false }
                    button.isChecked = true
                    selected = amount
                }
            })
        }

        purchaseButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                if(selected != 0)
                    TextGame.GPGServices.donate(selected)
            }
        })

        homeButton.addListener(object:ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                showMainMenu()
            }
        })

        boxTable.setFillParent(true)

        TextGame.stage.addActor(boxTable)
        TextGame.stage.addActor(purchaseButton)
        TextGame.stage.addActor(homeButton)
    }

    private fun crazyFade():ChainTask{
        val tsk = ChainTask({TextGame.backgroundColor.r < 1f}, {TextGame.backgroundColor.r+=0.05f; TextGame.backgroundColor.g+=0.05f; TextGame.backgroundColor.b+=0.05f},
                {mainMenu.game.screen = GameIntroScreen(mainMenu.game) })

        return tsk
    }

    private fun continueGameFade(mainTable:Table, rightTable:Table):ChainTask{
        val chainTask = ChainTask({ mainTable.color.a > 0 }, {
            val value = GH.lerpValue(mainTable.color.a, 1f, 0f, 1f)
            mainTable.color.a = value
            rightTable.color.a = value
        },{
            //Load the game!
            val gameScreen = GameScreen(mainMenu.game, true)
            mainMenu.game.screen = gameScreen
        })

        return chainTask
    }
}