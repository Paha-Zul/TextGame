package com.quickbite.rx2020.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.GameScreenInput
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.clamp
import com.quickbite.rx2020.gui.GameScreenGUI
import com.quickbite.rx2020.gui.GroupGUI
import com.quickbite.rx2020.gui.SupplyGUI
import com.quickbite.rx2020.gui.ROVPartsGUI
import com.quickbite.rx2020.managers.*
import com.quickbite.rx2020.managers.ScrollingBackgroundManager.scrollingBackgroundList
import com.quickbite.rx2020.util.*
import java.util.*

/**
 * Created by Paha on 2/3/2016.
 *
 * The main game screen
 */
class GameScreen(val game: TextGame, val loaded:Boolean = false): Screen {
    enum class State{
        TRAVELING, CAMP, GAMEOVER
    }

    var state = State.TRAVELING

    val timeTickEventList: LinkedList<ChainTask> = LinkedList()

    private val backgroundSky = TextGame.manager.get("backgroundSky", Texture::class.java)
    private val sunMoon = TextGame.manager.get("sunMoon", Texture::class.java)

    private var ROV: Texture = TextGame.manager.get("Exomer751ROV", Texture::class.java)

    private var currPosOfBackground:Float = 0f
    private var currPosOfSun:Float = 0f

    private val dailyEventTime = object {val min=12f; val max = 36f}
    private val weeklyEventTime = object {val min=84f; val max = 252f}
    private val MonthlyEventTime = object {val min=360f; val max = 1080f}

    private val dailyEventTimer: CustomTimer = CustomTimer(MathUtils.random(dailyEventTime.min, dailyEventTime.max).toFloat())
    private val weeklyEventTimer: CustomTimer = CustomTimer(MathUtils.random(weeklyEventTime.min, weeklyEventTime.max).toFloat())
    private val MonthlyEventTimer: CustomTimer = CustomTimer(MathUtils.random(MonthlyEventTime.min, MonthlyEventTime.max).toFloat())
    private val MonthlyNativeEventTimer: CustomTimer = CustomTimer(MathUtils.random(MonthlyEventTime.min, MonthlyEventTime.max).toFloat())

    val timerList:List<Pair<String, CustomTimer>>
        get() = listOf(Pair("daily", dailyEventTimer), Pair("weekly", weeklyEventTimer), Pair("monthly", MonthlyEventTimer),
                Pair("monthlyNative", MonthlyNativeEventTimer))

    val noticeEventTimer: CustomTimer = CustomTimer(MathUtils.random(1f, 1f)) //Used to trigger notices.

    private val purgeRecentChangeTimer: CustomTimer = CustomTimer(3f, true)

    var paused = false
        get
        private set

    private val gameInput: GameScreenInput = GameScreenInput()

    /** The SearchActivity that will be performed every hour*/
    var searchActivity:DataManager.SearchActivityJSON? = null
    /** The actual function of searching that will be performed every hour*/
    var searchFunc:Array<(()->Unit)?>? = null

    var numHoursToAdvance:Int = 0
    var speedToAdvance:Float = 0.1f

    companion object{
        var currGameTime:Double = 0.0
    }

    init{
        Logger.writeLog("log.txt")

        TextGame.stage.clear()
        fadeIn()

        GameStats.game = this

//        GameScreenGUI.init(this)
        EventManager.init(this)

        setupGUI()

        sunMoon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        val multi: InputMultiplexer = InputMultiplexer()
        multi.addProcessor(TextGame.stage)
        multi.addProcessor(gameInput)
        Gdx.input.inputProcessor = multi

        gameInput.keyEventMap[Input.Keys.E] = {SupplyManager.addToSupply("energy", -25f)}
        gameInput.keyEventMap[Input.Keys.R] = { GameScreenGUI.openEventGUI(GameEventManager.getAndSetEvent("Power", "monthly")!!)}
        gameInput.keyEventMap[Input.Keys.T] = {GameScreenGUI.openEventGUI(GameEventManager.getAndSetEvent("TestEnergy", "special")!!)}
        gameInput.keyEventMap[Input.Keys.Y] = {GameEventManager.addDelayedEvent("Hole", "daily", MathUtils.random(1, 5).toFloat())}

        dailyEventTimer.callback = timerFunc("daily", dailyEventTimer, dailyEventTime.min, dailyEventTime.max)
        weeklyEventTimer.callback = timerFunc("weekly", weeklyEventTimer, weeklyEventTime.min, weeklyEventTime.max)
        MonthlyEventTimer.callback = timerFunc("monthly", MonthlyEventTimer, MonthlyEventTime.min, MonthlyEventTime.max)
        MonthlyNativeEventTimer.callback = timerFunc("monthlyNative", MonthlyEventTimer, MonthlyEventTime.min, MonthlyEventTime.max)

        noticeEventTimer.stop()

        //If we are testing, run the tests!
        if(Tester.TESTING) {
            Tester.testEvents(50)
            GameScreenGUI.buildTradeWindow()
            GameScreenGUI.openTradeWindow()
            pauseGame()
        }

        if(loaded)
            loadGame()
    }

    private fun setupGUI(){
        val groupTable = GroupGUI.init()
        val rovPartsTable = ROVPartsGUI.init()
        val supplyTable = SupplyGUI.init()

        val leftTable = Table()
        leftTable.add(supplyTable)

        val rightTable = Table()
        rightTable.add(groupTable)
        rightTable.row()
        rightTable.add().expandY().fillY()
        rightTable.row()
        rightTable.add(rovPartsTable)

        val mainTable = Table()
        mainTable.add(leftTable)
        mainTable.add().expandX().fillX()
        mainTable.add(rightTable).expandY().fillY()

        mainTable.setFillParent(true)
        TextGame.stage.addActor(mainTable)
    }

    private fun fadeIn(){
        TextGame.backgroundColor.a = 0f
        TextGame.batch.color = Color(0f,0f,0f,0f)
        val blackPixel = TextGame.smallGuiAtlas.findRegion("pixelBlack")

        //Overlays a solid black rectangle and fades it out
        val task = ChainTask({TextGame.backgroundColor.r < 1},
            {
                TextGame.batch.begin()
                val amt = TextGame.backgroundColor.r + 0.01f
                TextGame.backgroundColor.r = amt; TextGame.backgroundColor.g=amt; TextGame.backgroundColor.b=amt; TextGame.backgroundColor.b=amt; TextGame.backgroundColor.a=amt
                TextGame.batch.color = Color(0f, 0f, 0f, (1-amt))
                TextGame.batch.draw(blackPixel, -TextGame.viewport.screenWidth/2f, -TextGame.viewport.screenHeight/2f, TextGame.viewport.screenWidth.toFloat(), TextGame.viewport.screenHeight.toFloat())
                TextGame.batch.end()
            })
        ChainTask.addTaskToEveryFrameList(task)
    }

    private fun loadGame(){
        SaveLoad.loadGame(this)
    }

    private fun newGame(){

    }

    override fun show() {

    }

    override fun hide() {
        Logger.writeLog("log.txt")
       SaveLoad.saveGame(true, this)
    }

    override fun resize(width: Int, height: Int) {
        TextGame.viewport.update(width, height)
    }

    /**
     * Called when the game is paused either by code or by an event (minimized game on android etc...)
     */
    override fun pause() {
        if(this.state != GameScreen.State.GAMEOVER) SaveLoad.saveGame(false, this)
        Logger.writeLog("log.txt")
    }

    /**
     * Called when the game resumes.
     */
    override fun resume() {
        if(this.state != GameScreen.State.GAMEOVER) SaveLoad.saveGame(false, this)
        Logger.writeLog("log.txt")
    }


    /**
     * Called to pause the game.
     */
    fun pauseGame(){
        this.paused = true
        this.pause()
    }

    /**
     * Called to resume the game. We separate this call from resume() because we don't want the
     * game necessarily resuming when the system decides to resume the game (like when you open the
     * tab again on android)
     */
    fun resumeGame(){
        this.paused = false;
        this.resume()
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

        GameScreenGUI.update(delta)
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

        //We do the drawing for the background here (in GameScreen) because we have to mix in the vehicle drawing
        for(i in ScrollingBackgroundManager.scrollingBackgroundList.indices) {
            val background = ScrollingBackgroundManager.scrollingBackgroundList[i]
            background.draw(batch, color)

            //To draw the ROV in the right area, we have to draw when i == 3 (after both the midgrounds). This lets it be
            //under the foreground but above the midground.
            if(i == 5){
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
     * Called every frame but only during the traveling state. Only runs when not paused.
     * @param delta Time between frames.
     */
    private fun travelUpdate(delta:Float){
        GameStats.update(delta)
        GameEventManager.update(delta) //Used for event timers and such.
        dailyEventTimer.update(delta)
        weeklyEventTimer.update(delta)
        MonthlyEventTimer.update(delta)
        MonthlyNativeEventTimer.update(delta)

        noticeEventTimer.update(delta)

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
        if(purgeRecentChangeTimer.done){
            ResultManager.purgeRecentResults(currGameTime)
            purgeRecentChangeTimer.restart()
        }

        currGameTime+=delta
    }

    /**
     * Called on every tick of a new game hour.
     * @param delta Time between frames.
     */
    fun onHourTick(delta:Float){
        //TODO Need to implement when you can't travel and need to camp.

        val gameOver = GH.checkGameOverConditions()
        //If gameOver is true, set the game as over
        if(gameOver.first) {
            setGameOver(gameOver.second)

        //If we made it to the end, we've won!
        }else if(GameStats.TravelInfo.totalDistToGo <= 0) {
            GameStats.gameOverStatus = "won"
            GameScreenGUI.openEventGUI(GameEventManager.getAndSetEvent("EndWin", "special")!!)

        //Otherwise, update everything as normal
        }else {
            GameStats.updateHourly(delta)
            SupplyManager.updateHourly(delta)
            GroupManager.updateHourly(delta)
            ChainTask.updateHourly(delta)

            if (ResultManager.recentDeathMap.isNotEmpty()) {
                GameScreenGUI.openEventGUI(GameEventManager.getAndSetEvent("Death", "special")!!)
            }

            if (numHoursToAdvance > 0) numHoursToAdvance--
            timeTickEventList.forEach { evt -> evt.update() }

            GameScreenGUI.updateOnTimeTick(delta) //GUI should be last thing updated since it relies on everything else.
            SupplyGUI.updateSuppliesGUI()
        }
    }

    /**
     * Changes the game state to camping mode.
     */
    fun changeToCamp(){
        if(this.state != State.CAMP) {
            this.state = State.CAMP
            this.ROV = TextGame.manager.get("NewCamp", Texture::class.java)
            GameScreenGUI.openCampMenu()
            this.resumeGame()
        }
    }

    /**
     * Changes the game state to travelling mode.
     */
    fun changeToTravel(){
        this.state = State.TRAVELING
        this.ROV = TextGame.manager.get("Exomer751ROV", Texture::class.java)
    }

    fun setGameOver(reason:String){
        GameStats.gameOverStatus = reason
        this.state = State.GAMEOVER
        GameScreenGUI.openEventGUI(GameEventManager.getAndSetEvent("EndLose", "special")!!)
    }


    override fun dispose() {

    }

    /**
     * @param eventType The type of event, ie: "daily"
     * @param timer The timer that we are attaching this function to. This is so we can restart it and stuff.
     * @param min The minimum time to random
     * @param max The maximum time to random.
     * @return The function that was made.
     */
    fun timerFunc(eventType:String, timer: CustomTimer, min:Float, max:Float):()->Unit{

        return {
            //Get the current event or a new one if we aren't on an event.
            val currEvent = GameEventManager.setNewRandomRoot(eventType)

            Logger.log("GameScreen", "Starting event ${GameEventManager.currActiveEvent!!.name}")

            if(currEvent == null) Logger.log("GameScreen", "Event skipped because it was null. Tried to get $eventType event.", Logger.LogLevel.Warning)
            else //Trigger the GUI UI and send a callback to it.
                GameScreenGUI.openEventGUI(currEvent)

            timer.restart(MathUtils.random(min, max))
        }
    }

    /**
     * Used mostly for setting the timers when the game is loaded.
     * @param eventType The type of event
     * @param time The exact time to set the timer for.
     */
    fun setTimer(eventType:String, time:Float){
        val timer:CustomTimer
        val callback:() -> Unit
        when(eventType){
            "daily" -> {timer = dailyEventTimer; callback = timerFunc(eventType, timer, dailyEventTime.min, dailyEventTime.max)}
            "weekly" -> {timer = weeklyEventTimer; callback = timerFunc(eventType, timer, weeklyEventTime.min, weeklyEventTime.max)}
            "monthly" -> {timer = MonthlyEventTimer; callback = timerFunc(eventType, timer, MonthlyEventTime.min, MonthlyEventTime.max)}
            else -> {timer = MonthlyNativeEventTimer; callback = timerFunc(eventType, timer, MonthlyEventTime.min, MonthlyEventTime.max)}
        }

        timer.callback = callback
        timer.restart(time)
    }

}
