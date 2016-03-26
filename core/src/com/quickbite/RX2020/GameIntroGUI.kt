package com.quickbite.rx2020

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.quickbite.rx2020.managers.SupplyManager
import com.quickbite.rx2020.screens.GameIntroScreen
import java.io.BufferedReader

/**
 * Created by Paha on 2/10/2016.
 */
class GameIntroGUI(val game:GameIntroScreen) {
    private val page1 = Gdx.files.internal("files/text/page1.txt");
    private val page2 = Gdx.files.internal("files/text/page2.txt");
    private val page3 = Gdx.files.internal("files/text/page3.txt");

    lateinit var reader:BufferedReader

    private lateinit var chainTask: ChainTask

    init{

    }

    fun firstPage(){
        TextGame.stage.clear()
        reader = BufferedReader(page1.reader());

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK) //Black no opacity
        val buttonStyle:ImageButton.ImageButtonStyle = ImageButton.ImageButtonStyle()
        var drawable = TextureRegionDrawable(TextureRegion(TextGame.manager.get("nextPage", Texture::class.java)))
        buttonStyle.up = drawable
        buttonStyle.over = drawable
        buttonStyle.down = drawable

        val layoutTable: Table = Table()
        layoutTable.setFillParent(true)

        val titleLabel:Label = Label(reader.readLine(), labelStyle)
        titleLabel.setFontScale(0.4f)
        titleLabel.color.a = 0f

        val bodyLabel:Label = Label(reader.readLine(), labelStyle)
        bodyLabel.setFontScale(0.2f)
        bodyLabel.setWrap(true)
        bodyLabel.color.a = 0f

        val nextPageButton:ImageButton = ImageButton(buttonStyle)
        nextPageButton.color.a = 0f

        layoutTable.top()
        layoutTable.add(titleLabel).left()
        layoutTable.row()
        layoutTable.add(bodyLabel).expandX().fillX().padLeft(5f)
        layoutTable.row().fill().expand()
        layoutTable.add(nextPageButton).size(64f, 64f).bottom()

        nextPageButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                secondPage()
            }
        })

        TextGame.stage.addActor(layoutTable)

        chainTask = ChainTask({titleLabel.color.a >= 1}, {
            titleLabel.color.a = GH.lerpValue(titleLabel.color.a, 0f, 1f, 1f)
        })

        val secondChain = chainTask.setChain(ChainTask({bodyLabel.color.a >= 1}, {
            bodyLabel.color.a = GH.lerpValue(bodyLabel.color.a, 0f, 1f, 1f)
        }))

        secondChain.setChain(ChainTask({nextPageButton.color.a >= 1}, {
            nextPageButton.color.a = GH.lerpValue(nextPageButton.color.a, 0f, 1f, 1f)
        }))
    }

    fun secondPage(){
        TextGame.stage.clear()

        reader = BufferedReader(page2.reader());

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK) //Black no opacity
        val buttonStyle:ImageButton.ImageButtonStyle = ImageButton.ImageButtonStyle()
        var drawable = TextureRegionDrawable(TextureRegion(TextGame.manager.get("nextPage", Texture::class.java)))
        buttonStyle.up = drawable
        buttonStyle.over = drawable
        buttonStyle.down = drawable

        val layoutTable: Table = Table()
        layoutTable.setFillParent(true)

        val bodyLabel:Label = Label(reader.readLine(), labelStyle)
        bodyLabel.setFontScale(0.2f)
        bodyLabel.setWrap(true)
        bodyLabel.color.a = 0f

        val nextPageButton:ImageButton = ImageButton(buttonStyle)
        nextPageButton.color.a = 0f

        layoutTable.add(bodyLabel).expand().fill().padLeft(5f)
        layoutTable.row()
        layoutTable.add(nextPageButton).size(64f).bottom()

        nextPageButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                thirdPage()
            }
        })

        chainTask = ChainTask({bodyLabel.color.a >= 1}, {
            bodyLabel.color.a = GH.lerpValue(bodyLabel.color.a, 0f, 1f, 1f)
        })

        chainTask.setChain(ChainTask({nextPageButton.color.a >= 1}, {
            nextPageButton.color.a = GH.lerpValue(nextPageButton.color.a, 0f, 1f, 1f)
        }))

        TextGame.stage.addActor(layoutTable)
    }

    fun thirdPage(){
        TextGame.stage.clear()

        reader = BufferedReader(page3.reader());

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)
        val buttonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.BLACK

        val layoutTable: Table = Table()
        layoutTable.setFillParent(true)

        val bodyLabel:Label = Label(reader.readLine()+"\n\nThe ship’s last coordinates place the crew ${GameStats.TravelInfo.totalDistOfGame} miles away from the RX-2020.", labelStyle)
        bodyLabel.setFontScale(0.2f)
        bodyLabel.setWrap(true)
        bodyLabel.color.a = 0f

        val nextPageButton:TextButton = TextButton("Count the Losses", buttonStyle)
        nextPageButton.color.a = 0f
        nextPageButton.label.setFontScale(0.4f)

        layoutTable.add(bodyLabel).expand().fill().padLeft(5f)
        layoutTable.row()
        layoutTable.add(nextPageButton).bottom()

        nextPageButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                suppliesPage()
            }
        })

        chainTask = ChainTask({bodyLabel.color.a >= 1}, {
            bodyLabel.color.a = GH.lerpValue(bodyLabel.color.a, 0f, 1f, 1f)
        })

        chainTask.setChain(ChainTask({nextPageButton.color.a >= 1}, {
            nextPageButton.color.a = GH.lerpValue(nextPageButton.color.a, 0f, 1f, 1f)
        }))

        TextGame.stage.addActor(layoutTable)
    }

    fun suppliesPage(){
        TextGame.stage.clear()

        reader = BufferedReader(page3.reader());

        val labelStyle:Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)
        val buttonStyle:TextButton.TextButtonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.BLACK

        val layoutTable: Table = Table()
        layoutTable.setFillParent(true)

        val titleLabel:Label = Label("Supplies Recovered", labelStyle)
        titleLabel.color.a = 0f
        titleLabel.setFontScale(0.4f)

        layoutTable.add(titleLabel)
        layoutTable.row()

        val labelList:MutableList<Label> = arrayListOf()
        val list = SupplyManager.getSupplyList()
        for(i in list.indices){
            val supply = list[i]
            val label = Label(supply.displayName+": "+supply.amt.toInt(), labelStyle)
            label.setFontScale(0.2f)
            label.color.a = 0f
            layoutTable.add(label)
            layoutTable.row()
            labelList.add(label)
        }

        val nextPageButton:TextButton = TextButton("Embark", buttonStyle)
        nextPageButton.color.a = 0f
        nextPageButton.label.setFontScale(0.4f)

        layoutTable.row().padTop(15f)
        layoutTable.add(nextPageButton)

        nextPageButton.addListener(object:ChangeListener(){
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                chainTask = ChainTask({layoutTable.color.a <= 0}, {layoutTable.color.a = GH.lerpValue(layoutTable.color.a, 1f, 0f, 1f)})
                chainTask.setChain(ChainTask(null, { TextGame.stage.clear(); game.done = true;}))

            }
        })

        chainTask = ChainTask({titleLabel.color.a >= 1}, {titleLabel.color.a = GH.lerpValue(titleLabel.color.a, 0f, 1f, 0.1f)})
        var nextChain:ChainTask = chainTask;

        for(label in labelList){
            nextChain = nextChain.setChain(ChainTask({label.color.a >= 1}, {
                label.color.a = GH.lerpValue(label.color.a, 0f, 1f, 0.1f)
            }))
        }

        nextChain.setChain(ChainTask({nextPageButton.color.a >= 1}, {
            nextPageButton.color.a = GH.lerpValue(nextPageButton.color.a, 0f, 1f, 1f)
        }))

        TextGame.stage.addActor(layoutTable)
    }

    fun update(delta:Float){
        chainTask.update()
    }
}