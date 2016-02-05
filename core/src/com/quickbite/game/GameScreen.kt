package com.quickbite.game

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Table

/**
 * Created by Paha on 2/3/2016.
 */
class GameScreen(val game: Game): Screen {
    val timeScale:Float = 12f
    val table: Table = Table()

    val background = Texture(Gdx.files.internal("art/background.png"), true)
    val sunMoon = Texture(Gdx.files.internal("art/sunMoon.png"), true)
    val scrolling1 = Texture(Gdx.files.internal("art/Seemlessbackground.png"), true)
    val scrolling2 = Texture(Gdx.files.internal("art/Seemlessbackground.png"), true)

    var startingPos:Float = Gdx.graphics.width/2f - scrolling1.width.toFloat()*2f
    val pos1: Vector2 = Vector2(Gdx.graphics.width/2f - scrolling1.width.toFloat(), -Gdx.graphics.height/2f)
    val pos2: Vector2 = Vector2(startingPos, -Gdx.graphics.height/2f)

    var currTime:Double = 0.0
    var currDist:Float = 0f
    var currPosOfBackground:Float = 0f
    var currPosOfSun:Float = 0f

    override fun show() {
        var tree:TreeNode<String> = TreeNode("I am you", "root")

        sunMoon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        //throw UnsupportedOperationException()
    }

    override fun hide() {
        //throw UnsupportedOperationException()
    }

    override fun resize(width: Int, height: Int) {
        TextGame.viewport.update(width, height)
        //throw UnsupportedOperationException()
    }

    override fun pause() {
        //throw UnsupportedOperationException()
    }

    override fun render(delta: Float) {
        TextGame.stage.draw()
        update(delta)

        TextGame.batch.begin()
        draw(TextGame.batch)
        TextGame.batch.end()
    }

    fun draw(batch:SpriteBatch){
        batch.color = Color.WHITE
        batch.draw(background, -400f, -background.height.toFloat() + 240f + currPosOfBackground*(background.height - 480f))
        batch.draw(sunMoon, -400f, -sunMoon.height.toFloat()/1.32f, sunMoon.width.toFloat()/2, sunMoon.height.toFloat()/2, sunMoon.width.toFloat(), sunMoon.height.toFloat(), 1f, 1f, MathUtils.radiansToDegrees* currPosOfSun,
                0, 0, sunMoon.width, sunMoon.height, false, true)

        val posX = (currDist%(scrolling1.width+Gdx.graphics.width) - scrolling1.width + Gdx.graphics.width/2f).toFloat()

        //batch.color = Color.GREEN
        batch.draw(scrolling1, pos1.x, pos1.y)
        //batch.color = Color.RED
        batch.draw(scrolling2, pos2.x, pos2.y)
    }

    fun update(delta:Float){
        currTime += delta
        currDist += 1
        currPosOfBackground = (MathUtils.sin(((currTime-timeScale)/timeScale).toFloat()).toFloat() + 1f)/2f;
        currPosOfSun = ((-currTime - timeScale*0.5f)/timeScale).toFloat()


        pos1.set(pos1.x + 2, pos1.y)
        pos2.set(pos2.x + 2, pos1.y)

        if(pos1.x > Gdx.graphics.width/2f)
            pos1.set(startingPos, -Gdx.graphics.height/2f)
        if(pos2.x > Gdx.graphics.width/2f)
            pos2.set(startingPos, -Gdx.graphics.height/2f)
    }

    override fun resume() {
        //throw UnsupportedOperationException()
    }

    override fun dispose() {
        //throw UnsupportedOperationException()
    }
}