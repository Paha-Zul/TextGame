package com.quickbite.rx2020.util

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.managers.*
import java.util.regex.Pattern

/**
 * Created by Paha on 4/10/2016.
 */
object GH {
    /**
     * Lerps a float value from start to target.
     * @param curr The current value of the lerp.
     * @param start The start value of the lerp.
     * @param target The target value of the lerp.
     * @param seconds The time in seconds for the lerp to happen.
     * @return The value of the lerp after the calculated tick amount.
     */
    fun lerpValue(curr: Float, start: Float, target: Float, seconds: Float): Float {
        var curr = curr
        val amt = Math.abs(start - target) / seconds / 60f
        if (start < target) {
            curr += amt
            if (curr >= target) curr = target
        } else {
            curr -= amt
            if (curr <= target) curr = target
        }

        return curr
    }

    fun executeEventActions(event: GameEventManager.EventJson) {
        val list = event.resultingAction
        if (list != null) {
            for (params in list) {
                if (params.size > 0)
                    EventManager.callEvent(params[0], params.slice(1.rangeTo(params.size - 1)))
            }
        }
    }

    fun getEventFromChoice(currEvent:GameEventManager.EventJson, choiceText:String):GameEventManager.EventJson?{
        return currEvent.selectChildEvent(choiceText)
    }

    /**
     * Takes a double array of restrictions, mainly those used in the search activities, and parses/checks them.
     * @param restrictionList A double array that contains a list of separate tokens which make up the restriction, ie: ["edibles", "<", "10"]
     * @return True if the restrictions were passed, false otherwise.
     */
    fun parseAndCheckRestrictions(restrictionList:Array<Array<String>>):Boolean{
        var passed = true
        restrictionList.forEach { params ->
            passed = parseAndCheckRestrictions(params)
            if(passed)
                return@forEach
        }

        return passed
    }

    /**
     * Will take a single string, split by spaces, and check if the restriction has passed.
     * @param restriction The restriction to pass.
     * @return True if the restriction passed, false otherwise.
     */
    fun parseAndCheckRestrictions(restriction:String):Boolean{
        return parseAndCheckRestrictions(restriction.split(" ").toTypedArray())
    }

    private fun parseAndCheckRestrictions(restrictions:Array<String>):Boolean{
        //If there's nothing to check, return true
        if(restrictions.size == 0 || restrictions[0].isEmpty())
            return true

        var passed = true

        var name:String = restrictions[0]
        var operation:String = restrictions[1]
        var amount:Int = restrictions[2].toInt()

        var amtToCheck = 0
        if(name.equals("ROV"))
            amtToCheck = ROVManager.ROVPartMap["ROV"]!!.currHealth.toInt()
        else
            amtToCheck = SupplyManager.getSupply(name).amt.toInt()

        if(operation.equals("<")){
            passed = amtToCheck < amount
        }else if(operation.equals("<=")){
            passed =  amtToCheck <= amount
        }else if(operation.equals(">")){
            passed =  amtToCheck > amount
        }else{
            passed =  amtToCheck >= amount
        }

        return passed
    }

    fun replaceEventDescription(event:GameEventManager.EventJson){
        val newDesc:MutableList<String> = mutableListOf()

        //For each description...
        for(desc in event.description){
            newDesc += replaceGenderAndNames(desc, event)
        }

        //Add the descriptions back to the event.
        event.modifiedDescription = newDesc.toTypedArray()
    }

    fun replaceChoiceForEvent(text:String, event:GameEventManager.EventJson):String{
        return replaceGenderAndNames(text, event)
    }

    private fun replaceGenderAndNames(text:String, event:GameEventManager.EventJson):String{
        val tokens = text.split(" ") //Split by spaces
        val newTokens:MutableList<String> = mutableListOf()

        val pronounPattern = Pattern.compile("((H|h)(is|im|e)[0-9])");
        val numberPattern = Pattern.compile("([0-9])");

        //For each token....
        for(token in tokens){
            var newToken = token

            //If the token matches this pattern, let's send it to the gender changes!
            if(token.matches("(\\S*\\(?(H|h)(is|im|e)[0-9]\\(?\\S*)".toRegex()))
                newToken = replaceGender(token, event, pronounPattern, numberPattern)

            //If it matches a name, replace it.
            if(token.matches("((%n[0-9]?)|(\\(?(N|n)ame[0-9]\\)?))\\S*".toRegex()))
                newToken = replaceName(token, event)

            //Add it to the new token list.
            newTokens += newToken
        }

        //Join all the new tokens back up.
        return newTokens.joinToString(" ")
    }

    private fun replaceGender(text:String, person: Person):String{
        val tokens = text.split(" ") //Split by spaces
        val newTokens:MutableList<String> = mutableListOf()

        val pronounPattern = Pattern.compile("((H|h)(is|im|e)[0-9])");

        //For each token....
        for(token in tokens){
            var newToken = token

            //If the token matches this pattern, let's send it to the gender changes!
            if(token.matches("(\\S*\\(?(H|h)(is|im|e)[0-9]\\(?\\S*)".toRegex()))
                newToken = replaceGender(token, person, pronounPattern)

            //Add it to the new token list.
            newTokens += newToken
        }

        //Join all the new tokens back up.
        return newTokens.joinToString(" ")
    }

    fun replaceGender(token:String, event:GameEventManager.EventJson, pronounPattern:Pattern, numberPattern:Pattern):String{
        var _token = ""

        //First, find the he/him/his [0-9] token.
        var matcher = pronounPattern.matcher(token);
        if(matcher.find()){
            _token = matcher.group(1)
        }

        var number:Int = 0

        matcher = numberPattern.matcher(_token);
        if(matcher.find()){
            number = matcher.group(1).toInt()
        }

        var pronoun = ""                                    //The pronoun that will be changed
        val person = if(number >= event.randomPersonList.size) null else event.randomPersonList[number] //The person to base the pronoun off of
        var male = if(person!=null) person.male else true   //If it is of the male gender

        if(_token.matches("((H|h)is[0-9])".toRegex())){
            if(male) pronoun = "his"
            else pronoun = "her"
        }else if(_token.matches("((H|h)e[0-9])".toRegex())){
            if(male) pronoun = "he"
            else pronoun = "she"
        }else if(_token.matches("((H|h)im[0-9])".toRegex())){
            if(male) pronoun = "him"
            else pronoun = "her"
        }

        //Change to uppercase if needed
        if(_token[0].isUpperCase())
            pronoun = pronoun.replaceRange(0, 1, pronoun[0].toUpperCase().toString())

        //Basically, if we have a situation like "I want his3!" the pronoun might only be "her" after stripping anything else, so we take the pronoun
        //and replace the token "his3!" with "her!" and assign it back to the pronoun. Return this!
        pronoun = token.replace("(\\(?(H|h)(is|im|e)[0-9]\\)?)".toRegex(), pronoun)

        return pronoun
    }

    fun replaceGender(token:String, person:Person, pronounPattern:Pattern):String{
        var _token = ""

        //First, find the he/him/his [0-9] token.
        var matcher = pronounPattern.matcher(token);
        if(matcher.find()){
            _token = matcher.group(1)
        }

        var pronoun = ""        //The pronoun that will be changed
        var male = person.male  //If it is of the male gender

        if(_token.matches("((H|h)is[0-9])".toRegex())){
            if(male) pronoun = "his"
            else pronoun = "her"
        }else if(_token.matches("((H|h)e[0-9])".toRegex())){
            if(male) pronoun = "he"
            else pronoun = "she"
        }else if(_token.matches("((H|h)im[0-9])".toRegex())){
            if(male) pronoun = "him"
            else pronoun = "her"
        }

        //Change to uppercase if needed
        if(_token[0].isUpperCase())
            pronoun = pronoun.replaceRange(0, 1, pronoun[0].toUpperCase().toString())

        //Basically, if we have a situation like "I want his3!" the pronoun might only be "her" after stripping anything else, so we take the pronoun
        //and replace the token "his3!" with "her!" and assign it back to the pronoun. Return this!
        pronoun = token.replace("(\\(?(H|h)(is|im|e)[0-9]\\)?)".toRegex(), pronoun)

        return pronoun
    }

    private fun replaceName(token:String, event:GameEventManager.EventJson):String{
        val _token = token.replace("[^a-zA-Z0-9]","")       //Strip anything like periods
        var number:Int = 0

        //First, find the he/him/his [0-9] token.
        var pattern = Pattern.compile("([0-9])");
        var matcher = pattern.matcher(token);
        if(matcher.find()){
            number = matcher.group(1).toInt()
            number -= 1
        }

        if(number > 9 || number < 0) number = 0 //If the character at the end is something higher than
        val person = if(number >= event.randomPersonList.size) null else event.randomPersonList[number] //The person to base the pronoun off of
        if(person != null)
            return token.replace("((%n[0-9]?\\S*)|(\\(?(N|n)ame[0-9]\\)?))".toRegex(), person.firstName) //Return the replaced token.
        else
            return "A Mysterious Stranger"
    }

    fun checkCantTravel():Boolean{
        val energy = SupplyManager.getSupply("energy")
        val tracks = ROVManager.ROVPartMap["track"]!!

        return (tracks.amt.toInt() == 0 && tracks.currHealth <= 0) || energy.amt.toInt() == 0
    }

    fun checkGameOverConditions():Pair<Boolean, String>{
        val storage = ROVManager.ROVPartMap["storage"]!!
        val ammo = SupplyManager.getSupply("ammo")
        val energy = SupplyManager.getSupply("energy")
        val edibles = SupplyManager.getSupply("edibles")
        val ROV = ROVManager.ROVPartMap["ROV"]!!
        val panels = SupplyManager.getSupply("panel")

        val cantGetEnergy = energy.amt <= 0 && panels.currHealth <= 0 && panels.amt <= 0 && edibles.amt <= 0
        val noStorage = storage.currHealth <= 0 && storage.amt.toInt() == 0
        val cantTravel = checkCantTravel() && ammo.amt.toInt() == 0

        val lost = cantTravel || noStorage || cantGetEnergy || ROV.currHealth <= 0f || GroupManager.numPeopleAlive == 0

        var evtName = ""
        if(lost){
            val tracks = SupplyManager.getSupply("track")
            val batteries = SupplyManager.getSupply("battery")

            if(panels.currHealth <= 0f && panels.amt <= 0f && energy.amt <= 0f)
                evtName = "LostPanel"
            else if(tracks.currHealth <= 0f && tracks.amt <= 0f && ammo.amt <= 0f)
                evtName = "LostTrack"
            else if(batteries.currHealth <= 0f && batteries.amt <= 0f && ammo.amt <= 0f)
                evtName = "LostBattery"
            else if(storage.currHealth <= 0f && storage.amt <= 0f)
                evtName = "LostStorage"
            else if(energy.amt <= 0f && edibles.amt <=0 && ammo.amt <= 0f)
                evtName = "LostEnergy"
            else if(tracks.currHealth <= 0f && tracks.amt <= 0f && ammo.amt <= 0f)
                evtName = "LostTrack"
            else if(ROV.currHealth <= 0f)
                evtName = "LostROV"
            else if(GroupManager.numPeopleAlive == 0)
                evtName = "LostLife"
        }

        return Pair(lost, evtName)
    }

    fun checkSupply(supply:SupplyManager.Supply, amtChanged:Float, amtBefore:Float):String{
        val isNewlyZero = supply.amt <= 0f && amtChanged != 0f
        var eventNameToCall = ""

        if(isNewlyZero){
            val ammo = SupplyManager.getSupply("ammo")

            when(supply.name){
                "panel" -> eventNameToCall = "LastPanel"
                "track" -> {
                    if(supply.amt <= 0 && ammo.amt > 0)
                        eventNameToCall = "NoTrack"
                    else
                        eventNameToCall = "LastTrack"
                }
                "battery"  -> {
                    if(supply.amt <= 0 && ammo.amt > 0)
                        eventNameToCall = "NoBattery"
                    else
                        eventNameToCall = "LastBattery"
                }
                "storage"  -> eventNameToCall = "LastStorage"
                "energy"  -> eventNameToCall = "NoEnergy"
            }
        }else{
            when(supply.name){
                "energy" -> {
                    val five = supply.maxAmount*0.05f
                    if (amtBefore > five && supply.amt <= five)
                        eventNameToCall = "LastEnergy"
                }
            }
        }

        return eventNameToCall
    }

    fun specialDeathTextReplacement(text:String, person: Person):String{
        var _text = replaceGender(text, person)
        _text = _text.replace("%d", person.firstName).replace("%t", formatTimeText(formatTime()))

        return _text
    }

    /**
     * Uses the game time to format time.
     */
    fun formatTime():Triple<Int, Int, Int>{
        return formatTime(GameStats.TimeInfo.totalTimeCounter.toInt())
    }

    /**
     * Uses passed in time to return a formatted time.
     */
    fun formatTime(hours:Int):Triple<Int, Int, Int>{
        val totalMonths = ( hours/(24*30)).toInt()
        val totalDays = ((hours - totalMonths*(24*30))/24).toInt()
        val totalHours =  (hours - (totalDays*24) - totalMonths*(24*30)).toInt()

        return Triple(totalMonths, totalDays, totalHours)
    }

    fun formatTimeText(formattedTime:Triple<Int, Int, Int>):String{
        val list:MutableList<String> = mutableListOf()

        if(formattedTime.first != 0) {
            list += "${formattedTime.first} months"
        }
        if(formattedTime.second != 0) {
            list += "${formattedTime.second} days"
        }
        if(formattedTime.third != 0) {
            list += "${formattedTime.third} hours"
        }

        var text = ""

        for(i in 0.rangeTo(list.size-1)){
            if(i == list.size-1 && list.size == 2){
                text += " and "

            //If we are at the end and the list size has more than 2 elements.
            }else if(i == list.size-1 && list.size > 2){
                text+=", and "

            //If we are not at the start but not at the end, use a comma
            }else if(i!=0 && list.size > 0){
                text += ", "
            }

            text += list[i]
        }

        return text
    }

    fun alterSupplies(size:String, lose:Boolean = false){
        val supplyList = listOf(SupplyManager.getSupply("energy"), SupplyManager.getSupply("edibles"), SupplyManager.getSupply("parts"), SupplyManager.getSupply("medkits"),
                SupplyManager.getSupply("wealth"), SupplyManager.getSupply("ammo"))

        val rndAmt:Pair<Float, Float> = if(size == "small") Pair(1f, 25f) else if(size == "medium") Pair(25f, 50f) else Pair(50f, 100f)
        val medkitAmount:Pair<Float, Float> = if(size == "small") Pair(0f, 0f) else if(size == "medium") Pair(1f, 2f) else Pair(2f, 4f)

        for(i in 0.rangeTo(supplyList.size-1)){
            val supply = supplyList[i]

            var amt = 0f
            if(i == 3) amt = MathUtils.random(medkitAmount.first, medkitAmount.second)
            else amt = MathUtils.random(rndAmt.first, rndAmt.second)

            if(lose) amt = -amt

            SupplyManager.addToSupply(supply, amt)
        }
    }

    fun applyReward(rewardName:String){
        val reward = Reward.rewardMap[rewardName]!!

        //For supplies
        for(i in 0.rangeTo(reward.supplies.size)){
            val supplyName = reward.supplies[i]
            val pair = Pair(reward.supplyAmounts[i][0], reward.supplyAmounts[i][1])

            SupplyManager.addToSupply(supplyName, MathUtils.random(pair.first, pair.second).toFloat())
        }

        //For parts...
        if(reward.parts.size > 0){
            val partName = reward.parts[MathUtils.random(0, reward.parts.size-1)]
            SupplyManager.addToSupply(partName, 1f)
        }
    }
}