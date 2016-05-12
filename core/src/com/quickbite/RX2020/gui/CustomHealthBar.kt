package com.quickbite.rx2020.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.Disableable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.managers.SupplyManager

/**
 * Created by Paha on 3/29/2016.
 * A specialized health bar for my game!
 */
class CustomHealthBar(val person: Person?, val background: TextureRegionDrawable, val whitePixel: TextureRegionDrawable) : Widget(), Disableable {
    var supply: SupplyManager.Supply? = null
    var currAmt:Float = 0f
    var maxAmt:Float = 0f
    var injuredAmt:Float = 0f

    constructor(supply: SupplyManager.Supply, background: TextureRegionDrawable, whitePixel: TextureRegionDrawable):this(null, background, whitePixel){
        this.supply = supply
    }

    constructor(currAmt:Float, maxAmt:Float, background: TextureRegionDrawable, whitePixel: TextureRegionDrawable):this(null, background, whitePixel){
        this.currAmt = currAmt
        this.maxAmt = maxAmt
    }

    constructor(currAmt:Float, injuredAmt:Float, maxAmt:Float, background: TextureRegionDrawable, whitePixel: TextureRegionDrawable):this(null, background, whitePixel){
        this.currAmt = currAmt
        this.injuredAmt = injuredAmt
        this.maxAmt = maxAmt
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        var currHealth = 0
        var maxHealth = 0

        //If we are displaying the health bar based on a person....
        if(batch != null && person != null){
            val missingHealth = width*((person.maxHealth.toFloat() - (person.healthInjury.toFloat()+person.healthNormal.toFloat()))/person.maxHealth.toFloat())
            val color = batch.color
            val healthColor = if(person.hasSickness) Color.YELLOW else Color.GREEN

            var injuryHealthBar = width*(person.healthInjury.toFloat()/person.maxHealth.toFloat())
            batch.color = Color.RED
            whitePixel.draw(batch, x + missingHealth, y, injuryHealthBar, height) //Go from the right to the left to posX

            var normalHealthBar = width*(person.healthNormal.toFloat()/person.maxHealth.toFloat())
            batch.color = healthColor
            whitePixel.draw(batch, x + missingHealth + injuryHealthBar, y, normalHealthBar, height)

            batch.color = color

            background.draw(batch, x, y, width, height) //Draw the bar background last.
        }else if(batch != null && supply != null){
            val missingHealth = width*(supply!!.maxHealth.toFloat() - supply!!.currHealth.toFloat())/supply!!.maxHealth.toFloat()
            val color = batch.color

            var healthBar = width*(supply!!.currHealth.toFloat()/supply!!.maxHealth.toFloat())
            batch.color = Color.GREEN
            whitePixel.draw(batch, x + missingHealth, y, healthBar, height) //Go from the right to the left to posX

            batch.color = color

            background.draw(batch, x, y, width, height) //Draw the bar background last.
        }
    }

    override fun setDisabled(p0: Boolean) {
        //        throw UnsupportedOperationException()
    }

    override fun isDisabled(): Boolean {
        //        throw UnsupportedOperationException()
        return false
    }

    class CustomHealthBarStyle{

    }

}