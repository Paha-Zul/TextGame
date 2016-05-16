package com.quickbite.rx2020

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
    val rg : Random = Random();
    for (i in 0..this.size - 1) {
        val randomPosition = rg.nextInt(this.size);
        swap(this, i, randomPosition);
    }
    return this;
}

fun <T> MutableList<T>.shuffle() : MutableList<T>{
    val rg : Random = Random();
    for (i in 0..this.size - 1) {
        val randomPosition = rg.nextInt(this.size);
        swap(this, i, randomPosition);
    }
    return this;
}

fun <T> swap(arr: MutableList<T>, i: Int, j: Int) : MutableList<T>{
    val tmp : T = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
    return arr;
}

fun <T> swap(arr: Array<T>, i: Int, j: Int) : Array<T>{
    val tmp : T = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
    return arr;
}