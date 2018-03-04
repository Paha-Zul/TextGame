package com.quickbite.rx2020

import com.quickbite.rx2020.managers.GroupManager
import com.quickbite.rx2020.managers.TraitManager

object Globals {
    val baseSurvivorHP = 100.0f
    var maxSurvivorHP = baseSurvivorHP
        private set

    private val baseROVHP = 100.0f
    var maxROVHP = baseROVHP
        private set

    val baseROVTravelSpeed = 10f
    var ROVTravelSpeed = baseROVTravelSpeed

    init{
        TraitManager.addListener("ROVMaxHp", "", {traitEffect, removing, _ ->
            if(!removing) //If we are not removing, add health
                maxROVHP += baseROVHP*(traitEffect.amount/100f)
            else //If we are removing, subtract health
                maxROVHP -= baseROVHP*(traitEffect.amount/100f)
        })

        //Change of survivor max HP global modifier
        TraitManager.addListener("survivorMaxHP", "", {traitEffect, removing, person ->
            val amt = if(!removing) //If we are not removing, add health
                baseSurvivorHP*(traitEffect.amount/100f)
            else //If we are removing, subtract health
                -baseSurvivorHP*(traitEffect.amount/100f)

            if(traitEffect.scope == "global") //If the scope is global...
                maxSurvivorHP += amt
            else if(traitEffect.scope == "individual") //If it's individual, only change the bonus max health of a specific survivor
                person!!.bonusMaxHealth += amt
        })

        //Changing of travel speed global modifier
        TraitManager.addListener("travelSpeed", "", {traitEffect, removing, _ ->
            val amt = if(!removing) //If we are not removing, positive!
                traitEffect.amount
            else //If we are removing, negative!
                -traitEffect.amount

            ROVTravelSpeed += amt
            ROVTravelSpeed.clamp(baseROVTravelSpeed, 100f) //100 is some arbitrary high number
        })

        //When someone gets an "addAilment" trait, we need to modify existing ailments
        //This doesn't really affect a global variable, but since it can apply to ALL ailments it's gotta go here
        TraitManager.addListener("addAilment", "duration", {traitEffect, removing, personWithTrait ->
            //If the scope is global, we need to add or remove the bonuses from ALL people
            if(traitEffect.scope == "global") {
                val peopleList = GroupManager.getPeopleList()
                //If not removing (adding), subtract the duration amount from all ailments of all people
                if (!removing) {
                    peopleList.forEach { person ->
                        person.ailmentList.forEach { ailment ->
                            //We subtract here cause traitEffect.amount will be positive
                            ailment.hoursRemaining -= (ailment.totalDuration * (traitEffect.amount / 100f)).toInt()
                        }
                    }
                //If we are removing, add the duration back on to all ailments on all people
                } else {
                    peopleList.forEach { person ->
                        person.ailmentList.forEach { ailment ->
                            //We add here because traitEffect.amount will be positive
                            ailment.hoursRemaining += (ailment.totalDuration * (traitEffect.amount / 100f)).toInt()
                        }
                    }
                }

            //If the scope is individual, we simply modify the person with the trait passed in.
            }else if(traitEffect.scope == "individual"){
                //If not removing (adding), remove the duration amount from the trait person passed in
                if (!removing)
                     //We subtract here because traitEffect.amount will be positive
                    personWithTrait!!.ailmentList.forEach { it.hoursRemaining -= (it.totalDuration*traitEffect.amount/100f).toInt() }

                //If we are removing, add the duration back on to the person with the trait
                else
                    //We add here because traitEffect.amount will be positive
                    personWithTrait!!.ailmentList.forEach { it.hoursRemaining += (it.totalDuration*traitEffect.amount/100f).toInt() }
            }
        })

        //When someone gets an "addAilment" trait, we need to modify existing ailments
        //This doesn't really affect a global variable, but since it can apply to ALL ailments it's gotta go here
        TraitManager.addListener("addAilment", "damage", {traitEffect, removing, personWithTrait ->
            //If the scope is global, we need to add or remove the bonuses from ALL people
            if(traitEffect.scope == "global") {
                val peopleList = GroupManager.getPeopleList()

                //If not removing (adding), subtract the duration amount from all ailments of all people
                if (!removing) {
                    peopleList.forEach { person ->
                        person.ailmentList.forEach { ailment ->
                            //We subtract here because traitEffect.amount will be positive
                            ailment.HPTakenByInjury -= (ailment.baseHPTakenByInjury * (traitEffect.amount / 100f)).toInt()
                            println("Such")
                        }
                    }
                //If we are removing, add the duration back on to all ailments on all people
                } else {
                    peopleList.forEach { person ->
                        person.ailmentList.forEach { ailment ->
                            //We add here because traitEffect.amount will be positive
                            ailment.HPTakenByInjury += (ailment.baseHPTakenByInjury * (traitEffect.amount / 100f)).toInt()
                        }
                    }
                }

                //Reapply all the ailments on the person
                peopleList.forEach { it.reapplyExistingAilments() }

            //If the scope is individual, we simply modify the person with the trait passed in.
            }else if(traitEffect.scope == "individual"){
                //If not removing (adding), remove the duration amount from the trait person passed in
                if (!removing)
                    //We subtract here because traitEffect.amount will be positive
                    personWithTrait!!.ailmentList.forEach { it.HPTakenByInjury -= (it.baseHPTakenByInjury*traitEffect.amount/100f).toInt() }

                //If we are removing, add the duration back on to the person with the trait
                else
                    //We add here because traitEffect.amount will be positive
                    personWithTrait!!.ailmentList.forEach { it.HPTakenByInjury += (it.baseHPTakenByInjury*traitEffect.amount/100f).toInt() }

                //Reapply all the ailments on the person
                personWithTrait.reapplyExistingAilments()
            }
        })
    }
}