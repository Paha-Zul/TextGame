package com.quickbite.rx2020.managers

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.Result
import com.quickbite.rx2020.SaveLoad
import com.quickbite.rx2020.clamp
import com.quickbite.rx2020.screens.GameScreen
import com.quickbite.rx2020.util.Logger
import java.util.*

/**
 * Created by Paha on 2/8/2016.
 */
object EventManager {
    private val eventMap: HashMap<String, (args: List<Any>) -> Unit> = hashMapOf()

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
        EventManager.onEvent("hurt", { args ->
            if(GameEventManager.currActiveEvent == null)
                return@onEvent

            var name = (args[0]) as String
            val min = ((args[1]) as String).toInt()
            val max = if(args.count() >= 3) ((args[2]) as String).toInt() else min
            val perc = if(args.count() >= 4) ((args[3]) as String).toBoolean() else false
            var numPeople = if(args.count() >= 5) ((args[4]) as String).toInt() else 1
            if(numPeople == -1) numPeople = GroupManager.numPeopleAlive
            numPeople.clamp(0, GroupManager.numPeopleAlive)

            if(name.equals("evt")) name = GameEventManager.currActiveEvent!!.randomPersonList[0].firstName

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

                    Result.addResult(person.firstName, res.toFloat(), gameScreen.currGameTime, "'s HP", gameScreen.gui)
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

                Result.addResult(person.firstName, amt.toFloat(), gameScreen.currGameTime, "'s HP", gameScreen.gui)
            }
        })

        EventManager.onEvent("heal", { args ->
            var name = (args[0]) as String
            val min = ((args[1]) as String).toInt()
            val max = ((args[2]) as String).toInt()

            val person = if(name.equals("evt")) GroupManager.getPerson(GameEventManager.currActiveEvent!!.randomPersonList[0].firstName)!! else GroupManager.getPerson(name)

            val amt = MathUtils.random(min, max);
            person!!.addHealth(amt.toFloat())
        })

        EventManager.onEvent("removeInjury", { args ->
            val name = args[0] as String
            val type = args[1] as String

            val person = GroupManager.getPerson(name)!!;
            when(type){
                "worst" -> person.removeWorstInjury()
                else -> person.removeLongestInjury()
            }

            EventManager.callEvent("addRndAmt", "-1", "-1", "medkits")
        })

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
                }
            }catch(e:NumberFormatException){
                e.printStackTrace()
                Logger.log("EventManager", "addRndAmt has some wrong parameters, make sure they are in the order: min/max/supplyName/perPerson/chance")
            }
        })

        EventManager.onEvent("addRndItem", {args ->
            try {
                val min: Float = (args[0] as String).toFloat()
                val max: Float = (args[1] as String).toFloat()
                val chance = (args[2] as String).toFloat()
                val list: List<Any> = args.subList(3, args.size - 1)

                if(MathUtils.random(100) <= chance) {
                    val randomSupply = list[MathUtils.random(list.size - 1)] as String
                    var num = MathUtils.random(Math.abs(min), Math.abs(max))

                    if (min < 0 || max < 0) num = -num
                    SupplyManager.addToSupply(randomSupply, num.toFloat())
                }
            }catch(e:NumberFormatException){
                e.printStackTrace()
                Logger.log("EventManager", "addRndItem has some wrong parameters, make sure they are in the order: min/max/chance/supplyNames(args)")
            }
        })

        EventManager.onEvent("rest", {args ->
            val amt = (args[0] as String).toFloat()
            val chance = if(args.size >= 2) (args[0] as String).toFloat() else 100f

            val rnd = MathUtils.random(100)

            if(chance >= rnd) {
                GroupManager.getPeopleList().forEach { person ->
                    person.addHealth(amt)
                    Result.addResult(person.firstName, amt.toFloat(), gameScreen.currGameTime, "'s HP", gameScreen.gui)
                }
            }
        })

        EventManager.onEvent("damageROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if(args.count() >= 3) (args[2] as String).toFloat() else 100f

            val rand = MathUtils.random(1, 100)
            if(rand <= chance) {
                val amt = -MathUtils.random(min, max)
                ROVManager.addHealthROV(amt)

                Result.addResult("ROV", amt.toFloat(), gameScreen.currGameTime, "'s HP", gameScreen.gui)
            }
        })

        EventManager.onEvent("repairROV", {args ->
            val min = (args[0] as String).toFloat()
            val max = if(args.count() >= 2) (args[1] as String).toFloat() else min
            val chance = if(args.count() >= 3) (args[2] as String).toFloat() else 100f

            if(MathUtils.random(100) <= chance) {
                val amt = MathUtils.random(min, max)

                ROVManager.addHealthROV(amt)

                Result.addResult("ROV", amt.toFloat(), gameScreen.currGameTime, "'s HP", gameScreen.gui)
            }
        })

        EventManager.onEvent("cutMiles", {args ->
            val min = (args[0] as String).toInt()
            val max = if(args.count() >= 2) (args[1] as String).toInt() else min

            var amt = MathUtils.random(Math.abs(min), Math.abs(max))
            if(min < 0 || max < 0) amt = -amt

            GameStats.TravelInfo.totalDistTraveled += amt

            Result.addResult("miles", -amt.toFloat(), gameScreen.currGameTime, gui = gameScreen.gui)
        })

        EventManager.onEvent("wait", {args ->
            val min = (args[0] as String).toInt()
            val max = if(args.count() >= 2) (args[1] as String).toInt() else min

            var amt = MathUtils.random(Math.abs(min), Math.abs(max))

            GameStats.TimeInfo.totalTimeCounter += amt

            Result.addResult("hours waited", amt.toFloat(), gameScreen.currGameTime, gui = gameScreen.gui)
        })

        EventManager.onEvent("openTrade", {args ->
            gameScreen.gui.buildTradeWindow()
            gameScreen.gui.openTradeWindow()
        })

        EventManager.onEvent("death", { args ->
            val person = args[0] as Person

            gameScreen.gui.buildGroupTable()

            Result.addDeath(person)

            if(GroupManager.numPeopleAlive == 0) //OH MY GAWD GAME OVER!!
                gameScreen.setGameOver()
        })

        EventManager.onEvent("healthChanged", {args ->
            val person = args[0] as Person
            val amt = args[1] as Float

            gameScreen.gui.buildGroupTable()

            Result.addResult(person.firstName, amt, gameScreen.currGameTime, "'s HP", gameScreen.gui)
        })

        EventManager.onEvent("supplyChanged", { args ->
            val supply = args[0] as SupplyManager.Supply
            val amt = args[1] as Float

            Result.addResult(supply.displayName, amt, gameScreen.currGameTime, "", gameScreen.gui)
        })

        EventManager.onEvent("eventStarted", { args ->
            //TODO Implementation?
        })

        EventManager.onEvent("eventFinished", { args ->
            SaveLoad.saveGame(true)
        })
    }
}