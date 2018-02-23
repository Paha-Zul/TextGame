package com.quickbite.rx2020.objects

/**
 * Generic supply class that holds info for supplies
 * @param name The name of the supply
 * @param abbrName The abbreviated name of the supply
 * @param displayName The display name of the supply
 * @param amt The initial amount of the supply
 * @param maxAmount The max amount the supply can stack to
 * @param currHealth The initial and current health of the supply
 * @param maxHealth The max health of the supply
 * @param affectedByHealth True if the supply's function is affected by the health of the supply (ex: energy gain from broken solar panels)
 */
class Supply(val name:String, val abbrName:String, val displayName:String, var amt:Float, val affectedByHealth:Boolean = false){
    var consumePerDay:Float = 0f

    var maxAmount:Int = 100000
    val baseMaxHealth:Float = 100f
    var maxHealth:Float = baseMaxHealth
    var currHealth = baseMaxHealth

    operator fun component1() = displayName
    operator fun component2() = amt
    operator fun component3() = maxAmount
}