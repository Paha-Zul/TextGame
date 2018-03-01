package com.quickbite.rx2020.objects

import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.interfaces.IUpdateable

class Ailment(level: AilmentLevel, var type: AilmentType): IUpdateable {
    enum class AilmentType {Injury, Sickness}
    enum class AilmentLevel {Minor, Regular, Major, Trauma}
    val done:Boolean
        get() = hoursRemaining <= 0

    //Need this empty constructor for loading/saving to json files.
    private constructor():this(AilmentLevel.Minor, AilmentType.Injury)

    val totalDuration:Int
    val baseHpLostPerHour:Float
    val baseHPTakenByInjury:Int
    var hoursRemaining = 0
    var HPTakenByInjury = 0
    var hpLostPerHour = 0f

    init{
        when(level){
            AilmentLevel.Minor ->{ totalDuration = MathUtils.random(10*24, 30*24); baseHPTakenByInjury = MathUtils.random(0, 25); baseHpLostPerHour = 0.12f}
            AilmentLevel.Regular ->{ totalDuration = MathUtils.random(30*24, 50*24); baseHPTakenByInjury = MathUtils.random(25, 50); baseHpLostPerHour = 0.14f}
            AilmentLevel.Major ->{ totalDuration = MathUtils.random(50*24, 70*24); baseHPTakenByInjury = MathUtils.random(50, 75); baseHpLostPerHour = 0.19f}
            AilmentLevel.Trauma ->{ totalDuration = MathUtils.random(70*24, 90*24); baseHPTakenByInjury = MathUtils.random(75, 100); baseHpLostPerHour = 0.29f}
        }

        hoursRemaining = totalDuration
        hpLostPerHour = baseHpLostPerHour
        HPTakenByInjury = baseHPTakenByInjury
    }

    override fun update(delta: Float) {}
    override fun updateHourly(delta: Float) {
        this.hoursRemaining--
    }
}