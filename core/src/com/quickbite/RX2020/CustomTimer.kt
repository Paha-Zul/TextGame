package com.quickbite.rx2020

/**
 * Created by Paha on 2/7/2016.
 * Creates the timer as one time.
 * @param secondsInterval The interval to be used for the timer. If no delay is defined, the interval will be used for the initial timer delay.
 * @param _callback The callback to be
 */
class CustomTimer(private var secondsDelay: Float, private var _callback: (() -> Unit)? = null) {
    var callback:(()->Unit)?
        get() = _callback
        set(value){_callback = value}

    val expired:Boolean
        get() = currTime >= secondsInterval && currTime >= secondsDelay

    val startedDelay:Boolean
        get() = currTime >= secondsDelay

    var stopped:Boolean = false
    var currTime:Float = 0f
    var repeating:Boolean = false
    var secondsInterval:Float = -1f
    var started = false

    init{
        callback = _callback
    }

    /**
     * Creates the timer as repeating
     * @param secondsDelay The delay before the first firing of the timer.
     * @param secondsInterval The interval to be used for the timer. -1 defines no interval, which means a one shot timer.
     */
    constructor(secondsDelay:Float, secondsInterval: Float, callback:(()->Unit)? = null):this(secondsDelay, callback){
        repeating = true
        this.secondsInterval = secondsInterval
    }

    fun update(delta:Float){
        if(!stopped) {
            currTime += delta
            if (!started && startedDelay) { //If we haven't used the delay yet...
                started = true //Set started to true.
                finish()       //Finish and restart.
            }else if(expired){ //Otherwise, if we have expired
                finish() //Finish the timer.
            }
        }
    }

    /**
     * Finishes (and restarts if there is an interval) the timer when the timer expires.
     */
    private fun finish(){
        callback?.invoke()
        stop()
        if(secondsInterval >= 0)
            restart()
    }

    /**
     * Stops the timer.
     */
    fun stop() {
        stopped = true
    }

    /**
     * Starts the timer (if it hasn't expired)
     */
    fun start(){
        if(!expired)
            stopped = false
    }

    /**
     * Restarts the timer with the optional settings. This will not reset the timer so the seconds delay will not be used unless reset() is called.
     * @param secondsDelay The delay until the timer fires the first interval
     * @param secondsInterval The interval for the timer to fire at.
     */
    fun restart(secondsDelay: Float = this.secondsDelay, secondsInterval: Float = this.secondsInterval){
        currTime = 0f
        this.secondsDelay = secondsDelay
        this.secondsInterval = secondsInterval
        start()
    }

    /**
     * Resets the timer. This will start the timer new and use secondsDelay as the initial timer start delay.
     */
    fun reset(secondsDelay: Float = this.secondsDelay, secondsInterval: Float = this.secondsInterval){
        started = false
        restart(secondsDelay, secondsInterval)
    }

}