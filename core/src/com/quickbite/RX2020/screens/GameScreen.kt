package com.quickbite.rx2020.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.*
import com.quickbite.rx2020.gui.GameScreenGUI
import com.quickbite.rx2020.managers.*
import com.quickbite.rx2020.util.GH
import com.quickbite.rx2020.util.Logger
import com.quickbite.rx2020.util.Tester
import java.util.*

/**
 * Created by Paha on 2/3/2016.
 */
class GameScreen(val game: TextGame): Screen {
    enum class State{
        TRAVELING, CAMP
    }

    var state = State.TRAVELING

    val timeTickEventList: LinkedList<ChainTask> = LinkedList()

    private val backgroundSky = TextGame.manager.get("backgroundSky", Texture::class.java)
    private val sunMoon = TextGame.manager.get("sunMoon", Texture::class.java)

    private val scrollingBackgroundList:MutableList<ScrollingBackground> = arrayListOf()

    private var ROV: Texture = TextGame.manager.get("Exomer751ROV", Texture::class.java)

    private var currPosOfBackground:Float = 0f
    private var currPosOfSun:Float = 0f

    private val commonEventTime = object {val min=12f; val max = 36f}
    private val rareEventTime = object {val min=84f; val max = 252f}
    private val epicEventTime = object {val min=360f; val max = 1080f}

    private val commonEventTimer: CustomTimer = CustomTimer(MathUtils.random(commonEventTime.min, commonEventTime.max).toFloat())
    private val rareEventTimer: CustomTimer = CustomTimer(MathUtils.random(rareEventTime.min, rareEventTime.max).toFloat())
    private val epicEventTimer: CustomTimer = CustomTimer(MathUtils.random(epicEventTime.min, epicEventTime.max).toFloat())

    private val purgeRecentChangeTimer: CustomTimer = CustomTimer(3f)

    var paused = false
        get
        private set


    private val gameInput: GameScreenInput = GameScreenInput()

    var searchActivity:DataManager.SearchActivityJSON? = null
    var searchFunc:Array<(()->Unit)?>? = null

    var numHoursToAdvance:Int = 0
    var speedToAdvance:Float = 0.1f

    companion object{
        var currGameTime:Double = 0.0
        lateinit var gui: GameScreenGUI
    }

    init{
        gui = GameScreenGUI(this)
        GameStats.init(this)

        gui.init()
        EventManager.init(this)

        sunMoon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        val multi: InputMultiplexer = InputMultiplexer()
        multi.addProcessor(TextGame.stage)
        multi.addProcessor(gameInput)
        Gdx.input.inputProcessor = multi

        val sc1: ScrollingBackground = ScrollingBackground(null, 3f, -100f, -TextGame.camera.viewportHeight / 2f)
        val sc2: ScrollingBackground = ScrollingBackground(null, 3f, 800f, -TextGame.camera.viewportHeight / 2f)
        sc1.following = sc2
        sc2.following = sc1

        val sc3: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Midground2", Texture::class.java)), 2f, -100f, -TextGame.camera.viewportHeight / 2f)
        val sc4: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Midground2", Texture::class.java)), 2f, 800f, -TextGame.camera.viewportHeight / 2f)
        sc3.following = sc4
        sc4.following = sc3

        val sc5: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Background2", Texture::class.java)), 0.2f, -100f, -TextGame.camera.viewportHeight / 2f)
        val sc6: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Background2", Texture::class.java)), 0.2f, 800f, -TextGame.camera.viewportHeight / 2f)
        sc5.following = sc6
        sc6.following = sc5

        scrollingBackgroundList.add(sc6)
        scrollingBackgroundList.add(sc5)
        scrollingBackgroundList.add(sc4)
        scrollingBackgroundList.add(sc3)
        scrollingBackgroundList.add(sc2)
        scrollingBackgroundList.add(sc1)

        gameInput.keyEventMap.put(Input.Keys.E, {gui.triggerEventGUI(GameEventManager.getAndSetEvent("RefreshBerry", "common"))})

        commonEventTimer.callback = timerFunc("common", commonEventTimer, commonEventTime.min, commonEventTime.max)
        rareEventTimer.callback = timerFunc("rare", rareEventTimer, rareEventTime.min, rareEventTime.max)
        epicEventTimer.callback = timerFunc("epic", epicEventTimer, epicEventTime.min, epicEventTime.max)

        if(TextGame.testMode) {
            Tester.testEvents(50)
            gui.buildTradeWindow()
            gui.openTradeWindow()
            pauseGame()
        }
    }

    override fun show() {

    }

    override fun hide() {
       SaveLoad.saveGame(true)
    }

    override fun resize(width: Int, height: Int) {
        TextGame.viewport.update(width, height)
    }

    override fun pause() {
        SaveLoad.saveGame(true)
    }

    override fun resume() {

    }

    override fun render(delta: Float) {
        update(delta)

        if(!paused) {
            if (state == State.TRAVELING) travelUpdate(delta)
            else if (state == State.CAMP) campUpdate(delta)
        }

        TextGame.batch.begin()
        draw(TextGame.batch)
        TextGame.batch.end()

        gui.update(delta)
        TextGame.stage.draw()
    }

    /**
     * Draws the general screen for the game.
     * @param batch The SpriteBatch to draw with.
     */
    private fun draw(batch: SpriteBatch){
        batch.color = Color.WHITE

        batch.draw(backgroundSky, -400f, -backgroundSky.height.toFloat() + TextGame.camera.viewportHeight/2f + (backgroundSky.height - TextGame.camera.viewportHeight)*currPosOfBackground)

        batch.draw(sunMoon, -400f, -sunMoon.height.toFloat()/1.32f, sunMoon.width.toFloat()/2, sunMoon.height.toFloat()/2, sunMoon.width.toFloat(), sunMoon.height.toFloat(), 1f, 1f, MathUtils.radiansToDegrees* currPosOfSun,
                0, 0, sunMoon.width, sunMoon.height, false, true)

        drawTravelScreen(batch)
    }

    /**
     * Draws the travel specific screen.
     * @param batch The SpriteBatch to draw with.
     */
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
                val shaking = if(state == State.TRAVELING) (GameStats.TimeInfo.totalTimeCounter%0.5f).toFloat()*2f else 0f
                batch.draw(ROV, -ROV.width/2f, -TextGame.camera.viewportHeight/3f + shaking)
            }
        }
    }

    /**
     * Called every frame but only during the camping state.
     * @param delta Time between frames.
     */
    private fun campUpdate(delta:Float){
        if(numHoursToAdvance > 0){
            GameStats.update(speedToAdvance)
        }
    }

    /**
     * Called every frame but only during the traveling state.
     * @param delta Time between frames.
     */
    private fun travelUpdate(delta:Float){
        GameStats.update(delta)
        commonEventTimer.update(delta)
        rareEventTimer.update(delta)
//        epicEventTimer.update(delta)

        for(background in scrollingBackgroundList)
            background.update(delta)
    }

    /**
     * Called every frame.
     * @param delta Time between frames.
     */
    private fun update(delta:Float){
        //the -MathUtils.PI/2f is to offset the value to 0. Since sine goes to -1 and 1 but normalize it 0 - 1, the initial value will be 0.5 and we don't want that!
        currPosOfBackground = (MathUtils.sin((((GameStats.TimeInfo.totalTimeCounter)/(GameStats.TimeInfo.timeScale/2f))* MathUtils.PI).toFloat() - MathUtils.PI/2f).toFloat() + 1f)/2f
        currPosOfSun = ((-GameStats.TimeInfo.totalTimeCounter)/(GameStats.TimeInfo.timeScale/2f)).toFloat()* MathUtils.PI

        purgeRecentChangeTimer.update(delta)
        if(purgeRecentChangeTimer.expired){
            Result.purgeRecentResults(currGameTime)
            purgeRecentChangeTimer.reset()
        }

        currGameTime+=delta;
    }

    /**
     * Called on every tick of a new game hour.
     * @param delta Time between frames.
     */
    fun onHourTick(delta:Float){
        GameStats.updateHourly(delta)
        SupplyManager.updateHourly(delta)
        GroupManager.updateHourly(delta)
        ChainTask.updateHourly(delta)

        if(Result.recentDeathMap.size > 0)
            gui.triggerEventGUI(GameEventManager.getEvent("Death", "special"))

        if(numHoursToAdvance > 0) numHoursToAdvance--
        timeTickEventList.forEach { evt -> evt.update()}

        gui.updateOnTimeTick(delta) //GUI should be last thing updated since it relies on everything else.
    }

    /**
     * Changes the game state to camping mode.
     */
    fun changeToCamp(){
        this.state = State.CAMP
        this.ROV = TextGame.manager.get("NewCamp", Texture::class.java)
    }

    /**
     * Changes the game state to travelling mode.
     */
    fun changeToTravel(){
        this.state = State.TRAVELING
        this.ROV = TextGame.manager.get("Exomer751ROV", Texture::class.java)
    }

    /**
     * Pauses the game.
     */
    fun pauseGame(){
        this.paused = true
        gui.pauseButton.isDisabled = true
    }

    /**
     * Resumes the game.
     */
    fun resumeGame(){
        this.paused = false;
        gui.pauseButton.isDisabled = false
    }

    fun setGameOver(){
        pauseGame()
        SaveLoad.deleteSave()
        var counter = 0
        var opacity = 0f
        var whitePixel = TextGame.smallGuiAtlas.findRegion("pixelWhite")
        val task = ChainTask({opacity < 1}, {
            counter++
            opacity = GH.lerpValue(opacity, 0f, 1f, 1f)
            TextGame.batch.color = Color(1f, 0f, 0f, opacity)
            TextGame.batch.begin()
            TextGame.batch.draw(whitePixel, -TextGame.viewport.screenWidth/2f, -TextGame.viewport.screenHeight/2f, TextGame.viewport.screenWidth.toFloat(), TextGame.viewport.screenHeight.toFloat())
            TextGame.batch.end()
            TextGame.batch.color = Color.WHITE
        }, {
            TextGame.stage.clear() //Make sure to clear the stage
            game.screen = MainMenuScreen(game)
        })

        ChainTask.addTaskToEveryFrameList(task)
    }


    override fun dispose() {

    }

    fun timerFunc(eventType:String, timer:CustomTimer, min:Float, max:Float):()->Unit{
        var func: (()->Unit)? = null
        func = {
            //Get the current event or a new one if we aren't on an event.
            var currEvent = GameEventManager.setNewRandomRoot(eventType);
            GameEventManager.currActiveEvent = currEvent;

            Logger.log("GameScreen", "Starting event ${GameEventManager.currActiveEvent!!.name}")

            //Trigger the GUI UI and send a callback to it.
            gui.triggerEventGUI(currEvent)

            timer.reset(min, max)
        }

        return func
    }

}
