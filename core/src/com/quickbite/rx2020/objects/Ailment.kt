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

    var hoursRemaining = 0
    var hpLost = 0
    var hpLostPerHour = 0f

    init{
        when(level){
            AilmentLevel.Minor ->{ hoursRemaining = MathUtils.random(10*24, 30*24); hpLost = MathUtils.random(0, 25); hpLostPerHour = 0.12f}
            AilmentLevel.Regular ->{ hoursRemaining = MathUtils.random(30*24, 50*24); hpLost = MathUtils.random(25, 50); hpLostPerHour = 0.14f}
            AilmentLevel.Major ->{ hoursRemaining = MathUtils.random(50*24, 70*24); hpLost = MathUtils.random(50, 75); hpLostPerHour = 0.19f}
            AilmentLevel.Trauma ->{ hoursRemaining = MathUtils.random(70*24, 90*24); hpLost = MathUtils.random(75, 100); hpLostPerHour = 0.29f}
        }
    }

    override fun update(delta: Float) {}
    override fun updateHourly(delta: Float) {
        this.hoursRemaining--
    }
}