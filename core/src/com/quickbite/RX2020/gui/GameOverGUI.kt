package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.managers.GroupManager

/**
 * Created by Paha on 5/3/2016.
 */
class GameOverGUI {
    var mainTable: Table = Table()
    //25 - 75 random weeks

    fun gameOver(){
        TextGame.stage.clear()

        val labelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.BLACK)

        var titleDesc = DataManager.end.desc[0]
        titleDesc = titleDesc.replace("%o", 10.toString()).replace("%a", GroupManager.numPeopleAlive.toString()).replace("%m", 10.toString()).replace("d", 10.toString()).replace("h", 15.toString())

        val titleLabel = Label(titleDesc, labelStyle)
        titleLabel.setFontScale(0.2f)
        titleLabel.color.a = 0f

        val bodyLabel = Label(DataManager.end.desc[1], labelStyle)
        bodyLabel.setFontScale(0.2f)
        bodyLabel.color.a = 0f

        var finalDesc = DataManager.end.desc[2]
        finalDesc = finalDesc.replace("%w", MathUtils.random(25, 75).toString())

        val finalLabel = Label(finalDesc, labelStyle)
        finalLabel.setFontScale(0.2f)
        finalLabel.color.a = 0f

        mainTable.add(titleLabel)
        mainTable.row()
        mainTable.add(bodyLabel)
        mainTable.row()
        mainTable.add(finalDesc)
        mainTable.row()

        mainTable.setFillParent(true)
        TextGame.stage.addActor(mainTable)
    }
}