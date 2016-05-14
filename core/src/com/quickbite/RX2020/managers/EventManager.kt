package com.quickbite.rx2020.managers

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.*
import com.quickbite.rx2020.interfaces.IResetable
import com.quickbite.rx2020.screens.GameOverScreen
import com.quickbite.rx2020.screens.GameScreen
import com.quickbite.rx2020.util.FunGameStats
import com.quickbite.rx2020.util.GH
import com.quickbite.rx2020.util.Logger
import java.util.*

/**
 * Created by Paha on 2/8/2016.
 */
object EventManager : IResetable{
    private var eventMap: HashMap<String, (args: List<Any>) -> Unit> = hashMapOf()

    fun init(gameScreen:GameScreen){
        makeEvents(gameScreen)
    }

    fun onEvent(name:String, event:(args:List<Any>) -> Unit){
        eventMap.put(name, event)
    }

    fun callEvent(name:String, vararg args:Any){
        val func = eventMap[name]
        if(func != null)
            func(args.toList())
        else
            Logger.log("EventSystem", "No event found for $name, is it spelled correctly?")
    }

    fun callEvent(name:String, args:List<Any>){
        val func = eventMap[name]
        if(func != null)
            func(args)
        else
            Logger.log("EventSystem", "No event found for $name, is it spelled correctly?")
    }

    /**
     * Adds events to the EventSystem to be called later.
     */
    private fun makeEvents(gameScreen:GameScreen){
        //TODO Need a 'wait' event.

        //We are hurting a person/people
        EventManager.onEvent("addHealth", { args ->
            var name = (args[0]) as String
            val min = ((args[1]) as String).toInt()
            val max = if(args.count() >= 3) ((args[2]) as String).toInt() else min
            val perc = if(args.count() >= 4) ((args[3]) as String).toBoolean() else false
            var numPeople = if(args.count() >= 5) ((args[4]) as String).toInt() else 1
            var randomPerPerson = if(args.count() >= 6) ((args[5]) as String).toBoolean() else false

            if(numPeople == -1) numPeople = GroupManager.numPeopleAlive
            numPeople.clamp(0, GroupManager.numPeopleAlive)

            if(name.matches("evt[0-9]".toRegex())) name = GameEventManager.currActiveEvent!!.randomPersonList[name.last().toString().toInt()].firstName
            else if(name.equals("evt")) name = GameEventManager.currActiveEvent!!.randomPersonList[0].firstName

            //If we are applying to all the people...
            if(numPeople == GroupManager.numPeopleAlive){
                var amt = MathUtils.random(Math.abs(min), Math.abs(max))
                if(min < 0) amt = -amt //If we are dealing with negative numbers, negatize it!
                val list = GroupManager.getPeopleList()
                list.forEach { person ->
                    if(randomPerPerson) {
                        amt = MathUtils.random(Math.abs(min), Math.abs(max))
                        if (min < 0) amt = -amt //If we are dealing with negative numbers, negatize it!
                    }
                    if (perc)
                        person.addPercentHealth(amt.toFloat()).toInt()
                    else
                        person.addHealth(amt.toFloat()).toInt()

                    FunGameStats.addFunStat("Total Health Net", amt.toInt().toString())
                }

                //If we are doing it to multiple people...
            }else if(numPeople > 1){
                //TODO no use for it yet...

            //For only one person...
            }else{
                var amt = MathUtils.random(Math.abs(min), Math.abs(max))
                if(min < 0) amt = -amt //If we are dealing with negative numbers, negatize it!
                val person = GroupManager.getPerson(name)!!
                if(perc)
                    person.addPercentHealth(amt.toFloat()).toInt()
                else
                    person.addHealth(amt.toFloat()).toInt()

                FunGameStats.addFunStat("Total Health Net", amt.toInt().toString())
            }
        })

        EventManager.onEvent("addAilment", {args ->
            val name = args[0] as String
            val type = args[1] as String
            val level = args[2] as String

            var person:Person

            if(name == "rand") person = GroupManager.getRandomPerson()!!                     //We need to do .toString() for the .toInt() method or else it converts the character to ascii value.
            else if(name.matches("evt[0-9]".toRegex())) person = GameEventManager.currActiveEvent!!.randomPersonList[name.last().toString().toInt()]
            else if(name == "evt") person = GameEventManager.currActiveEvent!!.randomPersonList[0]
            else person = GroupManager.getPerson(name)!!

            var disType:Person.Disability.DisabilityType
            var disLevel:Person.Disability.DisabilityLevel

            when(type){
                "sickness" -> disType = Person.Disability.DisabilityType.Sickness
                else -> disType = Person.Disability.DisabilityType.Injury
            }

            when(level){
                "minor" -> disLevel = Person.Disability.DisabilityLevel.Minor
                "regular" -> disLevel = Person.Disability.DisabilityLevel.Regular
                "major" -> disLevel = Person.Disability.DisabilityLevel.Major
                else -> disLevel = Person.Disability.DisabilityLevel.Trauma
            }

            person.addDisability(disLevel, disType)
            FunGameStats.addFunStat("Ailments Inflicted: ", "1")
        })

        //Called to remove and injury from a person.
        EventManager.onEvent("removeAilment", { args ->
            val name = args[0] as String
            val type = args[1] as String

            val person = GroupManager.getPerson(name)!!;
            when(type){
                "worst" -> person.removeWorstDisability()
                else -> person.removeLongestDisability()
            }

            FunGameStats.addFunStat("Ailments Cured: ", "1")
        })

        //Adds a random amount of an item.
        EventManager.onEvent("addRndAmt", {args ->
            try{
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
                    FunGameStats.addFunStat("$supplyName", num.toInt().toString())
                }

                GameScreen.gui.updateSuppliesGUI()
            }catch(e:NumberFormatException){
                e.printStackTrace()
                Logger.log("EventManager", "addRndAmt has some wrong parameters, make sure they are in the order: min/max/supplyName/perPerson/chance")
            }
        })

        //Adds a random item to the supply.
        EventManager.onEvent("addRndItem", {args ->
            try {
                val min: Float = (args[0] as String).toFloat()
                val max: Float = (args[1] as String).toFloat()
                val chance = (args[2] as String).toFloat()
                val list: List<Any> = args.subList(3, args.size)

                if(MathUtils.random(100) <= chance) {
                    val randomSupply = list[MathUtils.random(list.size - 1)] as String
                    var num = MathUtils.random(Math.abs(min), Math.abs(max))

                    if (min < 0 || max < 0) num = -num
                    SupplyManager.addToSupply(randomSupply, num.toFloat())

                    FunGameStats.addFunStat("$randomSupply", num.toInt().toString())
                }

                GameScreen.gui.updateSuppliesGUI()
            }catch(e:NumberFormatException){
                e.printStackTrace()
                Logger.log("EventManager", "addRndItem has some wrong parameters, make sure they are in the order: min/max/chance/supplyNames(args)")
            }
        })

        //A chance to rest. Heals the group memebers.
        EventManager.onEvent("rest", {args ->
            val amt = (args[0] as String).toFloat()
            val chance = if(args.size >= 2) (args[0] as String).toFloat() else 100f

            val rnd = MathUtils.random(100)

            if(chance >= rnd) {
                GroupManager.getPeopleList().forEach { person ->
                    person.addHealth(amt)
                    FunGameStats.addFunStat("Total Health Net", amt.toInt().toString())
                }
            }
        })

        //Called to damage the ROV
        EventManager.onEvent("addHealthROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if(args.count() >= 3) (args[2] as String).toFloat() else 100f

            val rand = MathUtils.random(1, 100)
            if(rand <= chance) {
                var amt = MathUtils.random(Math.abs(min), Math.abs(max))
                if(min < 0) amt = -amt

                ROVManager.addHealthROV(amt)

                FunGameStats.addFunStat("Total ROV Net Health", amt.toInt().toString())
            }
        })

        //Called to damage the ROV
        EventManager.onEvent("damageROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if(args.count() >= 3) (args[2] as String).toFloat() else 100f

            val rand = MathUtils.random(1, 100)
            if(rand <= chance) {
                val amt = -MathUtils.random(min, max)
                ROVManager.addHealthROV(amt)

                FunGameStats.addFunStat("Total ROV Health Net", amt.toInt().toString())
            }
        })

        //Called to repair the ROV
        EventManager.onEvent("repairROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if(args.count() >= 3) (args[2] as String).toFloat() else 100f

            if(MathUtils.random(100) <= chance) {
                val amt = MathUtils.random(min, max)

                ROVManager.addHealthROV(amt)
                FunGameStats.addFunStat("Total ROV Health Net", amt.toInt().toString())
            }
        })

        //Called when miles should be cut or added
        EventManager.onEvent("cutMiles", {args ->
            val min = (args[0] as String).toInt()
            val max = if(args.count() >= 2) (args[1] as String).toInt() else min

            var amt = MathUtils.random(Math.abs(min), Math.abs(max))
            if(min < 0 || max < 0) amt = -amt

            GameStats.TravelInfo.totalDistTraveled += amt

            Result.addRecentChange("miles", -amt.toFloat(), GameScreen.currGameTime, gui = GameScreen.gui, isEventRelated = GameEventManager.currActiveEvent != null)
            FunGameStats.addFunStat("Total Miles Net", amt.toInt().toString())
        })

        //Called when the game should 'wait' or progress some amount of time.
        EventManager.onEvent("wait", {args ->
            val min = (args[0] as String).toInt()
            val max = if(args.count() >= 2) (args[1] as String).toInt() else min

            var amt = MathUtils.random(Math.abs(min), Math.abs(max))

            GameStats.TimeInfo.totalTimeCounter += amt

            Result.addRecentChange("hours waited", amt.toFloat(), GameScreen.currGameTime, gui = GameScreen.gui, isEventRelated = GameEventManager.currActiveEvent != null)
            FunGameStats.addFunStat("Hours Waited", amt.toInt().toString())
        })

        //Called when the trade window should be opened.
        EventManager.onEvent("openTrade", {args ->
            GameScreen.gui.buildTradeWindow()
            GameScreen.gui.openTradeWindow()
        })

        //Called when a person dies.
        EventManager.onEvent("death", { args ->
            val person = args[0] as Person

            GameScreen.gui.buildGroupTable()

            Result.addRecentDeath(person)
            FunGameStats.addFunStat(person.fullName, "dead", true)
        })

        //Takes the recent deaths and puts them into the event deaths for displaying.
        EventManager.onEvent("showDeaths", {args->
            val pair = Result.recentDeathMap.toList()[0]
            Result.recentDeathResult = pair.second      //Store the first death result.
            Result.recentDeathMap.remove(pair.first)    //Remove it from the map.
        })

        //Called when a person's health changed.
        EventManager.onEvent("healthChanged", {args ->
            val person = args[0] as Person
            val amt = args[1] as Float

            GameScreen.gui.buildGroupTable()

            Result.addRecentChange(person.firstName, amt, GameScreen.currGameTime, "'s HP", GameScreen.gui, GameEventManager.currActiveEvent != null)
        })

        //Called when a supply from the SupplyManager has changed. This is called from SupplyManager usually.
        EventManager.onEvent("supplyChanged", { args ->
            val supply = args[0] as SupplyManager.Supply
            val amt = args[1] as Float
            val oldAmt = args[2] as Float

            if(oldAmt > 0 && supply.amt <= 0) {
                val name = GH.checkSupply(supply, amt, oldAmt)
                if (!name.isEmpty()) {
                    gameScreen.noticeEventTimer.callback = { GameScreen.gui.triggerEventGUI(GameEventManager.getAndSetEvent(name, "special")) }
                    gameScreen.noticeEventTimer.restart()
                    gameScreen.noticeEventTimer.start()
                }
            }

            Result.addRecentChange(supply.displayName, amt, GameScreen.currGameTime, "", GameScreen.gui, GameEventManager.currActiveEvent != null)
        })

        //Called when an event starts.
        EventManager.onEvent("eventStarted", { args ->
            //TODO Implementation?
        })

        //Called when an event finishes.
        EventManager.onEvent("eventFinished", { args ->
            if(gameScreen.state != GameScreen.State.GAMEOVER) SaveLoad.saveGame(false)
            GameEventManager.currActiveEvent = null
            Result.purgeEventResults()
        })

        //Called when an event finishes.
        EventManager.onEvent("forceCamp", { args ->
            gameScreen.changeToCamp()
        })

        //Called when the game is over. Shows the game over screen.
        EventManager.onEvent("gameOver", {args ->
            val win = (args[0] as String).toBoolean()

            for(person in GroupManager.getPeopleList())
                FunGameStats.addFunStat(person.fullName, "alive", true)

            GameStats.win = win
            gameScreen.pauseGame()
            gameScreen.state = GameScreen.State.GAMEOVER

            //Lets fade the screen out to white and load the game over screen.
            var opacity = 0f
            val overlay = TextGame.smallGuiAtlas.findRegion("pixelWhite")
            val task = ChainTask({opacity < 1f}, {
                gameScreen.pauseGame()
                opacity = GH.lerpValue(opacity, 0f, 1f, 1f) //Lerp opacity
                TextGame.batch.begin() //Begin the batch
                val color = TextGame.batch.color //Set the color
                TextGame.batch.color = Color(1f, 1f, 1f, opacity) //Make a new color
                TextGame.batch.draw(overlay, -TextGame.viewport.screenWidth.toFloat()/2, -TextGame.viewport.screenHeight.toFloat()/2, TextGame.viewport.screenWidth.toFloat(), TextGame.viewport.screenHeight.toFloat()) //Draw
                TextGame.batch.color = color //Reset color
                TextGame.batch.end() //End batch
            }, {
                TextGame.batch.begin() //Begin the batch
                val color = TextGame.batch.color //Set the color
                TextGame.batch.color = Color(1f, 1f, 1f, 1f) //Make a new color
                TextGame.batch.draw(overlay, -TextGame.viewport.screenWidth.toFloat()/2, -TextGame.viewport.screenHeight.toFloat()/2, TextGame.viewport.screenWidth.toFloat(), TextGame.viewport.screenHeight.toFloat()) //Draw
                TextGame.batch.color = color //Reset color
                TextGame.batch.end() //End batch

                //Load the game over screen.
                gameScreen.game.screen = GameOverScreen(gameScreen.game)
            })

            ChainTask.addTaskToEveryFrameList(task)
        })
    }

    override fun reset() {
        eventMap = hashMapOf()
    }
}