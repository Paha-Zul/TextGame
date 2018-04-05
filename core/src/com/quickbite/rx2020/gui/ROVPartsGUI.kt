package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.managers.ROVManager
import com.quickbite.rx2020.managers.ResultManager
import com.quickbite.rx2020.managers.SupplyManager

object ROVPartsGUI {
    private val ROVTable = Table()
    private val transparentBackground = NinePatchDrawable(NinePatch(TextGame.manager.get("GUIBackground", Texture::class.java), 4, 4, 4, 4))

    fun init():Table{
        buildROVTable()
        return ROVTable
    }

    /**
     * Builds the group table layout.
     */
    fun buildROVTable(){
        ROVTable.clear()

        ROVTable.background = transparentBackground

        val map = DataManager.guiData.getTable("partsGUI").getTable("tiny")

        val medkit = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("repair"))
        val medkitDis = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("repairDisabled"))

        val bg = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("bar"))
        val pixel = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("pixelWhite"))

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val ROVNameLabel = Label("ROV", labelStyle)
        ROVNameLabel.setFontScale(GUIScale.Normal.fontScale)

        val list = ROVManager.ROVPartMap
        for(supply in list.values){
            //Create the name label
            val nameLabel = Label(supply.displayName, labelStyle)
            nameLabel.setFontScale(map.getDouble("fontScale").toFloat()) //Set the font scale

            var repairButton: ImageButton? = null
            if(supply.name != "ROV") {
                repairButton = ImageButton(medkit)
                repairButton.style.imageDisabled = medkitDis
                if (supply.amt <= 0 || supply.currHealth >= supply.maxHealth)
                    repairButton.isDisabled = true
            }

            //The recent change amount label
            val changeLabel = Label("", labelStyle)
            changeLabel.setFontScale(map.getDouble("fontScale").toFloat())

            //Get the result manager. This holds recent changes
            val resultManager = ResultManager.recentChangeMap["${supply.name} health"]
            if(resultManager != null){
                var modifier = ""
                if(resultManager.amt > 0) { //If positive, make the color green and add a +
                    changeLabel.color = Color.GREEN
                    modifier = "+"
                }else if(resultManager.amt < 0) { //If negative, make the color red and add a -
                    changeLabel.color = Color.RED
                    modifier = "-"
                }
                changeLabel.setText("$modifier${resultManager.amt.toInt()}")
            }

            val healthBar = CustomHealthBar(supply, bg, pixel) //The health bar
            val hpBarDimension = map.getList<Float>("healthBarSize") //Get the health bar dimensions

            //Add all the stuff to the table
            ROVTable.add(nameLabel).right().colspan(3)
            ROVTable.row()
            ROVTable.add(changeLabel).right()
            ROVTable.add(repairButton).space(0f, 10f, 0f, 10f).size(map.getDouble("repairButtonSize").toFloat()).right()
            ROVTable.add(healthBar).right().width(hpBarDimension[0]).height(hpBarDimension[1])
            ROVTable.row().spaceTop(5f)

            //Add a listener to the repair button
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

    fun update(delta:Float){

    }
}