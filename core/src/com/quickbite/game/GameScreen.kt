package com.quickbite.game

import com.badlogic.gdx.*
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
    val timeScale:Float = 24f
    val table: Table = Table()

    val background = Texture(Gdx.files.internal("art/background.png"), true)
    val sunMoon = Texture(Gdx.files.internal("art/sunMoon.png"), true)
    val scrolling1 = Texture(Gdx.files.internal("art/Seemlessbackground.png"), true)
    val scrolling2 = Texture(Gdx.files.internal("art/Seemlessbackground.png"), true)

    var startingPos:Float = Gdx.graphics.width/2f - scrolling1.width.toFloat()*2f
    val pos1: Vector2 = Vector2(Gdx.graphics.width/2f - scrolling1.width.toFloat(), -Gdx.graphics.height/2f)
    val pos2: Vector2 = Vector2(startingPos, -Gdx.graphics.height/2f)

    var counter:Double = 0.0
    var currPosOfBackground:Float = 0f
    var currPosOfSun:Float = 0f

    var currTime:Int = 0
    var lastTime:Int = 0

    //Need total distance of the game, distance traveled, distance to go, mph traveling
    var totalDistOfGame:Int = MathUtils.random(36000, 108000)
    var totalDistTraveled:Int = 0
    var totalDistToGo:Int = 0
    var currMPH:Int = 20

    var paused = false

    val gui:GameScreenGUI = GameScreenGUI(this)

    val gameInput:GameScreenInput = GameScreenInput()


    override fun show() {
        gui.init()

        sunMoon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        //throw UnsupportedOperationException()

        Tester.testEvents("Event1", 20)

        val multi:InputMultiplexer = InputMultiplexer()
        multi.addProcessor(TextGame.stage)
        multi.addProcessor(gameInput)
        Gdx.input.inputProcessor = multi

        gameInput.keyEventMap.put(Input.Keys.P, {paused = !paused})
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
        if(!paused) update(delta)

        TextGame.batch.begin()
        draw(TextGame.batch)
        TextGame.batch.end()

        gui.update(delta)
        TextGame.stage.draw()
    }

    fun draw(batch:SpriteBatch){
        batch.color = Color.WHITE
        batch.draw(background, -400f, -background.height.toFloat() + Gdx.graphics.height/2f + (background.height - Gdx.graphics.height)*currPosOfBackground)
        batch.draw(sunMoon, -400f, -sunMoon.height.toFloat()/1.32f, sunMoon.width.toFloat()/2, sunMoon.height.toFloat()/2, sunMoon.width.toFloat(), sunMoon.height.toFloat(), 1f, 1f, MathUtils.radiansToDegrees* currPosOfSun,
                0, 0, sunMoon.width, sunMoon.height, false, true)

        batch.draw(scrolling1, pos1.x, pos1.y)
        batch.draw(scrolling2, pos2.x, pos2.y)
    }

    fun update(delta:Float){
        recordTime(delta)

        //the -MathUtils.PI/2f is to offset the value to 0. Since sine goes to -1 and 1 but normalize it 0 - 1, the initial value will be 0.5 and we don't want that!
        currPosOfBackground = (MathUtils.sin((((counter)/(timeScale/2f))*MathUtils.PI).toFloat() - MathUtils.PI/2f).toFloat() + 1f)/2f
        currPosOfSun = ((-counter)/(timeScale/2f)).toFloat()*MathUtils.PI

        pos1.set(pos1.x + currMPH/10, pos1.y)
        pos2.set(pos2.x + currMPH/10, pos1.y)

        if(pos1.x > Gdx.graphics.width/2f)
            pos1.set(pos2.x - scrolling2.width, -Gdx.graphics.height/2f)
        if(pos2.x > Gdx.graphics.width/2f)
            pos2.set(pos1.x - scrolling1.width, -Gdx.graphics.height/2f)

    }

    private fun recordTime(delta:Float){
        counter += delta
        currTime = (counter%timeScale).toInt()

        if(currTime != lastTime){
            lastTime = currTime
            onTimeTick(delta)
        }
    }

    private fun onTimeTick(delta:Float){
        totalDistTraveled += currMPH


        gui.updateOnTimeTick(delta)
    }

    override fun resume() {
        //throw UnsupportedOperationException()
    }

    override fun dispose() {
        //throw UnsupportedOperationException()
    }
}