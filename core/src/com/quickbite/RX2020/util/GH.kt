package com.quickbite.rx2020.util

import com.quickbite.rx2020.managers.*

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

    fun parseAndCheckRestrictions(restrictionList:Array<Array<String>>):Boolean{
        var passed = true
        restrictionList.forEach { params ->
            var name:String = params[0]
            var operation:String = params[1]
            var amount:Int = params[2].toInt()

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
        }

        return passed
    }

    fun replaceEventDescription(event:GameEventManager.EventJson){
        val replaceName:(token:String) -> String = {token ->
            val _token = token.replace("[^a-zA-Z0-9]","")       //Strip anything like periods
            var number:Int = _token[_token.length-1].toInt()
            if(number > 9) number = 0 //If the character at the end is something higher than
            val person = if(number >= event.randomPersonList.size) null else event.randomPersonList[number] //The person to base the pronoun off of
            if(person != null)
                token.replace("%n[0-9]?".toRegex(), person.firstName) //Return the replaced token.
            else
                "Mysterious Stranger"
        }

        val changeGender:(token:String, type:Int) -> String = {token, type ->
            val _token = token.replace("[^a-zA-Z0-9]","")       //Strip anything like periods
            val number:Int = _token[_token.length-1].toInt()    //Get the number associated with this pronoun.
            var pronoun = ""                                    //The pronoun that will be changed
            val person = if(number >= event.randomPersonList.size) null else event.randomPersonList[number] //The person to base the pronoun off of
            var male = if(person!=null) person.male else true   //If it is of the male gender

            if(type == 0){          //His
                if(male) pronoun = "his"
                else pronoun = "her"
            }else if(type == 1){    //He
                if(male) pronoun = "he"
                else pronoun = "she"
            }else if(type == 2){    //Him
                if(male) pronoun = "his"
                else pronoun = "her"
            }

            //Change to uppercase if needed
            if(_token[0].isUpperCase())
                pronoun.replaceRange(0, 1, pronoun[0].toUpperCase().toString())

            //Basically, if we have a situation like "I want his3!" the pronoun might only be "her" after stripping anything else, so we take the pronoun
            //and replace the token "his3!" with "her!" and assign it back to the pronoun. Return this!
            pronoun = token.replace("(H|h)(is|im|e)[0-9]".toRegex(), pronoun)

            pronoun
        }

        val newDesc:MutableList<String> = mutableListOf()

        //For each description...
        for(desc in event.description){
            val tokens = desc.split(" ") //Split by spaces
            val newTokens:MutableList<String> = mutableListOf()

            //For each token....
            for(token in tokens){
                var newToken = token

                //Figure out what it matches.
                if(token.matches("(H|h)is[0-9]\\S*".toRegex())){
                    newToken = changeGender(token, 0)
                }else if(token.matches("(H|h)e[0-9]\\S*".toRegex())){
                    newToken = changeGender(token, 1)
                }else if(token.matches("(H|h)im[0-9]\\S*".toRegex())){
                    newToken = changeGender(token, 2)
                }else if(token.matches("%n[0-9]?\\S*".toRegex()))
                    newToken = replaceName(token)

                //Add it to the new token list.
                newTokens += newToken
            }

            //Join all the new tokens back up.
            newDesc += newTokens.joinToString(" ")
        }

        //Add the descriptions back to the event.
        event.modifiedDescription = newDesc.toTypedArray()
    }

    fun checkCantTravel():Boolean{
        val energy = ROVManager.ROVPartMap["energy"]!!
        val tracks = ROVManager.ROVPartMap["track"]!!
        val panels = ROVManager.ROVPartMap["panel"]!!

        return energy.amt.toInt() == 0 && tracks.currHealth <= 0 && energy.amt.toInt() == 0
        && panels.currHealth <= 0 && panels.amt.toInt() == 0
    }

    fun checkGameOverConditions():Boolean{
        val storage = ROVManager.ROVPartMap["storage"]!!
        val ammo = SupplyManager.getSupply("ammo")

        return (checkCantTravel() && ammo.amt.toInt() == 0) || storage.amt.toInt() == 0 || GroupManager.numPeopleAlive == 0
    }
}