package com.quickbite.rx2020.managers

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.clamp
import com.quickbite.rx2020.gui.GameScreenGUI
import com.quickbite.rx2020.interfaces.IResetable
import com.quickbite.rx2020.screens.GameOverScreen
import com.quickbite.rx2020.screens.GameScreen
import com.quickbite.rx2020.util.FunGameStats
import com.quickbite.rx2020.util.GH
import com.quickbite.rx2020.util.Logger
import com.quickbite.rx2020.util.SaveLoad
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
            Logger.log("EventSystem", "No event found for $name, is it spelled correctly?", Logger.LogLevel.Warning)
    }

    fun callEvent(name:String, args:List<Any>){
        val func = eventMap[name]
        if(func != null)
            func(args)
        else
            Logger.log("EventSystem", "No event found for $name, is it spelled correctly?", Logger.LogLevel.Warning)
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
            val randomPerPerson = if(args.count() >= 6) ((args[5]) as String).toBoolean() else false

            if(numPeople == -1) numPeople = GroupManager.numPeopleAlive
            numPeople.clamp(0, GroupManager.numPeopleAlive)

            //If our name is supposed to be an event person, get it from the current event.
            if(name.matches("evt[0-9]".toRegex())) name = GameEventManager.currActiveEvent!!.randomPersonList[name.last().toString().toInt()].firstName
            else if(name == "evt") name = GameEventManager.currActiveEvent!!.randomPersonList[0].firstName

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

            val person:Person

            if(name == "rand") person = GroupManager.getRandomPerson()!!                     //We need to do .toString() for the .toInt() method or else it converts the character to ascii value.
            else if(name.matches("evt[0-9]".toRegex())) person = GameEventManager.currActiveEvent!!.randomPersonList[name.last().toString().toInt()]
            else if(name == "evt") person = GameEventManager.currActiveEvent!!.randomPersonList[0]
            else person = GroupManager.getPerson(name)!!

            val disType:Person.Ailment.AilmentType
            val disLevel:Person.Ailment.AilmentLevel

            when(type){
                "sickness" -> disType = Person.Ailment.AilmentType.Sickness
                else -> disType = Person.Ailment.AilmentType.Injury
            }

            when(level){
                "minor" -> disLevel = Person.Ailment.AilmentLevel.Minor
                "medium" -> disLevel = Person.Ailment.AilmentLevel.Regular
                "major" -> disLevel = Person.Ailment.AilmentLevel.Major
                else -> disLevel = Person.Ailment.AilmentLevel.Trauma
            }

            person.addAilment(disLevel, disType)

            FunGameStats.addFunStat("Ailments Inflicted: ", "1")
            ResultManager.addRecentChange("$level $type for ${person.firstName}", 1f, GameScreen.currGameTime, "", true)
        })

        //Called to remove and injury from a person.
        EventManager.onEvent("removeAilment", { args ->
            val name = args[0] as String
            val type = args[1] as String

            val person = GroupManager.getPerson(name)!!;
            when(type){
                "worst" -> person.removeWorstAilment()
                else -> person.removeLongestAilment()
            }

            FunGameStats.addFunStat("Ailments Cured: ", "1")
        })

        //Adds a random amount of an item. This specifically chooses from all items that are available
        EventManager.onEvent("addRndAmt", {args ->
            try{
                val min:Float = (args[0] as String).toFloat() //The min amt
                val max:Float = (args[1] as String).toFloat() //The max amt
                val supplyName:String = args[2] as String //The name of the supply
                val perPerson = if(args.count() >= 4) (args[3] as String).toBoolean() else false //If it is per person
                val chance = if(args.count() >= 5) (args[4] as String).toFloat() else 100f //Chance to happen

                val rand = MathUtils.random(100)
                if(rand <= chance) {
                    var amount = MathUtils.random(Math.abs(min), Math.abs(max))
                    if (min < 0 || max < 0) amount = -amount
                    if(perPerson) amount *= GroupManager.numPeopleAlive

                    addItemAmount(supplyName, amount, "")
                }

                GameScreenGUI.updateSuppliesGUI()
            }catch(e:NumberFormatException){
                e.printStackTrace()
                Logger.log("EventManager", "addRndAmt has some wrong parameters, make sure they are in the order: min/max/supplyName/perPerson/chance")
            }
        })

        //Adds a random item to the supply. This specifically chooses from a list of items passed in
        EventManager.onEvent("addRndItem", {args ->
            try {
                val min: Float = (args[0] as String).toFloat()
                val max: Float = (args[1] as String).toFloat()
                val chance = (args[2] as String).toFloat()
                val list: List<Any> = args.subList(3, args.size)

                if(MathUtils.random(100) <= chance) {
                    val randomSupply = list[MathUtils.random(list.size - 1)] as String
                    var amount = MathUtils.random(Math.abs(min), Math.abs(max))

                    if (min < 0 || max < 0) amount = -amount

                    addItemAmount(randomSupply, amount, "")
                }

                GameScreenGUI.updateSuppliesGUI()
            }catch(e:NumberFormatException){
                e.printStackTrace()
                Logger.log("EventManager", "addRndItem has some wrong parameters, make sure they are in the order: min/max/chance/supplyNames(args)")
            }
        })

        //Adds a random item to the supply.
        EventManager.onEvent("addRndItemHealth", {args ->
            try {
                val min: Float = (args[0] as String).toFloat()
                val max: Float = (args[1] as String).toFloat()
                val chance = (args[2] as String).toFloat()
                val supplyList: List<Any> = args.subList(3, args.size)

                if(MathUtils.random(100) <= chance) {
                    val randomSupply = supplyList[MathUtils.random(supplyList.size - 1)] as String
                    var num = MathUtils.random(Math.abs(min), Math.abs(max))

                    if (min < 0 || max < 0) num = -num
                    SupplyManager.addHealthToSupply(randomSupply, num.toFloat())

                    FunGameStats.addFunStat("$randomSupply damage", num.toInt().toString())
                }

                GameScreenGUI.updateSuppliesGUI()
            }catch(e:NumberFormatException){
                e.printStackTrace()
                Logger.log("EventManager", "addRndItem has some wrong parameters, make sure they are in the order: min/max/chance/supplyNames(args)")
            }
        })

        EventManager.onEvent("alterSupplies", {args ->
            val size = args[0] as String
            val lose = if(args.size > 1) (args[1] as String).toBoolean() else false

            GH.alterSupplies(size, lose)
        })

        EventManager.onEvent("reward", {args ->
            val size = args[0] as String

            GH.applyReward(size)
        })

        EventManager.onEvent("scheduleEvent", {args ->
            val evtName = args[0] as String
            val minHours = (args[1] as String).toFloat()
            val maxHours = (args[2] as String).toFloat()
            val evtType = if(args.size > 3) args[3] as String else ""
            val evtPage = if(args.size > 4) (args[4] as String).toInt() else 0

            if(minHours > 0) {
                //Add a timer to call the event later
                GameEventManager.addDelayedEvent(evtName, evtType, MathUtils.random(minHours, maxHours), evtPage)
            }else{
                GameScreenGUI.openEventGUI(GameEventManager.getAndSetEvent(evtName, evtType)!!, evtPage)
            }
        })

        //A chance to rest. Heals the group members.
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
                changeHealthROV("damageROV", amt)
            }
        })

        //Called to repair the ROV
        EventManager.onEvent("repairROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if(args.count() >= 3) (args[2] as String).toFloat() else 100f

            if(MathUtils.random(100) <= chance) {
                val amt = MathUtils.random(min, max)

                changeHealthROV("repairROV", amt)
            }
        })

        //Called when miles should be cut or added
        EventManager.onEvent("cutMiles", {args ->
            val min = (args[0] as String).toInt()
            val max = if(args.count() >= 2) (args[1] as String).toInt() else min

            var amt = MathUtils.random(Math.abs(min), Math.abs(max))
            if(min < 0 || max < 0) amt = -amt

            GameStats.TravelInfo.totalDistTraveled += amt

            ResultManager.addRecentChange("miles", -amt.toFloat(), GameScreen.currGameTime, isEventRelated = GameEventManager.currActiveEvent != null)
            FunGameStats.addFunStat("Total Miles Net", amt.toInt().toString())
        })

        //Called when the game should 'wait' or progress some amount of time.
        EventManager.onEvent("wait", {args ->
            val min = (args[0] as String).toInt()
            val max = if(args.count() >= 2) (args[1] as String).toInt() else min

            val amt = MathUtils.random(Math.abs(min), Math.abs(max))

            GameStats.TimeInfo.totalTimeCounter += amt

            ResultManager.addRecentChange("hours waited", amt.toFloat(), GameScreen.currGameTime, isEventRelated = GameEventManager.currActiveEvent != null)
            FunGameStats.addFunStat("Hours Waited", amt.toString())
        })

        //Called when the trade window should be opened.
        EventManager.onEvent("openTrade", {args ->
            GameScreenGUI.buildTradeWindow()
            GameScreenGUI.openTradeWindow()
        })

        //Called when a person dies.
        EventManager.onEvent("death", { args ->
            val person = args[0] as Person

            GameScreenGUI.buildGroupTable()
            
            //Remove all the traits from the manager when a person dies
            person.traitList.foreach { trait ->
                TraitManager.removeTrait(trait.traitDef, person.name)
            }

            ResultManager.addRecentDeath(person)
            FunGameStats.addFunStat(person.fullName, "dead", true)
        })

        //Takes the recent deaths and puts them into the event deaths for displaying.
        EventManager.onEvent("showDeaths", {args->
            val pair = ResultManager.recentDeathMap.toList()[0]
            ResultManager.recentDeathResult = pair.second      //Store the first death ResultManager.
            ResultManager.recentDeathMap.remove(pair.first)    //Remove it from the map.
        })

        //Called when a person's health changed.
        EventManager.onEvent("healthChanged", {args ->
            val person = args[0] as Person
            val amt = args[1] as Float

            GameScreenGUI.buildGroupTable()

            ResultManager.addRecentChange(person.firstName, amt, GameScreen.currGameTime, "'s HP", GameEventManager.currActiveEvent != null)
        })

        //Called when a supply from the SupplyManager has changed. This is called from SupplyManager usually.
        EventManager.onEvent("supplyChanged", { args ->
            val supply = args[0] as SupplyManager.Supply
            val amt = args[1] as Float
            val oldAmt = args[2] as Float

            val name = GH.checkSupplyAmount(supply, amt, oldAmt)
            if (!name.isEmpty()) {
                GameScreenGUI.openEventGUI(GameEventManager.getAndSetEvent(name, "special")!!)
//                gameScreen.noticeEventTimer.callback = { GameScreenGUI.beginEventGUI(GameEventManager.getAndSetEvent(name, "special")!!) }
//                gameScreen.noticeEventTimer.restart()
//                gameScreen.noticeEventTimer.start()
            }

            ResultManager.addRecentChange(supply.displayName, amt, GameScreen.currGameTime, "", GameEventManager.currActiveEvent != null)
        })

        //Called when a supply from the SupplyManager has changed. This is called from SupplyManager usually.
        EventManager.onEvent("supplyHealthChanged", { args ->
            val supply = args[0] as SupplyManager.Supply
            val amt = args[1] as Float //The amount changed.
            val oldAmt = args[2] as Float //The amount before the change

            GH.checkSupplyHealth(supply, amt, oldAmt)

//            GH.checkSupplyHealth(supply, amt, oldAmt)

            ResultManager.addRecentChange("${supply.displayName} health", amt, GameScreen.currGameTime, "", GameEventManager.currActiveEvent != null)
        })

        //Called when an event starts.
        EventManager.onEvent("eventStarted", { args ->
            val name = args[0] as String

            Logger.log("EventManager", "Event $name is starting")
            //TODO Implementation?
        })

        //Called when an event finishes.
        EventManager.onEvent("eventFinished", { args ->
            val name = args[0] as String

            Logger.log("EventManager", "Event $name is ending")

            ResultManager.purgeEventResults()
        })

        //Called when an event finishes.
        EventManager.onEvent("forceCamp", { args ->
            GameScreenGUI.closeEventGUI(false, true)
            val gameOver = GH.checkGameOverConditions()
            if(gameOver.first)
                gameScreen.setGameOver(gameOver.second)
            else
                gameScreen.changeToCamp()
        })

        //Called when the game is over. Shows the game over screen.
        EventManager.onEvent("gameOver", {args ->
            val win = (args[0] as String).toBoolean()

            SaveLoad.deleteSave()

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

    /**
     * Any adding or removing of any type of item through events is done through here.
     * @param itemName The name of the item
     * @param amount The amount of the item
     * @param person The name of the person that found the item (for any modifiers). Optional
     */
    private fun addItemAmount(itemName:String, amount:Float, person:String? = null){
        var amount = amount //Make this mutable
        val itemDef = DataManager.getItem(itemName)!!

        var modifier = if(itemDef.type != "ROVPart")
            TraitManager.getTraitModifier("addRndAmt", itemName, person)
        else
            TraitManager.getTraitModifier("addRndAmt", subType = itemDef.type, person)

        if(modifier.second)
            amount += amount*(modifier/100f)
        else
            amount += modifier


        SupplyManager.addToSupply(itemName, amount)
        FunGameStats.addFunStat(itemName, amount.toString())
    }

    fun changeHealthROV(command:String, amt:Float){
        val modifier = TraitManager.getTraitModifier(command)
        ROVManager.addHealthROV(amt + amt*(modifier.first/100f))
        FunGameStats.addFunStat("Total ROV Health Net", amt.toInt().toString())
    }

    override fun reset() {
        eventMap = hashMapOf()
    }
}