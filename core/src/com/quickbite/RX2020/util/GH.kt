package com.quickbite.rx2020.util

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
            newDesc += iterateText(desc, event)
        }

        //Add the descriptions back to the event.
        event.modifiedDescription = newDesc.toTypedArray()
    }

    fun replaceChoiceForEvent(text:String, event:GameEventManager.EventJson):String{
        return iterateText(text, event)
    }

    private fun iterateText(text:String, event:GameEventManager.EventJson):String{
        val tokens = text.split(" ") //Split by spaces
        val newTokens:MutableList<String> = mutableListOf()

        //For each token....
        for(token in tokens){
            var newToken = token

            //If the token matches this pattern, let's send it to the gender changes!
            if(token.matches("(\\S*\\(?(H|h)(is|im|e)[0-9]\\(?\\S*)".toRegex()))
                newToken = changeGender(token, event)

            //If it matches a name, replace it.
            if(token.matches("((%n[0-9]?\\S*)|(\\(?(N|n)ame[0-9]\\)?))".toRegex()))
                newToken = replaceName(token, event)

            //Add it to the new token list.
            newTokens += newToken
        }

        //Join all the new tokens back up.
        return newTokens.joinToString(" ")
    }

    fun changeGender(token:String, event:GameEventManager.EventJson):String{
        var _token = ""

        //First, find the he/him/his [0-9] token.
        var pattern = Pattern.compile("((H|h)(is|im|e)[0-9])");
        var matcher = pattern.matcher(token);
        if(matcher.find()){
            _token = matcher.group(1)
        }

        var number:Int = 0

        pattern = Pattern.compile("([0-9])");
        matcher = pattern.matcher(_token);
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
            if(male) pronoun = "im"
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
        val panels = ROVManager.ROVPartMap["panel"]!!

        return tracks.amt.toInt() == 0 && tracks.currHealth <= 0 && energy.amt.toInt() == 0
        && panels.currHealth <= 0 && panels.amt.toInt() == 0
    }

    fun checkGameOverConditions():Boolean{
        val storage = ROVManager.ROVPartMap["storage"]!!
        val ammo = SupplyManager.getSupply("ammo")

        return (checkCantTravel() && ammo.amt.toInt() == 0) || (storage.currHealth <= 0 && storage.amt.toInt() == 0) || GroupManager.numPeopleAlive == 0 ||
                (SupplyManager.getSupply("energy").amt <= 0 && SupplyManager.getSupply("edibles").amt <= 0)
    }
}