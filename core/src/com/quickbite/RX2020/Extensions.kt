package com.quickbite.rx2020

import com.moandjiezana.toml.Toml
import java.text.DecimalFormat
import java.util.*

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

fun <T> Array<T>.shuffle() : Array<T>{
    val rg = Random()
    for (i in 0 until this.size) {
        val randomPosition = rg.nextInt(this.size);
        swap(this, i, randomPosition);
    }
    return this
}

fun <T> MutableList<T>.shuffle() : MutableList<T>{
    val rg : Random = Random();
    for (i in 0 until this.size) {
        val randomPosition = rg.nextInt(this.size);
        swap(this, i, randomPosition);
    }
    return this;
}

fun <T> swap(arr: MutableList<T>, i: Int, j: Int) : MutableList<T>{
    val tmp : T = arr[i]
    arr[i] = arr[j]
    arr[j] = tmp
    return arr
}

fun <T> swap(arr: Array<T>, i: Int, j: Int) : Array<T>{
    val tmp : T = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
    return arr;
}

fun Toml.getFloat(vararg strings:String):Float{
    var table = this
    strings.forEachIndexed {index, string ->
        when{
            index < strings.size - 1 -> table = table.getTable(string)
            else -> return table.getDouble(string).toFloat()
        }
    }
    Exception("Didn't find the value in toml table")
    return 0f
}