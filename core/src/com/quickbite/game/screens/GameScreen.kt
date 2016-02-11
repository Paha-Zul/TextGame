package com.quickbite.game.screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.quickbite.game.*
import com.quickbite.game.managers.DataManager
import com.quickbite.game.managers.EventManager

/**
 * Created by Paha on 2/3/2016.
 */
class GameScreen(val game: Game): Screen {
    enum class State{
        TRAVELING, CAMP
    }
    var state = State.TRAVELING

    val table: Table = Table()

    val backgroundSky = Texture(Gdx.files.internal("art/backgroundSky.png"), true)
    val sunMoon = Texture(Gdx.files.internal("art/sunMoon.png"), true)

    val scrollingBackgroundList:MutableList<ScrollingBackground> = arrayListOf()
    val campScreenBackground: Texture = TextGame.manager.get("Camp", Texture::class.java)

    val ROV: Texture = TextGame.manager.get("ROV", Texture::class.java)

    var currPosOfBackground:Float = 0f
    var currPosOfSun:Float = 0f

    //Need total distance of the game, distance traveled, distance to go, mph traveling
    var totalDistOfGame:Int = MathUtils.random(36000, 108000)
    var totalDistTraveled:Int = 0
    var totalDistToGo:Int = 0
    var currMPH:Int = 20

    val eventCustomTimerTest: CustomTimer = CustomTimer(null, MathUtils.random(10, 30).toFloat())

    private var paused = false

    val gui: GameScreenGUI = GameScreenGUI(this)

    val gameInput: GameScreenInput = GameScreenInput()

    override fun show() {
        GameStats.init(this)
        gui.init()

        sunMoon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        //throw UnsupportedOperationException()

        //Tester.testEvents("Event1", 20)

        val multi: InputMultiplexer = InputMultiplexer()
        multi.addProcessor(TextGame.stage)
        multi.addProcessor(gameInput)
        Gdx.input.inputProcessor = multi

        val sc1: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Foreground", Texture::class.java)), 3f, -100f, -Gdx.graphics.height / 2f)
        val sc2: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Foreground", Texture::class.java)), 3f, 800f, -Gdx.graphics.height / 2f)
        sc1.following = sc2
        sc2.following = sc1

        val sc3: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Midground", Texture::class.java)), 2f, -100f, -Gdx.graphics.height / 2f)
        val sc4: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Midground", Texture::class.java)), 2f, 800f, -Gdx.graphics.height / 2f)
        sc3.following = sc4
        sc4.following = sc3

        val sc5: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Background", Texture::class.java)), 1f, -100f, -Gdx.graphics.height / 2f)
        val sc6: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Background", Texture::class.java)), 1f, 800f, -Gdx.graphics.height / 2f)
        sc5.following = sc6
        sc6.following = sc5

        scrollingBackgroundList.add(sc6)
        scrollingBackgroundList.add(sc5)
        scrollingBackgroundList.add(sc4)
        scrollingBackgroundList.add(sc3)
        scrollingBackgroundList.add(sc2)
        scrollingBackgroundList.add(sc1)

        //gameInput.keyEventMap.put(Input.Keys.E, {gui.triggerEventGUI(DataManager.rootEventMap["Event1"]!!); paused = true})

        var currEvent: DataManager.EventJson? = DataManager.rootEventMap.values.toTypedArray()[MathUtils.random(DataManager.rootEventMap.size-1)]

        val func = {
            if(currEvent!=null){
                val _evt = currEvent as DataManager.EventJson
                gui.triggerEventGUI(_evt, { choice ->
                    //If the list has a resulting action, call it!
                    val list = currEvent!!.resultingAction;
                    if(list != null && list.size > 0)
                        EventManager.callEvent(list[0], list.slice(1.rangeTo(list.size)))

                    currEvent = _evt.selected(choice, MathUtils.random(100))
                    if(currEvent != null){
                        eventCustomTimerTest.restart(0.00001f)
                    }else{
                        currEvent = DataManager.rootEventMap.values.toTypedArray()[MathUtils.random(DataManager.rootEventMap.size-1)]
                        eventCustomTimerTest.restart(MathUtils.random(10, 30).toFloat())
                    }
                })
            }
        }

        eventCustomTimerTest.callback = func

        makeEvents()
    }

    private fun makeEvents(){
        EventManager.onEvent("hurt", { args ->
            var name = (args[0]) as String
            val amt = ((args[1]) as String).toInt()

            val person = GameStats.groupManager.getPerson(name)!!
            person.health -= amt
            if(person.health <= 0)
                GameStats.groupManager.killPerson(name)

            gui.buildGroupTable()
        })

        EventManager.onEvent("die", { args ->
            var name = (args[0]) as String

            GameStats.groupManager.killPerson(name)
            gui.buildGroupTable()
        })

        EventManager.onEvent("heal", { args ->
            var name = (args[0]) as String
            val amt = ((args[1]) as String).toInt()

            val player = GameStats.groupManager.getPerson(name)!!;

            player.health += amt;
            if(player.health >= 100) player.health = 100;

            gui.buildGroupTable()
        })
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

    override fun resume() {
        //throw UnsupportedOperationException()
    }

    override fun render(delta: Float) {
        if(!paused && state == State.TRAVELING) update(delta)

        TextGame.batch.begin()
        draw(TextGame.batch)
        TextGame.batch.end()

        gui.update(delta)
        TextGame.stage.draw()
    }

    private fun draw(batch: SpriteBatch){
        batch.color = Color.WHITE

        batch.draw(backgroundSky, -400f, -backgroundSky.height.toFloat() + Gdx.graphics.height/2f + (backgroundSky.height - Gdx.graphics.height)*currPosOfBackground)

        batch.draw(sunMoon, -400f, -sunMoon.height.toFloat()/1.32f, sunMoon.width.toFloat()/2, sunMoon.height.toFloat()/2, sunMoon.width.toFloat(), sunMoon.height.toFloat(), 1f, 1f, MathUtils.radiansToDegrees* currPosOfSun,
                0, 0, sunMoon.width, sunMoon.height, false, true)

        if(state == State.TRAVELING)
            drawTravelScreen(batch)
        else if(state == State.CAMP)
            drawCampScreen(batch)
    }

    private fun drawTravelScreen(batch: SpriteBatch){
        val value = currPosOfBackground.clamp(0.3f, 1f)

        val color = Color(value, value, value, 1f)
        batch.color = color

        for(i in scrollingBackgroundList.indices) {
            val background = scrollingBackgroundList[i]
            background.draw(batch, color)

            //To draw the ROV in the right area, we have to draw when i == 3 (after both the midgrounds). This lets it be
            //under the foreground but above the midground.
            if(i == 3){
                val shaking = (GameStats.TimeInfo.totalTimeCounter%0.5f).toFloat()*2f
                batch.draw(ROV, -ROV.width/2f, -Gdx.graphics.height/3f + shaking)
            }
        }

    }

    private fun drawCampScreen(batch: SpriteBatch){
        batch.draw(campScreenBackground, -Gdx.graphics.width/2f, -Gdx.graphics.height/2f)

    }

    private fun update(delta:Float){
        GameStats.update(delta)
        eventCustomTimerTest.update(delta)

        //the -MathUtils.PI/2f is to offset the value to 0. Since sine goes to -1 and 1 but normalize it 0 - 1, the initial value will be 0.5 and we don't want that!
        currPosOfBackground = (MathUtils.sin((((GameStats.TimeInfo.totalTimeCounter)/(GameStats.TimeInfo.timeScale/2f))* MathUtils.PI).toFloat() - MathUtils.PI/2f).toFloat() + 1f)/2f
        currPosOfSun = ((-GameStats.TimeInfo.totalTimeCounter)/(GameStats.TimeInfo.timeScale/2f)).toFloat()* MathUtils.PI

        for(background in scrollingBackgroundList)
            background.update(delta)
    }

    public fun onTimeTick(delta:Float){
        totalDistTraveled += currMPH

        GameStats.updateTimeTick()

        gui.updateOnTimeTick(delta) //GUI should be last thing updated since it relies on everything else.
    }

    fun Float.clamp(min:Float, max:Float):Float{
        if(this <= min) return min
        if(this >= max) return max
        return this
    }

    fun pauseGame(){
        this.paused = true
    }

    fun resumeGame(){
        this.paused = false;
    }

    override fun dispose() {
        //throw UnsupportedOperationException()
    }
}