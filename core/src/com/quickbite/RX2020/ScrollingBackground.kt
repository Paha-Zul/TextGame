package com.quickbite.rx2020

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.quickbite.rx2020.interfaces.IResetable

/**
 * Created by Paha on 2/10/2016.
 */

class ScrollingBackground(val sprite: Sprite?, val speed:Float, x:Float, y:Float, var resetCallback: (() -> Unit)? = null) : IResetable{
    lateinit var following:ScrollingBackground
    var invisible:Boolean = false

    init{
        sprite?.y = y
        sprite?.x = x
    }

    fun update(delta:Float){
        if(sprite != null) {
            sprite.setPosition(sprite.x + speed, sprite.y)
            if (sprite.x >= TextGame.camera.viewportWidth/2f)
                reset()
        }
    }

    fun draw(batch:SpriteBatch, color: Color){
        if(sprite != null && !invisible) {
            sprite.color = color
            sprite.draw(batch)
        }
    }

    override fun reset(){
        sprite!!.setPosition(following.sprite!!.x - following.sprite!!.width + 10f, sprite.y)
        resetCallback?.invoke()
    }
}
