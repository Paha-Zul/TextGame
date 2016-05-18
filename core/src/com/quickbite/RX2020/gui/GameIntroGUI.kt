package com.quickbite.rx2020.gui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Json
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.GameStats
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.managers.SupplyManager
import com.quickbite.rx2020.screens.GameIntroScreen
import com.quickbite.rx2020.util.GH
import java.io.BufferedReader

/**
 * Created by Paha on 2/10/2016.
 */
class GameIntroGUI(val game: GameIntroScreen) {
    lateinit var reader: BufferedReader

    private val json: Json = Json()

    private val layoutTable: Table = Table()
    private val supplyTable: Table = Table()

    private lateinit var labelStyle:Label.LabelStyle
    private lateinit var buttonStyle:ImageTextButton.ImageTextButtonStyle
    private lateinit var drawable:TextureRegionDrawable

    private class IntroJson{
        var desc:Array<String> = arrayOf("")
        var action:String = ""
        var replace:Boolean = false
        var showSupplies:Boolean = false
    }

    init{
        TextGame.stage.clear()

        val intro = json.fromJson(Array<IntroJson>::class.java, Gdx.files.internal("files/text/intro.json"))

        labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK) //Black no opacity
        buttonStyle = ImageTextButton.ImageTextButtonStyle()
        drawable = TextureRegionDrawable(TextureRegion(Texture("art/sheets/smallui/nextButton.png")))

        buttonStyle.up = drawable
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.BLACK

        layoutTable.setFillParent(true)
        TextGame.stage.addActor(layoutTable)

        displayPage(intro, 0)
    }

    private fun displayPage(introList:Array<IntroJson>, pageNumber:Int){
        if(pageNumber >= introList.size) {
            val chainTask = ChainTask({ layoutTable.color.a > 0 }, {
                val value = GH.lerpValue(layoutTable.color.a, 1f, 0f, 1f)
                layoutTable.color.a = value
                TextGame.backgroundColor.r = value; TextGame.backgroundColor.g = value; TextGame.backgroundColor.b = value
            },{
//                TextGame.stage.clear();
                game.done = true;
                TextGame.backgroundColor.a = 0f
                TextGame.batch.color = Color(0f,0f,0f,0f)
                val blackPixel = TextGame.smallGuiAtlas.findRegion("pixelBlack")
                val task = ChainTask({TextGame.backgroundColor.r < 1},
                        {
                            TextGame.batch.begin()
                            var amt = TextGame.backgroundColor.r + 0.01f
                            TextGame.backgroundColor.r = amt; TextGame.backgroundColor.g=amt; TextGame.backgroundColor.b=amt; TextGame.backgroundColor.b=amt; TextGame.backgroundColor.a=amt
                            TextGame.batch.color = Color(0f, 0f, 0f, (1-amt))
                            TextGame.batch.draw(blackPixel, -TextGame.viewport.screenWidth/2f, -TextGame.viewport.screenHeight/2f, TextGame.viewport.screenWidth.toFloat(), TextGame.viewport.screenHeight.toFloat())
                            TextGame.batch.end()
                        })

                ChainTask.addTaskToEveryFrameList(task)
            })

            ChainTask.addTaskToEveryFrameList(chainTask)
            return
        }

        val intro = introList[pageNumber]
        layoutTable.clear()

        if(intro.desc.size > 1){
            val titleLabel = Label(intro.desc[0], labelStyle)
            titleLabel.setFontScale(0.4f)
            titleLabel.color.a = 0f

            val descLabel = Label(intro.desc[1], labelStyle)
            descLabel.setFontScale(0.25f)
            descLabel.setWrap(true)
            descLabel.color.a = 0f

            val nextPageButton = ImageTextButton("", buttonStyle)
            nextPageButton.color.a = 0f

            layoutTable.add(titleLabel).top().left()
            layoutTable.row()
            layoutTable.add(descLabel).center().fillX().expandX().top().pad(0f, 10f, 0f, 10f)
            layoutTable.row().expand().fill()
            layoutTable.add(nextPageButton).size(64f).bottom()

            titleLabel.addAction(Actions.fadeIn(1f))
            descLabel.addAction(Actions.sequence(Actions.delay(1f), Actions.fadeIn(1f)))
            nextPageButton.addAction(Actions.sequence(Actions.delay(2f), Actions.fadeIn(1f)))

            nextPageButton.addListener(object:ChangeListener(){
                override fun changed(p0: ChangeEvent?, p1: Actor?) {
                    displayPage(introList, pageNumber+1)
                }
            })
        }else{
            var desc = intro.desc[0]
            if(intro.replace){
                desc = desc.replace("%n", GameStats.TravelInfo.totalDistOfGame.toString())
            }

            val descLabel = Label(desc, labelStyle)
            descLabel.setFontScale(0.25f)
            descLabel.setWrap(true)
            descLabel.color.a = 0f
            descLabel.setAlignment(Align.center)

            val nextPageButton = ImageTextButton("",buttonStyle)
            nextPageButton.color.a = 0f
            nextPageButton.label.setFontScale(0.35f)
            nextPageButton.style.up = drawable

            if(intro.action.length > 0) {
                nextPageButton.text = intro.action
                nextPageButton.style.up = null
            }

            var delay = 0f
            var counter = 0

            if(intro.showSupplies){
                val supplyGroup = Table()
                val partGroup = Table()
                val survivorGroup = Table()

                var currTable = supplyGroup

//                crewMembers.addAction(Actions.sequence(Actions.delay(counter*0.1f), Actions.fadeIn(0.8f)))
//                supplyTable.add(crewMembers).colspan(2)
//                supplyTable.row()
//                counter++

                var titleLabel = Label("Supplies", labelStyle)
                titleLabel.setFontScale(0.25f)
                titleLabel.color.a = 0f

                currTable.add(titleLabel).colspan(2)
                currTable.row()

                titleLabel.addAction(Actions.sequence(Actions.delay(counter*0.1f), Actions.fadeIn(0.8f)))
                delay += 0.15f
                counter++

                SupplyManager.getSupplyList().forEachIndexed { i, supply ->
                    val nameLabel = Label(supply.displayName, labelStyle)
                    nameLabel.setFontScale(0.2f)
                    nameLabel.color.a = 0f

                    val amtLabel = Label(supply.amt.toInt().toString(), labelStyle)
                    amtLabel.setFontScale(0.2f)
                    amtLabel.color.a = 0f

                    if(supply.name.equals("panel")) {
                        titleLabel = Label("Spare ROV Parts", labelStyle)
                        titleLabel.setFontScale(0.25f)
                        titleLabel.color.a = 0f

                        supplyTable.add(currTable).space(0f, 10f, 0f, 10f).top()

                        currTable = partGroup
                        currTable.add(titleLabel).colspan(2)
                        currTable.row()

                        titleLabel.addAction(Actions.sequence(Actions.delay(counter*0.1f), Actions.fadeIn(0.8f)))
                        delay += 0.15f
                        counter++
                    }

                    if(supply.name.equals("panel") || supply.name.equals("track") || supply.name.equals("battery") || supply.name.equals("storage")){
                        nameLabel.setText("Spare " + nameLabel.text)
                    }

                    currTable.add(nameLabel)
                    currTable.add(amtLabel)
                    currTable.row()

                    nameLabel.addAction(Actions.sequence(Actions.delay(counter*0.1f), Actions.fadeIn(0.8f)))
                    amtLabel.addAction(Actions.sequence(Actions.delay(counter*0.1f), Actions.fadeIn(0.8f)))

                    delay += 0.15f
                    counter++
                }

                supplyTable.add(currTable).space(0f, 10f, 0f, 10f).top()
                currTable = survivorGroup

                titleLabel = Label("Survivors", labelStyle)
                titleLabel.setFontScale(0.25f)
                titleLabel.color.a = 0f

                currTable.add(titleLabel)
                currTable.row()

                titleLabel.addAction(Actions.sequence(Actions.delay(counter*0.1f), Actions.fadeIn(0.8f)))
                delay += 0.15f
                counter++

                for(person in GroupManager.getPeopleList()){
                    val nameLabel = Label(person.fullName, labelStyle)
                    nameLabel.setFontScale(0.2f)
                    nameLabel.color.a = 0f

                    currTable.add(nameLabel)
                    currTable.row()

                    nameLabel.addAction(Actions.sequence(Actions.delay(counter*0.1f), Actions.fadeIn(0.8f)))
                    delay += 0.15f
                    counter++
                }

                supplyTable.add(currTable).space(0f, 10f, 0f, 10f).top()
            }

            if(delay == 0f) delay = 1f

            layoutTable.add(descLabel).center().fillX().expandX().top().pad(0f, 10f, 0f, 10f)
            layoutTable.row()
            layoutTable.add(supplyTable).spaceTop(10f)
            layoutTable.row().expand().fill()
            layoutTable.add(nextPageButton).size(64f).bottom()

            descLabel.addAction(Actions.fadeIn(1f))
            nextPageButton.addAction(Actions.sequence(Actions.delay(delay), Actions.fadeIn(1.5f)))

            nextPageButton.addListener(object:ChangeListener(){
                override fun changed(p0: ChangeEvent?, p1: Actor?) {
                    displayPage(introList, pageNumber+1)
                }
            })
        }
    }

    fun update(delta:Float){

    }
}