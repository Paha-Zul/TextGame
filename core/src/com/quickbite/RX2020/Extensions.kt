package com.quickbite.rx2020

import java.text.DecimalFormat

/**
 * Created by Paha on 4/6/2016.
 */

fun Float.format(decimals:Int) = DecimalFormat("#.0").format(this)

fun Float.clamp(min:Float, max:Float):Float{
    if(this <= min) return min
    if(this >= max) return max
    return this
}

fun Int.clamp(min:Int, max:Int):Int{
    if(this <= min) return min
    if(this >= max) return max
    return this
}