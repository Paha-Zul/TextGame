package com.quickbite.game

/**
 * Created by Paha on 2/7/2016.
 * Creates the timer as one time.
 */
class Timer(private var _callback:(()->Unit)?, private var secondsDelay:Float) {
    var callback:(()->Unit)?
        get() = _callback
        set(value){_callback = value}

    val done:Boolean
        get() = currTime >= secondsDelay

    var stopped:Boolean = false
    var currTime:Float = 0f
    var repeating:Boolean = false
    var secondInterval:Float = 0f

    init{
        callback = _callback
    }

    /**
     * Creates the timer as repeating
     */
    constructor(callback:(()->Unit)?, secondsDelay:Float, secondsInterval: Float):this(callback, secondsDelay){
        repeating = true
    }

    constructor():this(null, 0f, 0f){

    }

    fun update(delta:Float){
        if(!stopped) {
            currTime += delta
            if (done) {
                stop()
                callback?.invoke()
            }
        }
    }

    fun stop(){
        stopped = true
    }

    fun start(){
        if(!done)
            stopped = false
    }

    fun restart(){
        currTime = 0f
        start()
    }

    fun restart(secondDelay: Float){
        this.secondsDelay = secondsDelay
        repeating = false
        restart()
    }

    fun restart(secondDelay:Float, secondInterval:Float){
        this.secondsDelay = secondsDelay
        this.secondInterval = secondInterval
        repeating = true
        restart()
    }
}