package com.quickbite.rx2020

/**
 * Created by Paha on 4/5/2016.
 *
 * A data class to hold some info regarding a result
 */
data class Result(val name:String, var amt:Float, val desc:String = "", var timeLastUpdated:Double = 0.0)