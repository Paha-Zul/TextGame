package com.quickbite.rx2020.tests

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.Person
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.managers.TraitManager
import com.quickbite.rx2020.objects.Ailment

object TraitTest {

    private val listOfTraitsAdded = mutableListOf<DataManager.TraitJson>()

    fun testTraits(type:String = "skills", numPeopleToTest:Int = 5, numTraitsToTest:Int = 5, numModifiersToTest:Int = 20){
        val personList = mutableListOf<Person>()
        val traitList = when(type) {
            "professions" -> DataManager.traitList.professions.listOfTraits
            "skills" -> DataManager.traitList.skills.listOfTraits
            else -> DataManager.traitList.stateofbeing.listOfTraits
        }

        //Create the people
        for(i in 0 until numPeopleToTest){
            val randomName = DataManager.pullRandomName()
            val person = Person(randomName.first, randomName.second, randomName.third, 0L)
            personList += person
            println("Creating random person $person")
        }

        //Create the traits and assign them to people
        for(i in 0 until numTraitsToTest){
            val randomPerson = personList[MathUtils.random(personList.size - 1)]
            val randomSkill = traitList[MathUtils.random(traitList.size-1)]

            System.out.println("Testing ${randomSkill.name} on $randomPerson")
            TraitManager.addTrait(traitList[MathUtils.random(traitList.size-1)], randomPerson)
        }

        //Test the modifiers
        testModifierAmount(numModifiersToTest, people = personList)
        //Test the injuries
        testInjuryTraits(type)
    }

    fun testSkills(testAmount:Int = 5){
        val personList = mutableListOf<Person>()
        val skillList = DataManager.traitList.skills.listOfTraits

        for(i in 0 until testAmount){
            val randomName = DataManager.pullRandomName()
            val person = Person(randomName.first, randomName.second, randomName.third, 0L)
            personList += person

            val randomSkill = skillList[MathUtils.random(skillList.size-1)]

            System.out.println("Testing ${randomSkill.name} on $person")

            TraitManager.addTrait(skillList[MathUtils.random(skillList.size-1)], person)
        }

        testModifierAmount()
        testInjuryTraits("skills")
    }

    //Tests injuries to see if the traits correctly affect them. This is random testing
    private fun testInjuryTraits(type:String = "professions"){
        val listToUse = when (type) {
            "professions" -> DataManager.traitList.professions.listOfTraits
            "skills" -> DataManager.traitList.skills.listOfTraits
            else -> DataManager.traitList.stateofbeing.listOfTraits
        }

        val injuryDuration = listToUse.firstOrNull {
            it.effects.firstOrNull { it.affects == "addAilment" && it.subCommand == "duration" && it.scope == "global"} != null }
        val injuryDamage = listToUse.firstOrNull {
            it.effects.firstOrNull { it.affects == "addAilment" && it.subCommand == "damage"&& it.scope == "global" } != null }

        val injuryDurationIndividual = listToUse.firstOrNull {
            it.effects.firstOrNull { it.affects == "addAilment" && it.subCommand == "duration" && it.scope == "individual" } != null }
        val injuryDamageIndividual = listToUse.firstOrNull {
            it.effects.firstOrNull { it.affects == "addAilment" && it.subCommand == "damage" && it.scope == "individual"} != null }

        println()
        println("---- testing injuries for $type ----")

        //Add at least one person to the group manager for testing
        val person = GroupManager.addPerson(Person("Mike", "Thomas", 100f, true, 0L))
        person.addAilment(Ailment.AilmentLevel.Regular, Ailment.AilmentType.Injury)

        System.out.println("Before adding trait... Health: ${person.healthNormal}, Injury health: ${person.healthInjury}, Duration: ${person.ailmentList[0].hoursRemaining}")

        if(injuryDuration!= null) TraitManager.addTrait(injuryDuration)
        if(injuryDamage!= null) TraitManager.addTrait(injuryDamage)

        System.out.println("After adding trait... Health: ${person.healthNormal}, Injury health: ${person.healthInjury}, Duration: ${person.ailmentList[0].hoursRemaining}")

        if(injuryDuration!= null) TraitManager.removeTrait(injuryDuration)
        if(injuryDamage!= null) TraitManager.removeTrait(injuryDamage)

        System.out.println("After removing trait... Health: ${person.healthNormal}, Injury health: ${person.healthInjury}, Duration: ${person.ailmentList[0].hoursRemaining}")

        println("---- done testing injuries ----")
        println()
    }

    fun test(){
        System.out.println("-- Testing Traits --")

        for(i in 0.until(100)){
            val randomIndex = MathUtils.random(DataManager.traitList.professions.listOfTraits.size-1)
            val randomTrait = DataManager.traitList.professions.listOfTraits[randomIndex]
            TraitManager.addTrait(randomTrait, Person("Mike", "Thomas", 100f, true, 0L))
            System.out.println("Adding trait $randomTrait for Mike")

            listOfTraitsAdded += randomTrait
        }

        System.out.println("After adding traits")
        printDataFromMaps()

        System.out.println()
        System.out.println("Testing modifier amounts")
        testModifierAmount()
        System.out.println()

        removeTraits()
        System.out.println("After removing traits")
        printDataFromMaps()

        System.out.println("-- Done Testing Traits --")

        //Clear these both to make sure nothing gets stuck in here after testing
        TraitManager.individualTraitMap.clear()
        TraitManager.globalTraitMap.clear()
    }

    private fun printDataFromMaps(){
        System.out.println()

        System.out.println("Individuals")
        TraitManager.individualTraitMap.toList().forEach{ kv ->
            System.out.println("${kv.first}:${kv.second.first}:${kv.second.second}")
        } //Print out

        System.out.println()

        System.out.println("Globals")
        TraitManager.globalTraitMap.toList().forEach { kv1 ->
            System.out.println("${kv1.first}:${kv1.second.first}:${kv1.second.second}")
        }
    }

    private fun testModifierAmount(amountOfTimes:Int = 20, person:Person? = null, people:List<Person> = listOf()){
        val itemList = DataManager.getItemList()
        val peopleList = if(person != null) listOf(person) else people

        for(i in 0 until amountOfTimes){
            val randomItem = itemList[MathUtils.random(itemList.size - 1)]
            val baseAmount = MathUtils.random(50, 150).toFloat()
            val result:Pair<Float, Boolean>
            result = if(randomItem.type == "ROVPart")
                TraitManager.getTraitModifier("addRndAmt", subType = randomItem.type, people = peopleList)
            else
                TraitManager.getTraitModifier("addRndAmt", randomItem.name, people = peopleList)

            val modifiedAmount:Float=
                    if(result.second) //If we are using percent
                        baseAmount + baseAmount*(result.first/100f) //Divide by 100 to get actual percent
                    else
                        baseAmount + result.first //If not using percent, add it straight up

            System.out.println("Item ${randomItem.name}")
            System.out.println("Base amount: $baseAmount, Modified amount: $modifiedAmount, modifier: ${result.first}")
        }
    }

    private fun removeTraits(){
        listOfTraitsAdded.forEach{ trait ->
            TraitManager.removeTrait(trait, Person("Mike", "Thomas", 100f, true, 0L))
            System.out.println("Removing trait $trait for Mike")
        }
    }

}