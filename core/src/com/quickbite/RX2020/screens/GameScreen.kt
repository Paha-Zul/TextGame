package com.quickbite.rx2020.screens

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.*
import com.quickbite.rx2020.managers.*
import com.quickbite.rx2020.util.Tester
import java.util.*

/**
 * Created by Paha on 2/3/2016.
 */
class GameScreen(val game: Game): Screen {
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

    private var paused = false

    private val gui: GameScreenGUI = GameScreenGUI(this)

    private val gameInput: GameScreenInput = GameScreenInput()

    private var currGameTime:Double = 0.0

    var searchActivity:DataManager.SearchActivityJSON? = null
    var searchFunc:Array<(()->Unit)?>? = null

    var numHoursToAdvance:Int = 0
    var speedToAdvance:Float = 0.1f

    override fun show() {
        GameStats.init(this)
        gui.init()

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

        //gameInput.keyEventMap.put(Input.Keys.E, {gui.triggerEventGUI(DataManager.rootEventMap["Event1"]!!); paused = true})

        commonEventTimer.callback = timerFunc("common", commonEventTimer, commonEventTime.min, commonEventTime.max)
        rareEventTimer.callback = timerFunc("rare", rareEventTimer, rareEventTime.min, rareEventTime.max)
        epicEventTimer.callback = timerFunc("epic", epicEventTimer, epicEventTime.min, epicEventTime.max)

        makeEvents()

        if(TextGame.testMode) {
            Tester.testEvents(50)

            gui.buildTradeWindow()
            gui.openTradeWindow()
            pauseGame()
        }
    }

    /**
     * Adds events to the EventSystem to be called later.
     */
    private fun makeEvents(){
        //TODO Need a 'wait' event.

        //We are hurting a person/people
        EventManager.onEvent("hurt", { args ->
            if(GameEventManager.currActiveEvent == null)
                return@onEvent

            var name = GameEventManager.currActiveEvent!!.randomName
            val min = ((args[0]) as String).toInt()
            val max = if(args.count() >= 2) ((args[1]) as String).toInt() else min
            val perc = if(args.count() >= 3) ((args[2]) as String).toBoolean() else false
            var numPeople = if(args.count() >= 4) ((args[3]) as String).toInt() else 1
            if(numPeople == -1) numPeople = GroupManager.numPeopleAlive
            numPeople.clamp(0, GroupManager.numPeopleAlive)

            var randomPerPerson = if(args.count() >= 5) ((args[4]) as String).toBoolean() else false

            //If we are applying to all the people...
            if(numPeople == GroupManager.numPeopleAlive){
                var dmg = MathUtils.random(min, max)
                val list = GroupManager.getPeopleList()
                list.forEach { person ->
                    if(randomPerPerson) dmg = MathUtils.random(min, max)
                    var res = 0
                    if (perc)
                        res = person.addPercentHealth(-(dmg.toFloat())).toInt()
                    else
                        res = person.addHealth(-(dmg.toFloat())).toInt()

                    Result.addResult(person.firstName, res.toFloat(), currGameTime, "'s HP", gui)
                }

                //If we are doing it to multiple people...
            }else if(numPeople > 1){
                //TODO no use for it yet...

            //For only one person...
            }else{
                var amt = MathUtils.random(min, max)
                val person = GroupManager.getPerson(name)!!
                if(perc)
                    amt = person.addPercentHealth(-(amt.toFloat())).toInt()
                else
                    amt = person.addHealth(-(amt.toFloat())).toInt()

                Result.addResult(person.firstName, amt.toFloat(), currGameTime, "'s HP", gui)
            }
        })

        EventManager.onEvent("heal", { args ->
            val min = ((args[0]) as String).toInt()
            val max = ((args[1]) as String).toInt()

            val person = GroupManager.getPerson(GameEventManager.currActiveEvent!!.randomName)!!;

            val amt = MathUtils.random(min, max);
            person.addHealth(amt.toFloat())

            Result.addResult(person.firstName, amt.toFloat(), currGameTime, "'s HP", gui)
        })

        EventManager.onEvent("addRndAmt", {args ->
            val min:Float = (args[0] as String).toFloat() //The min amt
            val max:Float = (args[1] as String).toFloat() //The max amt
            val supplyName:String = args[2] as String //The name of the supply
            val perPerson = if(args.count() >= 4) (args[3] as String).toBoolean() else false //If it is per person
            val chance = if(args.count() >= 5) (args[4] as String).toFloat() else 100f //Chance to happen

            val rand = MathUtils.random(100)
            if(rand <= chance) {
                var num = MathUtils.random(Math.abs(min), Math.abs(max))
                if (min < 0 || max < 0) num = -num
                if(perPerson) num *= GroupManager.numPeopleAlive

                SupplyManager.addToSupply(supplyName, num.toFloat())
            }
        })

        EventManager.onEvent("addRndItem", {args ->
            val min:Float = (args[0] as String).toFloat()
            val max:Float = (args[1] as String).toFloat()
            val chance = (args[2] as String).toFloat()
            val list:List<Any> = args.subList(3, args.size-1)

            if(MathUtils.random(100) <= chance) {
                val randomSupply = list[MathUtils.random(list.size - 1)] as String
                var num = MathUtils.random(Math.abs(min), Math.abs(max))

                if (min < 0 || max < 0) num = -num
                SupplyManager.addToSupply(randomSupply, num.toFloat())
            }
        })

        EventManager.onEvent("rest", {args ->
            val amt = (args[0] as String).toFloat()
            val chance = if(args.size >= 2) (args[0] as String).toFloat() else 100f

            val rnd = MathUtils.random(100)

            if(chance >= rnd) {
                GroupManager.getPeopleList().forEach { person ->
                    person.addHealth(amt)
                    Result.addResult(person.firstName, amt.toFloat(), currGameTime, "'s HP", gui)
                }
            }
        })

        EventManager.onEvent("damageROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if(args.count() >= 3) (args[2] as String).toFloat() else 0f

            val rand = MathUtils.random(1, 100)
            if(rand <= chance) {
                val amt = -MathUtils.random(min, max)
                ROVManager.addHealthROV(amt)

                Result.addResult("ROV", amt.toFloat(), currGameTime, "'s HP", gui)
            }
        })

        EventManager.onEvent("repairROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if(args.count() >= 3) (args[2] as String).toFloat() else 100f

            if(MathUtils.random(100) <= chance) {
                val amt = MathUtils.random(min, max)

                ROVManager.addHealthROV(amt)

                Result.addResult("ROV", amt.toFloat(), currGameTime, "'s HP", gui)
            }
        })

        EventManager.onEvent("cutMiles", {args ->
            val min = (args[0] as String).toInt()
            val max = if(args.count() >= 2) (args[1] as String).toInt() else min

            val amt = MathUtils.random(min, max)

            GameStats.TravelInfo.totalDistTraveled += amt

            Result.addResult("miles", -amt.toFloat(), currGameTime, gui = gui)
        })

        EventManager.onEvent("openTrade", {args ->
            gui.buildTradeWindow()
            gui.openTradeWindow()
        })

        EventManager.onEvent("death", { args ->
            val person = args[0] as Person

            gui.buildGroupTable()

            Result.addDeath(person)
        })

        EventManager.onEvent("healthChanged", {args ->
            val person = args[0] as Person
            val amt = args[1] as Float

            gui.buildGroupTable()

            Result.addResult(person.firstName, amt, currGameTime, "'s HP", gui)
        })

        EventManager.onEvent("supplyChanged", { args ->
            val supply = args[0] as SupplyManager.Supply
            val amt = args[1] as Float

            Result.addResult(supply.displayName, amt, currGameTime, "", gui)
        })
    }

    override fun hide() {
    }

    override fun resize(width: Int, height: Int) {
        TextGame.viewport.update(width, height)
    }

    override fun pause() {
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
        epicEventTimer.update(delta)

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
    }

    /**
     * Resumes the game.
     */
    fun resumeGame(){
        this.paused = false;
    }

    override fun dispose() {
        //throw UnsupportedOperationException()
    }

    fun timerFunc(eventType:String, timer:CustomTimer, min:Float, max:Float):()->Unit{
        val func = {
            var currEvent = GameEventManager.currActiveEvent
            if(currEvent == null) currEvent = GameEventManager.setNewRandomRoot(eventType);
            GameEventManager.currActiveEvent = currEvent;

            gui.triggerEventGUI(currEvent, { choice ->

                //If the list has a resulting action, call it!
                val list = currEvent!!.resultingAction;
                var exit:Boolean = true

                Result.clearResultLists()
                //We are about to perform some actions and generate results for the event.
                if(list != null && list.size != 0) {
                    //Call each event through the event manager.
                    for (l in list.iterator()) {
                        if (l.size > 0) {
                            exit = false
                            EventManager.callEvent(l[0], l.slice(1.rangeTo(l.size-1)))
                        }
                    }

                    //Display the event results to the player.
                    gui.showEventResults(Result.eventResultMap.values.toList(), Result.deathResultMap.values.toList())
                }

                //Get event related to the choice we selected.
                currEvent = currEvent!!.selectChildEvent(choice)
                //If the event is not null, restart the timer to make the next event display.
                if(currEvent != null){
                    timer.restart(0.00001f)
                    if(exit){
                        resumeGame()
                        gui.closeEvent()
                    }

                    GameEventManager.currActiveEvent = currEvent
                //Otherwise if it is null, we need to pick a new root.
                }else{
                    if(exit){
                        resumeGame()
                        gui.closeEvent()
                    }
                    timer.restart(MathUtils.random(min, max).toFloat())
                    GameEventManager.setNewRandomRoot(eventType)
                }

            })
        }

        return func
    }
}

fun Float.clamp(min:Float, max:Float):Float{
    if(this <= min) return min
    if(this >= max) return max
    return this
}

fun Int.clamp(min:Int, max:Int):Int{
    if(this <= min) return min
    if(this >= max) return max
    return this
}