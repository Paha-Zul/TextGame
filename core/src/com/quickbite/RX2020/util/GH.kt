package com.quickbite.rx2020.util

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.managers.*
import com.quickbite.rx2020.objects.Supply
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

    /**
     * Executes an events resulting actions
     * @param event The EventJson object to get the actions from
     */
    fun executeEventActions(event: GameEventManager.EventJson) {
        val list = event.resultingAction
            list?.filter { it.isNotEmpty() } //For each set (list) of parameters that is not empty
                    //Call the EventManager using it[0] as the name and it[1-size-1] as the parameters
                    ?.forEach { EventManager.callEvent(it[0], it.slice(1.rangeTo(it.size - 1))) }
    }

    /**
     * Returns an event from the choice of another event
     * @param currEvent The event to get the next event (child) from
     * @param choiceText The choice text selected and used to get the next event
     * @return Another event if found (is a child of the event), null otherwise
     */
    fun getEventFromChoice(currEvent:GameEventManager.EventJson, choiceText:String):GameEventManager.EventJson?{
        return currEvent.selectChildEvent(choiceText)
    }

    /**
     * Takes a double array of restrictions, mainly those used in the search activities, and parses/checks them.
     * @param restrictionList A double array that contains a list of separate tokens which make up the restriction, ie: ("edibles", "<", "10")
     * @return True if the restrictions were passed, false otherwise.
     */
    fun parseAndCheckRestrictions(restrictionList:Array<Array<String>>):Triple<Boolean, String, String>{
        var triple = Triple(true, "", "")
        restrictionList.forEach { params ->
            triple = parseAndCheckRestrictions(params)
            if(!triple.first) //If we *didn't* pass the restriction test, return out of the lambda and return the pair.
                return@forEach
        }

        return triple
    }

    /**
     * Will take a single string, split by spaces, and check if the restriction has passed.
     * @param restriction The restriction to pass.
     * @return True if the restriction passed, false otherwise.
     */
    fun parseAndCheckRestrictions(restriction:String):Triple<Boolean, String, String>{
        return parseAndCheckRestrictions(restriction.split(" ").toTypedArray())
    }

    //TODO Fix this javadoc
    /**
     * Parses an array of strings into a check of restrictions (ex: 'food' '>' '0' will be converted into food>0)
     * @param restrictions An array of strings (usually 3) to indicate a check.
     * @return A Triple that holds the following:
     * <ul>
     *     <li> true/false if the check passed </li>
     *     <li> the name of the thing we're checking (like 'food') </li>
     *     <li> the operation used (<,<=, etc...) </li>
     * </ul>
     */
    private fun parseAndCheckRestrictions(restrictions:Array<String>):Triple<Boolean, String, String>{
        //If there's nothing to check, return true
        if(restrictions.isEmpty() || restrictions[0].isEmpty())
            return Triple(true, "", "")

        var passed = true

        val name:String = restrictions[0]
        val operation:String = restrictions[1]
        val amount:Int = restrictions[2].toInt()

        var amtToCheck = 0
        if(name == "ROV")
            amtToCheck = ROVManager.ROVPartMap["ROV"]!!.currHealth.toInt()
        else
            amtToCheck = SupplyManager.getSupply(name).amt.toInt()

        //Perform the operation
        when(operation){
            "<" -> passed = amtToCheck < amount
            "<=" -> passed =  amtToCheck <= amount
            ">" -> passed =  amtToCheck > amount
            ">=" -> passed =  amtToCheck >= amount
            else -> IllegalArgumentException("Must be one of the following strings: \"<\" \"<=\" \">\" \">=\"")
        }

        return Triple(passed, name, operation)
    }

    fun getRestrictionFailReason(reason:String, operator:String):String{
        var text:String
        when(operator){
            "<" -> text = "Too much "
            "<=" -> text = "Too much "
            ">" -> text = "Not enough "
            ">=" -> text = "Not enough "
            else -> text = "operator? "
        }
        when(reason){
            "ROV" -> text += "ROV health!"
            else -> text += "$reason!"
        }

        return text
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

        val pronounPattern = Pattern.compile("(([Hh])(is|im|e)[0-9])")
        val numberPattern = Pattern.compile("([0-9])")

        //For each token.... (which is each word in the text)
        for(token in tokens){
            var newToken = token

            //If the token matches this pattern, let's send it to the gender changes!
            if(token.matches("(\\S*\\(?([Hh])(is|im|e)[0-9]\\(?\\S*)".toRegex()))
                newToken = replaceGender(token, event, pronounPattern, numberPattern)

            //If it matches a name, replace it.
            if(token.matches("((%n[0-9]?)|(\\(?([Nn])ame[0-9]\\)?))\\S*".toRegex()))
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

        val pronounPattern = Pattern.compile("(([Hh])(is|im|e)[0-9])")

        //For each token....
        for(token in tokens){
            var newToken = token

            //If the token matches this pattern, let's send it to the gender changes!
            if(token.matches("(\\S*\\(?([Hh])(is|im|e)[0-9]\\(?\\S*)".toRegex()))
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
        var matcher = pronounPattern.matcher(token)
        if(matcher.find()){
            _token = matcher.group(1)
        }

        var number:Int = 0

        matcher = numberPattern.matcher(_token)
        if(matcher.find()){
            number = matcher.group(1).toInt()
            number -= 1 //We do this because array indexes start at 0. So Name1 refers to names[0]
        }

        var pronoun = ""                                    //The pronoun that will be changed
        val person = if(number >= event.randomPersonList.size) null else event.randomPersonList[number] //The person to base the pronoun off of
        val male = person?.male ?: true   //If it is of the male gender

        if(_token.matches("(([Hh])is[0-9])".toRegex())){
            pronoun = if(male) "his"
            else "her"
        }else if(_token.matches("(([Hh])e[0-9])".toRegex())){
            pronoun = if(male) "he"
            else "she"
        }else if(_token.matches("(([Hh])im[0-9])".toRegex())){
            pronoun = if(male) "him"
            else "her"
        }

        //Change to uppercase if needed
        if(_token[0].isUpperCase())
            pronoun = pronoun.replaceRange(0, 1, pronoun[0].toUpperCase().toString())

        //Basically, if we have a situation like "I want his3!" the pronoun might only be "her" after stripping anything else, so we take the pronoun
        //and replace the token "his3!" with "her!" and assign it back to the pronoun. Return this!
        pronoun = token.replace("(\\(?([Hh])(is|im|e)[0-9]\\)?)".toRegex(), pronoun)

        return pronoun
    }

    fun replaceGender(token:String, person:Person, pronounPattern:Pattern):String{
        var _token = ""

        //First, find the he/him/his [0-9] token.
        val matcher = pronounPattern.matcher(token)
        if(matcher.find()){
            _token = matcher.group(1)
        }

        var pronoun = ""        //The pronoun that will be changed
        val male = person.male  //If it is of the male gender

        if(_token.matches("(([Hh])is[0-9])".toRegex())){
            pronoun = if(male) "his"
            else "her"
        }else if(_token.matches("(([Hh])e[0-9])".toRegex())){
            pronoun = if(male) "he"
            else "she"
        }else if(_token.matches("(([Hh])im[0-9])".toRegex())){
            pronoun = if(male) "him"
            else "her"
        }

        //Change to uppercase if needed
        if(_token[0].isUpperCase())
            pronoun = pronoun.replaceRange(0, 1, pronoun[0].toUpperCase().toString())

        //Basically, if we have a situation like "I want his3!" the pronoun might only be "her" after stripping anything else, so we take the pronoun
        //and replace the token "his3!" with "her!" and assign it back to the pronoun. Return this!
        pronoun = token.replace("(\\(?([Hh])(is|im|e)[0-9]\\)?)".toRegex(), pronoun)

        return pronoun
    }

    /**
     * Replaces a token with a name from the event
     * @param token The token to parse
     * @param event The GameEvent to use for text displaying and stuff
     * @return Returns a new token with the replaced name
     */
    private fun replaceName(token:String, event:GameEventManager.EventJson):String{
        var number = 0

        //First, find the he/him/his [0-9] token.
        val pattern = Pattern.compile("([0-9])")
        val matcher = pattern.matcher(token)
        if(matcher.find()){
            number = matcher.group(1).toInt() //This grabs the number in the brackets, for example [1]
            number -= 1 //Since our text starts with [1] but code uses [0] as a start, subtract one to get code friendly numbers
        }

        if(number > 9 || number < 0) number = 0 //If the character at the end is something higher than
        event.numOfPeopleInEvent = Math.max(event.numOfPeopleInEvent, number) //Take the greater of the two and store it in the numOfPeopleInEvent
        val person = if(number >= event.randomPersonList.size) null else event.randomPersonList[number] //The person to base the pronoun off of
        if(person != null)
            return token.replace("((%n[0-9]?\\S*)|(\\(?([Nn])ame[0-9]\\)?))".toRegex(), person.firstName) //Return the replaced token.
        else
            return "A Mysterious Stranger"
    }

    fun checkCantTravel():Boolean{
        val energy = SupplyManager.getSupply("energy")
        val tracks = ROVManager.ROVPartMap["track"]!!
        val panels = ROVManager.ROVPartMap["panel"]!!
        val battery = ROVManager.ROVPartMap["battery"]!!

        if(battery.currHealth <= 0) SupplyManager.setSupplyAmount("energy", 0f)

        return tracks.currHealth <= 0 || energy.amt <= 0
    }

    /**
     * Checks for the conditions of game over
     * @return A pair that holds: True/false if we lost/didn't lose, and the reason we lost.
     */
    fun checkGameOverConditions():Pair<Boolean, String>{
        val storage = ROVManager.ROVPartMap["storage"]!!
        val ammo = SupplyManager.getSupply("ammo")
        val energy = SupplyManager.getSupply("energy")
        val edibles = SupplyManager.getSupply("edibles")
        val ROV = ROVManager.ROVPartMap["ROV"]!!
        val panels = SupplyManager.getSupply("panel")
        val battery = SupplyManager.getSupply("battery")
        val tracks = SupplyManager.getSupply("track")

        val cantGetEnergy = energy.amt <= 0 && edibles.amt <= 0 && ammo.amt <= 0
        val noStorage = storage.currHealth <= 0 && storage.amt.toInt() == 0
        val noTracks =  tracks.currHealth <= 0 && tracks.amt <= 0 && ammo.amt.toInt() == 0
        val noEnergy = battery.currHealth <= 0 && battery.amt <= 0 && energy.amt <= 0 && ammo.amt <= 0

        val lost = (checkCantTravel() && cantGetEnergy) || ROV.currHealth <= 0f || noTracks || GroupManager.numPeopleAlive == 0 || noStorage

        var reason = ""
        if(lost){

            if(panels.currHealth <= 0f && panels.amt <= 0f && energy.amt <= 0f)
                reason = "LostPanel"
            else if(noTracks)
                reason = "LostTrack"
            else if(battery.currHealth <= 0f && battery.amt <= 0f && ammo.amt <= 0f)
                reason = "LostBattery"
            else if(storage.currHealth <= 0f && storage.amt <= 0f)
                reason = "LostStorage"
            else if(energy.amt <= 0f && edibles.amt <=0 && ammo.amt <= 0f)
                reason = "LostEnergy"
            else if(ROV.currHealth <= 0f)
                reason = "LostROV"
            else if(GroupManager.numPeopleAlive == 0)
                reason = "LostLife"
            else
                reason = "I don't know?"
        }

        return Pair(lost, reason)
    }

    fun checkSupplyAmount(supply: Supply, amtChanged:Float, amtBefore:Float):String{
        val isNewlyZero = supply.amt <= 0f && amtBefore > 0
        var eventNameToCall = ""

        if(isNewlyZero){
            val ammo = SupplyManager.getSupply("ammo")

            when(supply.name){
                "panel" -> eventNameToCall = "LastPanel"
                "track" -> {
                    if(supply.amt <= 0 && supply.currHealth <= 0)
                        eventNameToCall = "NoTrack"
                    else if(supply.amt <= 0)
                        eventNameToCall = "LastTrack"
                }
                "battery"  -> {
                    if(supply.amt <= 0)
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

    fun checkSupplyHealth(supply:Supply, amtChanged:Float, amtBefore:Float){
        if(supply.currHealth <= 0f && amtBefore > 0){
            when(supply.name){
                "battery" -> {
                    SupplyManager.setSupplyAmount(supply, 0f)
                    EventManager.callEvent("scheduleEvent", "BatteryBreak", "1", "1")
                }
                "track" -> {
                    if(supply.amt <= 0)
                        EventManager.callEvent("scheduleEvent", "NoTrack", "1", "1")
                    else
                        EventManager.callEvent("scheduleEvent", "TrackBreak", "1", "1")
                }
                "storage" -> EventManager.callEvent("scheduleEvent", "StorageBreak", "1", "1")
                "panel" -> EventManager.callEvent("scheduleEvent", "PanelTrack", "1", "1")
            }
        }
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
        val totalMonths = ( hours/(24*30))
        val totalDays = ((hours - totalMonths*(24*30))/24)
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

    /**
     * Applies a preset reward base on the reward name. Can also be used to lose a certain reward
     * @param rewardName The name of the reward to give. These are defined in rewards.json
     * @param lose False (default) if we should gain the reward, True if we should lose the reward
     */
    fun applyReward(rewardName:String, lose:Boolean = false){
        val reward = Reward.rewardMap[rewardName]!!

        //For supplies
        for(i in 0 until reward.supplies.size){
            val supplyName = reward.supplies[i]
            val mult = if(supplyName == "edibles") GroupManager.numPeopleAlive else 1
            val pair = Pair(reward.supplyAmounts[i][0]*mult, reward.supplyAmounts[i][1]*mult)

            if(lose)
                SupplyManager.addToSupply(supplyName, -MathUtils.random(pair.first, pair.second).toFloat())
            else
                SupplyManager.addToSupply(supplyName, MathUtils.random(pair.first, pair.second).toFloat())
        }

        //For parts...
        if(reward.parts.isNotEmpty()){
            val partName = reward.parts[MathUtils.random(0, reward.parts.size-1)]
            SupplyManager.addToSupply(partName, 1f)
        }
    }
}