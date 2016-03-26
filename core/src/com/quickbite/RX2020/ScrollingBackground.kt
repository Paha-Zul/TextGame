package com.quickbite.rx2020

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * Created by Paha on 2/10/2016.
 */

class ScrollingBackground(val sprite: Sprite?, val speed:Float, x:Float, y:Float){
    lateinit var following:ScrollingBackground

    init{
        sprite?.y = y
        sprite?.x = x
    }

    fun update(delta:Float){
        if(sprite != null) {
            sprite.setPosition(sprite.x + speed, sprite.y)
            if (sprite.x >= TextGame.camera.viewportWidth/2f)
                sprite.setPosition(following.sprite!!.x - following.sprite!!.width + 10f, sprite.y)
        }
    }

    fun draw(batch:SpriteBatch, color: Color){
        if(sprite != null) {
            sprite.color = color
            sprite.draw(batch)
        }
    }
}
