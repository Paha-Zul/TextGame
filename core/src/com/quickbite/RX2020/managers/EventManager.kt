@file:Suppress("NAME_SHADOWING")

package com.quickbite.rx2020.managers

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.clamp
import com.quickbite.rx2020.gui.GameScreenGUI
import com.quickbite.rx2020.interfaces.IResetable
import com.quickbite.rx2020.objects.Ailment
import com.quickbite.rx2020.objects.Supply
import com.quickbite.rx2020.screens.GameOverScreen
import com.quickbite.rx2020.screens.GameScreen
import com.quickbite.rx2020.util.FunGameStats
import com.quickbite.rx2020.util.GH
import com.quickbite.rx2020.util.Logger
import com.quickbite.rx2020.util.SaveLoad

/**
 * Created by Paha on 2/8/2016.
 */
object EventManager : IResetable {
    private var eventMap: HashMap<String, (args: List<Any>) -> Unit> = hashMapOf()

    fun init(gameScreen: GameScreen) {
        makeEvents(gameScreen)
    }

    /**
     * Calls an event from the event map
     * @param name The name of the event
     * @param people The list of people (names) that are involved in the event
     * @param args A list of arguments to be used by the event
     */
    fun onEvent(name: String, event: (List<Any>) -> Unit) {
        eventMap.put(name, event)
    }

    /**
     * Calls an event from the event map
     * @param name The name of the event
     * @param people The list of people (names) that are involved in the event
     * @param args A list of arguments to be used by the event
     */
    fun callEvent(name: String, vararg args: Any) {
        val func = eventMap[name]
        if (func != null)
            func(args.toList())
        else
            Logger.log("EventSystem", "No event found for $name, is it spelled correctly?", Logger.LogLevel.Warning)
    }

    /**
     * Calls an event from the event map
     * @param name The name of the event
     * @param people The list of people (names) that are involved in the event
     * @param args A list of arguments to be used by the event
     */
    fun callEvent(name: String, args: List<Any>) {
        val func = eventMap[name]
        if (func != null)
            func(args)
        else
            Logger.log("EventSystem", "No event found for $name, is it spelled correctly?", Logger.LogLevel.Warning)
    }

    /**
     * Adds events to the EventSystem to be called later.
     */
    private fun makeEvents(gameScreen: GameScreen) {
        //TODO Need a 'wait' event.

        //We are hurting a person/people
        EventManager.onEvent("addHealth", {  args ->
            var name = (args[0]) as String
            val min = ((args[1]) as String).toInt()
            val max = if (args.count() >= 3) ((args[2]) as String).toInt() else min
            val perc = if (args.count() >= 4) ((args[3]) as String).toBoolean() else false
            var numPeople = if (args.count() >= 5) ((args[4]) as String).toInt() else 1
            val randomPerPerson = if (args.count() >= 6) ((args[5]) as String).toBoolean() else false

            //Set how many people to affect. If -1, affect all people
            if (numPeople == -1) numPeople = GroupManager.numPeopleAlive
            numPeople.clamp(0, GroupManager.numPeopleAlive)

            //If our name is supposed to be an event person, get it from the current event.
            if (name.matches("evt[0-9]".toRegex())) name = GameEventManager.currActiveEvent!!.randomPersonList[name.last().toString().toInt()].firstName
            else if (name == "evt") name = GameEventManager.currActiveEvent!!.randomPersonList[0].firstName

            //If we are applying to all the people...
            when {
                numPeople == GroupManager.numPeopleAlive -> {
                    var amt = MathUtils.random(min, max)
                    val list = GroupManager.getPeopleList()
                    addHealthToPerson(list.toList(), amt, min, max, perc, randomPerPerson)

                    //If we are doing it to multiple people...
                }
                numPeople > 1 -> {
                    //TODO no use for it yet...

                    //For only one person...
                }
                else -> {
                    var amt = MathUtils.random(Math.abs(min), Math.abs(max))
                    if (min < 0) amt = -amt //If we are dealing with negative numbers, negatize it!
                    val person = GroupManager.getPerson(name)!!
                    addHealthToPerson(listOf(person), amt, min, max, perc, randomPerPerson)
                }
            }
        })

        EventManager.onEvent("addAilment", {  args ->
            val name = args[0] as String
            val type = args[1] as String
            val level = args[2] as String

            val person: Person

            person = when {
                name == "rand" -> GroupManager.getRandomPerson()!!  //If the name is "rand", get a random person
                name.matches("evt[0-9]".toRegex()) -> GameEventManager.currActiveEvent!!.randomPersonList[name.last().toString().toInt()] //Otherwise, get the person from the random person list of the event
                name == "evt" -> GameEventManager.currActiveEvent!!.randomPersonList[0] //Otherwise if it's just "evt", get the first person in the event people list
                else -> GroupManager.getPerson(name)!! //Otherwise it's a specific name, get it from the group manager
            }

            val ailmentType = when (type) {
                "sickness" -> Ailment.AilmentType.Sickness
                else -> Ailment.AilmentType.Injury
            }

            val ailmentLevel = when (level) {
                "minor" -> Ailment.AilmentLevel.Minor
                "medium" -> Ailment.AilmentLevel.Regular
                "major" -> Ailment.AilmentLevel.Major
                else -> Ailment.AilmentLevel.Trauma
            }

            addRemoveAilment("addAilment", person, ailmentType, ailmentLevel)
        })

        //Called to remove and injury from a person.
        EventManager.onEvent("removeAilment", {  args ->
            val name = args[0] as String
            val type = args[1] as String

            val person = GroupManager.getPerson(name)!!
            //Call the addRemoveAilment. Ailment type and ailment level don't really matter here.
            addRemoveAilment("removeAilment", person, Ailment.AilmentType.Injury, Ailment.AilmentLevel.Minor, type)
        })

        //Adds a random amount of an item. This specifically chooses from all items that are available
        EventManager.onEvent("addRndAmt", {  args ->
            try {
                val min: Float = (args[0] as String).toFloat() //The min amt
                val max: Float = (args[1] as String).toFloat() //The max amt
                val supplyName: String = args[2] as String //The name of the supply
                val perPerson = if (args.count() >= 4) (args[3] as String).toBoolean() else false //If it is per person
                val chance = if (args.count() >= 5) (args[4] as String).toFloat() else 100f //Chance to happen

                val rand = MathUtils.random(100)
                if (rand <= chance) {
                    var amount = MathUtils.random(Math.abs(min), Math.abs(max))
                    if (min < 0 || max < 0) amount = -amount
                    if (perPerson) amount *= GroupManager.numPeopleAlive

                    addItemAmount(supplyName, amount)
                }

                GameScreenGUI.updateSuppliesGUI()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                Logger.log("EventManager", "addRndAmt has some wrong parameters, make sure they are in the order: min/max/supplyName/perPerson/chance")
            }
        })

        //Adds a random item to the supply. This specifically chooses from a list of items passed in
        EventManager.onEvent("addRndItem", {  args ->
            try {
                val min: Float = (args[0] as String).toFloat()
                val max: Float = (args[1] as String).toFloat()
                val chance = (args[2] as String).toFloat()
                val list: List<Any> = args.subList(3, args.size)

                if (MathUtils.random(100) <= chance) {
                    val randomSupply = list[MathUtils.random(list.size - 1)] as String
                    var amount = MathUtils.random(Math.abs(min), Math.abs(max))

                    if (min < 0 || max < 0) amount = -amount

                    addItemAmount(randomSupply, amount)
                }

                GameScreenGUI.updateSuppliesGUI()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                Logger.log("EventManager", "addRndItem has some wrong parameters, make sure they are in the order: min/max/chance/supplyNames(args)")
            }
        })

        //Adds a random item to the supply.
        EventManager.onEvent("addRndItemHealth", {  args ->
            try {
                val min: Float = (args[0] as String).toFloat()
                val max: Float = (args[1] as String).toFloat()
                val chance = (args[2] as String).toFloat()
                val supplyList: List<Any> = args.subList(3, args.size)

                if (MathUtils.random(100) <= chance) {
                    val randomSupply = supplyList[MathUtils.random(supplyList.size - 1)] as String
                    val amt = MathUtils.random(min, max)
                    addItemHealth(randomSupply, amt)
                }

                GameScreenGUI.updateSuppliesGUI()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                Logger.log("EventManager", "addRndItem has some wrong parameters, make sure they are in the order: min/max/chance/supplyNames(args)")
            }
        })

        EventManager.onEvent("alterSupplies", {  args ->
            val size = args[0] as String
            val lose = if (args.size > 1) (args[1] as String).toBoolean() else false

            //TODO Should applying these rewards be boosted by traits?
            GH.applyReward(size, lose)
        })

        EventManager.onEvent("reward", {  args ->
            val size = args[0] as String

            GH.applyReward(size)
        })

        EventManager.onEvent("scheduleEvent", {  args ->
            val evtName = args[0] as String
            val minHours = (args[1] as String).toFloat()
            val maxHours = (args[2] as String).toFloat()
            val evtType = if (args.size > 3) args[3] as String else ""
            val evtPage = if (args.size > 4) (args[4] as String).toInt() else 0

            if (minHours > 0) {
                //Add a timer to call the event later
                GameEventManager.addDelayedEvent(evtName, evtType, MathUtils.random(minHours, maxHours), evtPage)
            } else {
                GameScreenGUI.openEventGUI(GameEventManager.getAndSetEvent(evtName, evtType)!!, evtPage)
            }
        })

        //A chance to rest. Heals the group members.
        EventManager.onEvent("rest", {  args ->
            val amt = (args[0] as String).toFloat()
            val chance = if (args.size >= 2) (args[0] as String).toFloat() else 100f

            val rnd = MathUtils.random(100)

            if (chance >= rnd) {
                GroupManager.getPeopleList().forEach { person ->
                    person.addHealth(amt)
                    FunGameStats.addFunStat("Total Health Net", amt.toInt().toString())
                }
            }
        })

        //Called to damage the ROV
        EventManager.onEvent("addHealthROV", {  args ->
            val min = (args[0] as String).toFloat()
            val max = if (args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if (args.count() >= 3) (args[2] as String).toFloat() else 100f

            val rand = MathUtils.random(1, 100)
            if (rand <= chance) {
                val amt = MathUtils.random(min, max)

                ROVManager.addHealthROV(amt)

                FunGameStats.addFunStat("Total ROV Net Health", amt.toInt().toString())
            }
        })

        //Called to damage the ROV
        EventManager.onEvent("damageROV", {  args ->
            val min = (args[0] as String).toFloat()
            val max = if (args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if (args.count() >= 3) (args[2] as String).toFloat() else 100f

            val rand = MathUtils.random(1, 100)
            if (rand <= chance) {
                val amt = -MathUtils.random(min, max)
                changeHealthROV("damageROV", amt)
            }
        })

        //Called to repair the ROV
        EventManager.onEvent("repairROV", {  args ->
            val min = (args[0] as String).toFloat()
            val max = if (args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if (args.count() >= 3) (args[2] as String).toFloat() else 100f

            if (MathUtils.random(100) <= chance) {
                val amt = MathUtils.random(min, max)
                changeHealthROV("repairROV", amt)
            }
        })

        //Called when miles should be cut or added
        EventManager.onEvent("cutMiles", {  args ->
            val min = (args[0] as String).toInt()
            val max = if (args.count() >= 2) (args[1] as String).toInt() else min

            var amt = MathUtils.random(Math.abs(min), Math.abs(max))
            if (min < 0 || max < 0) amt = -amt

            cutMiles(amt.toFloat())
        })

        //Called when the game should 'wait' or progress some amount of time.
        EventManager.onEvent("wait", {  args ->
            val min = (args[0] as String).toInt()
            val max = if (args.count() >= 2) (args[1] as String).toInt() else min

            val amt = MathUtils.random(Math.abs(min), Math.abs(max))

            GameStats.TimeInfo.totalTimeCounter += amt

            ResultManager.addRecentChange("hours waited", amt.toFloat(), GameScreen.currGameTime, isEventRelated = GameEventManager.currActiveEvent != null)
            FunGameStats.addFunStat("Hours Waited", amt.toString())
        })

        //Called when the trade window should be opened.
        EventManager.onEvent("openTrade", {  args ->
            GameScreenGUI.buildTradeWindow()
            GameScreenGUI.openTradeWindow()
        })

        //Called when a person dies.
        EventManager.onEvent("death", {  args ->
            val person = args[0] as Person

            GameScreenGUI.buildGroupTable()

            //Remove all the traits from the manager when a person dies
            person.traitList.forEach { trait ->
                TraitManager.removeTrait(trait.traitDef, person)
            }

            ResultManager.addRecentDeath(person)
            FunGameStats.addFunStat(person.fullName, "dead", true)
        })

        //Takes the recent deaths and puts them into the event deaths for displaying.
        EventManager.onEvent("showDeaths", {  args ->
            val pair = ResultManager.recentDeathMap.toList()[0]
            ResultManager.recentDeathResult = pair.second      //Store the first death ResultManager.
            ResultManager.recentDeathMap.remove(pair.first)    //Remove it from the map.
        })

        //Called when a person's health changed.
        EventManager.onEvent("healthChanged", {  args ->
            val person = args[0] as Person
            val amt = args[1] as Float

            GameScreenGUI.buildGroupTable()

            ResultManager.addRecentChange(person.firstName, amt, GameScreen.currGameTime, "'s HP", GameEventManager.currActiveEvent != null)
        })

        //Called when a supply from the SupplyManager has changed. This is called from SupplyManager usually.
        EventManager.onEvent("supplyChanged", {  args ->
            val supply = args[0] as Supply
            val amt = args[1] as Float
            val oldAmt = args[2] as Float

            val name = GH.checkSupplyAmount(supply, amt, oldAmt)
            if (!name.isEmpty()) {
                GameScreenGUI.openEventGUI(GameEventManager.getAndSetEvent(name, "special")!!)
            }

            ResultManager.addRecentChange(supply.displayName, amt, GameScreen.currGameTime, "", GameEventManager.currActiveEvent != null)
        })

        //Called when a supply from the SupplyManager has changed. This is called from SupplyManager usually.
        EventManager.onEvent("supplyHealthChanged", {  args ->
            val supply = args[0] as Supply
            val amt = args[1] as Float //The amount changed.
            val oldAmt = args[2] as Float //The amount before the change

            GH.checkSupplyHealth(supply, amt, oldAmt)

//            GH.checkSupplyHealth(supply, amt, oldAmt)

            ResultManager.addRecentChange("${supply.displayName} health", amt, GameScreen.currGameTime, "", GameEventManager.currActiveEvent != null)
        })

        //Called when an event starts.
        EventManager.onEvent("eventStarted", {  args ->
            val name = args[0] as String

            Logger.log("EventManager", "Event $name is starting")
            //TODO Implementation?
        })

        //Called when an event finishes.
        EventManager.onEvent("eventFinished", {  args ->
            val name = args[0] as String

            Logger.log("EventManager", "Event $name is ending")

            ResultManager.purgeEventResults()
        })

        //Called when an event finishes.
        EventManager.onEvent("forceCamp", {  args ->
            GameScreenGUI.closeEventGUI(false, true)
            val gameOver = GH.checkGameOverConditions()
            if (gameOver.first)
                gameScreen.setGameOver(gameOver.second)
            else
                gameScreen.changeToCamp()
        })

        //Called when the game is over. Shows the game over screen.
        EventManager.onEvent("gameOver", {  args ->
            val win = (args[0] as String).toBoolean()

            SaveLoad.deleteSave()

            for (person in GroupManager.getPeopleList())
                FunGameStats.addFunStat(person.fullName, "alive", true)

            GameStats.win = win
            gameScreen.pauseGame()
            gameScreen.state = GameScreen.State.GAMEOVER

            //Lets fade the screen out to white and load the game over screen.
            var opacity = 0f
            val overlay = TextGame.smallGuiAtlas.findRegion("pixelWhite")
            val task = ChainTask({ opacity < 1f }, {
                gameScreen.pauseGame()
                opacity = GH.lerpValue(opacity, 0f, 1f, 1f) //Lerp opacity
                TextGame.batch.begin() //Begin the batch
                val color = TextGame.batch.color //Set the color
                TextGame.batch.color = Color(1f, 1f, 1f, opacity) //Make a new color
                TextGame.batch.draw(overlay, -TextGame.viewport.screenWidth.toFloat() / 2, -TextGame.viewport.screenHeight.toFloat() / 2, TextGame.viewport.screenWidth.toFloat(), TextGame.viewport.screenHeight.toFloat()) //Draw
                TextGame.batch.color = color //Reset color
                TextGame.batch.end() //End batch
            }, {
                TextGame.batch.begin() //Begin the batch
                val color = TextGame.batch.color //Set the color
                TextGame.batch.color = Color(1f, 1f, 1f, 1f) //Make a new color
                TextGame.batch.draw(overlay, -TextGame.viewport.screenWidth.toFloat() / 2, -TextGame.viewport.screenHeight.toFloat() / 2, TextGame.viewport.screenWidth.toFloat(), TextGame.viewport.screenHeight.toFloat()) //Draw
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
     * @param people A list of people to use for modifiers. Usually
     */
    private fun addItemAmount(itemName: String, amount: Float, people:List<Person>? = null) {
        var amount = amount //Make this mutable
        val itemDef = DataManager.getItem(itemName)!!

        var modifier = if (itemDef.type != "ROVPart")
            TraitManager.getTraitModifier("addRndAmt", itemName, people = people)
        else
            TraitManager.getTraitModifier("addRndAmt", subType = itemDef.type, people = people)

        System.out.println("Base amount $amount")

        //TODO Maybe in the future allow things to REDUCE items LOST. Right now it's just bonus to getting items
        //Only apply the modifier if we are GAINING. Traits
        if (amount > 0) {
            amount += if (modifier.second)
                amount * (modifier.first / 100f)
            else
                modifier.first
        }

        System.out.println("Modified amount $amount")

        SupplyManager.addToSupply(itemName, amount)
        FunGameStats.addFunStat(itemName, amount.toString())
    }

    /**
     * Called when the health of the ROV needs to be changed
     * @param command The command to use to get the trait
     * @param amt The amount to change the ROV's health by
     */
    private fun changeHealthROV(command: String, amt: Float) {
        val modifier = TraitManager.getTraitModifier(command, people = GameEventManager.currActiveEvent!!.randomPersonList) //Get the modifier
        val modifiedAmount = if (amt < 0) amt * (modifier.first / 100f) else 0f + amt //Modify if negative, otherwise 0 change
        ROVManager.addHealthROV(modifiedAmount) //Add the health to the ROV
        FunGameStats.addFunStat("Total ROV Health Net", modifiedAmount.toInt().toString()) //Record in fun stats
    }

    /**
     * Cuts miles from the trip.
     * @param amt The amount to cut. Expected to be negative
     **/
    private fun cutMiles(amt: Float) {
        var amt = amt //Shadow this to make it mutable

        val modifier = TraitManager.getTraitModifier("cutMiles") //Get the modifier
        amt += (-modifier.first / 100f) * amt //Subtract the modified amount. The modifier amount is negative so we negate it to a positive

        GameStats.TravelInfo.totalDistTraveled += amt.toInt() //Add it to the distance traveled (which is really subtracting)

        //Add it to the recent changes and fun game stats
        ResultManager.addRecentChange("miles", -amt, GameScreen.currGameTime, isEventRelated = GameEventManager.currActiveEvent != null)
        FunGameStats.addFunStat("Total Miles Net", amt.toInt().toString())
    }

    /**
     * Adds health to an item.
     * @param itemName The name of the item (to find in the supply manager)
     * @param amt The amount of health to add
     */
    private fun addItemHealth(itemName: String, amt: Float) {
        var amt = amt //Shadow to make mutable
        val item = DataManager.getItem(itemName)!! //Get the item from the manager
        val currEvent = GameEventManager.currActiveEvent!! //Get the current active event. We'll use this to grab the active people from
        val peopleInvolvedInEvent = currEvent.randomPersonList.subList(0, currEvent.numOfPeopleInEvent) //Gets a sublist of the people involved
        val modifier = TraitManager.getTraitModifier("addRndItemHealth", subType = item.type, people = peopleInvolvedInEvent) //Get the trait modifier
        amt += if(amt < 0) amt * (modifier.first / 100f) else 0f //Increase by the modifier amount only IF we are taking away health (amt < 0)
        SupplyManager.addHealthToSupply(itemName, amt) //Add health to the supply
        FunGameStats.addFunStat("$itemName damage", amt.toInt().toString())
    }

    /**
     * Adds health to a person. This could either be adding a positive or negative amount
     * @param amt The amount to add. This could be negative or positive
     */
    private fun addHealthToPerson(list: List<Person>, amt: Int, min: Int, max: Int, perc: Boolean, randomPerPerson: Boolean) {
        var amt = amt
        list.forEach { person ->
            if (randomPerPerson)
                amt = MathUtils.random(Math.abs(min), Math.abs(max))

            val modifier = TraitManager.getTraitModifier("addHealth", subCommand = "remove")
            val multiplier = if(amt < 0) modifier.first / 100f else 0f //Only take the modifier if we are dealing with removing health (amt < 0)

            if (perc)
                person.addPercentHealth(amt.toFloat() + multiplier).toInt()
            else
                person.addHealth(amt.toFloat() + amt.toFloat() * multiplier).toInt()

            FunGameStats.addFunStat("Total Health Net", amt.toString())
        }
    }

    /**
     * Handles adding or removing an ailment to/from a person
     * @param command The command, either addAilment or removeAilment
     * @param person The person to apply this change to
     * @param ailmentType The type of ailment
     * @param ailmentLevel The level of ailment
     * @param typeToRemove The type to remove. This is optional and only needed if removing. Defaults to "worst"
     */
    private fun addRemoveAilment(command:String, person:Person, ailmentType:Ailment.AilmentType, ailmentLevel:Ailment.AilmentLevel, typeToRemove:String = "worse"){
        if(command == "addAilment") {
            person.addAilment(ailmentLevel, ailmentType)
            FunGameStats.addFunStat("Ailments Inflicted: ", "1")
            ResultManager.addRecentChange("$ailmentLevel $ailmentType for ${person.firstName}", 1f, GameScreen.currGameTime, "", true)
        }else if(command == "removeAilment"){
            when (typeToRemove) {
                "worst" -> person.removeWorstAilment()
                else -> person.removeLongestAilment()
            }
            FunGameStats.addFunStat("Ailments Cured: ", "1")
        }
    }

    override fun reset() {
        eventMap = hashMapOf()
    }

}