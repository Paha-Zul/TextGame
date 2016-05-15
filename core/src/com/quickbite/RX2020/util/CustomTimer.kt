package com.quickbite.rx2020.util

/**
 * Created by Paha on 2/7/2016.
 * Creates the timer as one time.
 * @param secondsInterval The interval to be used for the timer. If no delay is defined, the interval will be used for the initial timer delay.
 * @param _callback The callback to be
 */
class CustomTimer(private var secondsDelay: Float, private var secondsInterval: Float, private var _callback: (() -> Unit)? = null){
    companion object{
        val gameTimerList:MutableList<CustomTimer> = mutableListOf()

        fun updateGameTimerList(delta:Float){
            val iter = gameTimerList.iterator()
            while(iter.hasNext()){
                val next = iter.next()
                next.update(delta)
                if(next.done)
                    iter.remove()
            }
        }

        fun addGameTimer(timer:CustomTimer){
            gameTimerList.add(timer)
        }
    }

    var callback:(()->Unit)?
        get() = _callback
        set(value){_callback = value}

    /** If the timer is done. */
    val done:Boolean
        get() = currTime >= secondsInterval && currTime >= secondsDelay

    /** If the delay has been fired once */
    private val startedDelay:Boolean
        get() = currTime >= secondsDelay

    var stopped:Boolean = false
        get
        private set

    private var currTime:Float = 0f
    private var intervalStarted = false

    init{
        callback = _callback
    }

    /**
     * Creates the timer as repeating
     * @param secondsDelay The delay before the first firing of the timer.
     * @param secondsInterval The interval to be used for the timer. -1 defines no interval, which means a one shot timer.
     */
    constructor(secondsDelay:Float, callback:(()->Unit)? = null):this(secondsDelay, -1f, callback){

    }

    fun update(delta:Float){
        //If we're not stopped...
        if(!stopped) {
            currTime += delta   //Increment timer
            if (!intervalStarted && startedDelay) {     //If we haven't used the delay yet...
                intervalStarted = true                  //Set interval started to true.
                finish()                                //Finish and restart.
            }else if(done){     //Otherwise, if is done
                finish()        //Finish the timer.
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
        if(!done)
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
        intervalStarted = false
        restart(secondsDelay, secondsInterval)
    }

}