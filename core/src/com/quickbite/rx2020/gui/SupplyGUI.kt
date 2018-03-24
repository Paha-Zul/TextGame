package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Align
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.managers.ResultManager
import com.quickbite.rx2020.managers.SupplyManager

object SupplyGUI {
    private val supplyTable = Table()
    private val transparentBackground = NinePatchDrawable(NinePatch(TextGame.manager.get("GUIBackground", Texture::class.java), 4, 4, 4, 4))

    private val supplyAmountList:MutableList<Label> = arrayListOf()
    private val supplyChangeList:MutableList<Label> = arrayListOf()

    fun init():Table{
        buildSupplyTable()
        return supplyTable
    }

    /**
     * Builds the supply table layout
     */
    private fun buildSupplyTable(){
        supplyTable.clear()
        supplyAmountList.clear()
        supplyChangeList.clear()

        supplyTable.padLeft(10f)
        supplyTable.background = transparentBackground

        val map = DataManager.guiData.getTable("supplyGUI").getTable("tiny")

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val title = Label("Supplies", labelStyle)
        title.setFontScale(GameScreenGUI.titleFontScale)

        val innerTable = Table()
        val list = SupplyManager.getSupplyList()
        for(i in list.indices){
            val value = list[i]
            val nameLabel = Label(value.displayName, labelStyle)
            nameLabel.setFontScale(map.getDouble("fontScale").toFloat())
            nameLabel.setAlignment(Align.left)

            val amtLabel = Label("" + value.amt.toInt(), labelStyle)
            amtLabel.setFontScale(map.getDouble("fontScale").toFloat())
            amtLabel.setAlignment(Align.left)

            val changeLabel = Label("", labelStyle)
            changeLabel.setFontScale(map.getDouble("fontScale").toFloat())
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

    fun updateSuppliesGUI(){
        val list = SupplyManager.getSupplyList()
        for(i in list.indices){
            supplyAmountList[i].setText( list[i].amt.toInt().toString())

            //Update the change list
            supplyChangeList[i].setText("")
            val supplyChanged = ResultManager.recentChangeMap[list[i].displayName]
            if(supplyChanged != null) {
                supplyChangeList[i].setText(supplyChanged.amt.toInt().toString())
                when {
                    supplyChanged.amt > 0 -> supplyChangeList[i].color = Color.GREEN
                    supplyChanged.amt < 0 -> supplyChangeList[i].color = Color.RED
                    else -> supplyChangeList[i].color = Color.WHITE
                }
            }
        }
    }
}