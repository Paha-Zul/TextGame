package com.quickbite.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch

/**
 * Created by Paha on 2/10/2016.
 */

class ScrollingBackground(val sprite: Sprite, val speed:Float, x:Float, y:Float){
    lateinit var following:ScrollingBackground

    init{
        sprite.y = y
        sprite.x = x
    }

    fun update(delta:Float){
        sprite.setPosition(sprite.x + speed, sprite.y)
        if(sprite.x >= Gdx.graphics.width)
            sprite.setPosition(following.sprite.x - following.sprite.width + 10f, sprite.y)
    }

    fun draw(batch:SpriteBatch, color: Color){
        sprite.color = color
        sprite.draw(batch)
    }
}
