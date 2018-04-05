package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.*

object GroupGUI {
    private val groupTable = Table()
    private val transparentBackground = NinePatchDrawable(NinePatch(TextGame.manager.get("GUIBackground", Texture::class.java), 4, 4, 4, 4))

    fun init():Table{
        return buildGroupTable()
    }

    fun buildGroupTable():Table{
        groupTable.clear()
        groupTable.width = 200f

        val map = DataManager.guiData.getTable("groupGUI").getTable("tiny")

        groupTable.background = transparentBackground
        groupTable.padRight(10f)

        val labelStyle: Label.LabelStyle = Label.LabelStyle(TextGame.manager.get("spaceFont2", BitmapFont::class.java), Color.WHITE)

        val imageButtonStyle: ImageButton.ImageButtonStyle = ImageButton.ImageButtonStyle()
        imageButtonStyle.up = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("medkit"))
        imageButtonStyle.disabled = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("medkitDisabled"))

        val hasMedkits = SupplyManager.getSupply("medkits").amt.toInt() > 0
        val maleGenderSymbol = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("maleGender"))
        val femaleGenderSymbol = TextureRegionDrawable(TextGame.smallGuiAtlas.findRegion("femaleGender"))

        //Get the current people as a list
        val list:Array<Person> = GroupManager.getPeopleList()

        //For each person...
        for(person: Person in list.iterator()){
            val pairTable = Table() //Make a table to hold the name and health

            //Here's the name
            val nameLabel = Label(person.fullName, labelStyle)
            nameLabel.setFontScale(map.getDouble("fontScale").toFloat())
            nameLabel.setAlignment(Align.center)

            //Here's the medkit button
            val medkitButton = ImageButton(imageButtonStyle)
            medkitButton.isDisabled = !hasMedkits || (!person.hasInjury && person.healthNormal >= person.totalMaxHealth)

            //Here's the recent change in health label
            val recentChangeLabel = Label("", labelStyle)
            recentChangeLabel.setFontScale(map.getDouble("fontScale").toFloat())
            recentChangeLabel.setAlignment(Align.right)

            //The gender symbol
            val genderImage: Image = if(person.male) Image(maleGenderSymbol) else Image(femaleGenderSymbol)

            //This is the change in health
            val change = ResultManager.recentChangeMap[person.firstName]
            if(change != null){
                var modifier = ""
                if(change.amt > 0) { //If the amount is positive
                    recentChangeLabel.color = Color.GREEN
                    modifier = "+"
                }else if(change.amt < 0) { //If the amount is negative
                    recentChangeLabel.color = Color.RED
                }
                //Set the text here
                recentChangeLabel.setText("$modifier${change.amt.toInt()}")
            }

            //Make the health bar for the person
            val healthBar = CustomHealthBar(person, TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("bar"))),
                    TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("pixelWhite"))))

            //The name table
            val nameTable = Table()
            nameTable.add(genderImage).size(map.getDouble("genderIconSize").toFloat()).right()
            nameTable.add(nameLabel).expandX().fillX().right()

            val healthBarSize = map.getList<Float>("healthBarSize")
            //The health table. This includes the recent changes, medkit, and health bar
            val healthTable = Table()
            healthTable.add(recentChangeLabel).spaceRight(5f).expandX().fillX().right()
            healthTable.add(medkitButton).size(map.getDouble("medkitButtonSize").toFloat()).center().spaceRight(5f)
            healthTable.add(healthBar).fillX().width(healthBarSize[0]).height(healthBarSize[1]).right()

            //Add the name and the health to the person
            pairTable.add(nameTable).expandX().fillX().right()
            pairTable.row()
            pairTable.add(healthTable).expandX().fillX().right()

            //Add the pair table to the group table
            groupTable.add(pairTable).expandX().fillX().right()
            groupTable.row().spaceTop(2f).right()

            //Add this listener for when the medkit is clicked. Basically we want to remove an ailment, add health, and subtract a medkit
            //Then we rebuild this table
            medkitButton.addListener(object: ChangeListener(){
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

        return groupTable
    }

    fun update(delta:Float){

    }
}