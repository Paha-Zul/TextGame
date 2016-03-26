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
import com.quickbite.game.*
import com.quickbite.game.managers.*
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

    private val backgroundSky = Texture(Gdx.files.internal("art/backgroundSky.png"), true)
    private val sunMoon = Texture(Gdx.files.internal("art/sunMoon.png"), true)

    private val scrollingBackgroundList:MutableList<ScrollingBackground> = arrayListOf()

    private var ROV: Texture = TextGame.manager.get("Exomer751ROV", Texture::class.java)

    private var currPosOfBackground:Float = 0f
    private var currPosOfSun:Float = 0f

    private val eventCustomTimerTest: CustomTimer = CustomTimer(null, MathUtils.random(15, 30).toFloat())

    private var paused = false

    private val gui: GameScreenGUI = GameScreenGUI(this)

    private val gameInput: GameScreenInput = GameScreenInput()

    private var currEvent: DataManager.EventJson? = DataManager.EventJson.getRandomRoot()

    private var resultsList:MutableList<Pair<Int, String>> = arrayListOf()

    var searchActivity:DataManager.SearchActivityJSON? = null
    var searchFunc:(()->Unit)? = null

    var numHoursToAdvance:Int = 0
    var speedToAdvance:Float = 0.1f

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

        val func = {
            if(currEvent!=null){
                val _evt = currEvent as DataManager.EventJson
                gui.triggerEventGUI(_evt, { choice ->

                    //If the list has a resulting action, call it!
                    val list = currEvent!!.resultingAction;
                    var exit:Boolean = true
                    if(list != null) {
                        resultsList.clear()
                        for (l in list.iterator()) {
                            if (l.size > 0) {
                                exit = false
                                EventManager.callEvent(l[0], l.slice(1.rangeTo(l.size-1)))
                            }
                        }
                        gui.showEventResults(resultsList)
                    }

                    currEvent = _evt.select(choice, MathUtils.random(100))
                    if(currEvent != null){
                        eventCustomTimerTest.restart(0.00001f)
                        if(exit){
                            resumeGame()
                            gui.closeEvent()
                        }

                    //Get a new root sometime.
                    }else{
                        if(exit){
                            resumeGame()
                            gui.closeEvent()
                        }
                        currEvent = DataManager.EventJson.getRandomRoot()
                        eventCustomTimerTest.restart(MathUtils.random(15, 30).toFloat())
                    }
                })
            }
        }

        eventCustomTimerTest.callback = func

        makeEvents()

//        gui.buildTradeWindow()
//        gui.openTradeWindow()
//        pauseGame()
    }

    /**
     * Adds events to the EventSystem to be called later.
     */
    private fun makeEvents(){

        //We are hurting a person/people
        EventManager.onEvent("hurt", { args ->
            var name = currEvent!!.randomName
            val min = ((args[0]) as String).toInt()
            val max = if(args.count() >= 2) ((args[1]) as String).toInt() else min
            val perc = if(args.count() >= 3) ((args[2]) as String).toBoolean() else false
            var numPeople = if(args.count() >= 4) ((args[3]) as String).toInt() else 1
            if(numPeople == -1) numPeople = GroupManager.numPeopleAlive

            //If we are applying to all the people...
            if(numPeople == GroupManager.numPeopleAlive){
                val amt = MathUtils.random(min, max)
                val list = GroupManager.getPeopleList()
                list.forEach { person ->
                    var res = 0
                    if(perc)
                        res = person.addPercentHealth(-(amt.toFloat())).toInt()
                    else
                        res = person.addHealth(-(amt.toFloat())).toInt()
                    resultsList.add(Pair(res, person.firstName + "'s HP"))}

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

                resultsList.add(Pair(amt, person.firstName + "'s HP"))
            }

            gui.buildGroupTable()
        })

        EventManager.onEvent("die", { args ->
            var name = (args[0]) as String

            GroupManager.killPerson(name)
            gui.buildGroupTable()
        })

        EventManager.onEvent("heal", { args ->
            var name = (args[0]) as String
            val amt = ((args[1]) as String).toInt()

            val person = GroupManager.getPerson(name)!!;

            person.health += amt;
            if(person.health >= 100) person.health = 100;

            gui.buildGroupTable()
            resultsList.add(Pair(amt, person.firstName + "'s HP"))
        })

        EventManager.onEvent("addRndAmt", {args ->
            val min:Int = (args[0] as String).toInt()
            val max:Int = (args[1] as String).toInt()
            val supplyName:String = args[2] as String
            val perPerson = if(args.count() >= 4) (args[3] as String).toBoolean() else false

            var chance = 100f
            if(args.count() > 4)
                chance = (args[4] as String).toFloat()

            if(MathUtils.random(100) <= chance) {
                var num = MathUtils.random(Math.abs(min), Math.abs(max))
                if (min < 0 || max < 0) num = -num
                if(perPerson) num *= GroupManager.numPeopleAlive

                val supply = SupplyManager.addToSupply(supplyName, num.toFloat())

                resultsList.add(Pair(num, supply.displayName))
            }
        })

        EventManager.onEvent("addRndItem", {args ->
            val min:Float = (args[0] as String).toFloat()
            val max:Float = (args[1] as String).toFloat()
            val list:List<Any> = args.subList(2, args.size-1)

            val randomSupply = list[MathUtils.random(list.size-1)] as String
            var num = MathUtils.random(Math.abs(min), Math.abs(max))

            if(min < 0 || max < 0) num = -num
            val supply = SupplyManager.addToSupply(randomSupply, num.toFloat())

            resultsList.add(Pair(num.toInt(), supply.displayName))
        })

        EventManager.onEvent("rest", {args ->
            val amt = (args[0] as String).toFloat()

            GroupManager.getPeopleList().forEach { person ->
                person.addHealth(amt)
            }
        })

        EventManager.onEvent("damageROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min

            val amt = MathUtils.random(min, max)

            ROVManager.addHealthROV(-amt)
        })

        EventManager.onEvent("repairROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min

            val amt = MathUtils.random(min, max)

            ROVManager.addHealthROV(amt)
        })

        EventManager.onEvent("cutMiles", {args ->
            val min = (args[0] as String).toInt()
            val max = if(args.count() >= 2) (args[1] as String).toInt() else min

            val amt = MathUtils.random(min, max)

            GameStats.TravelInfo.totalDistTraveled += amt
        })

        EventManager.onEvent("openTrade", {args ->
            gui.buildTradeWindow()
            gui.openTradeWindow()
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
        update(delta)
        if(!paused && state == State.TRAVELING) travelUpdate(delta)
        else if(!paused && state == State.CAMP) campUpdate(delta)

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

//        if(state == State.TRAVELING)
//            //drawTravelScreen(batch)
//        else if(state == State.CAMP)
//            //drawCampScreen(batch)
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
        eventCustomTimerTest.update(delta)

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
    }

    /**
     * Called on every tick of a new game hour.
     * @param delta Time between frames.
     */
    fun onHourTick(delta:Float){
        GameStats.updateTimeTick()
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