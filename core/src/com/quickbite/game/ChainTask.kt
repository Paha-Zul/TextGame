package com.quickbite.game

/**
 * Created by Paha on 2/10/2016.
 * A fun little chain task class where a predicate and function is supplied. When the predicate is true, the chain task
 * will try to go to the next task. If null, nothing is called within update()
 */
class ChainTask(var predicate:(() -> Boolean)?, var func:(()->Unit)?) {
    private var currChain:ChainTask? = this
    private var chain:ChainTask? = null

    constructor():this(null, null)

    var done:Boolean = false
        get() = currChain == null

    fun update(delta:Float){
        if(!done) {
            //If the func is null, just fetch the next chain.
            if(currChain!!.func == null){
                currChain = currChain!!.chain
            }else {
                (currChain!!.func!!)() //Call the function

                //If the predicate is no null, call the predicate to check if we are done.
                if(currChain!!.predicate != null) {
                    if ((currChain!!.predicate!!)()) {
                        currChain = currChain!!.chain
                    }

                //If the predicate is null, we'll assum we only wanted to call the function once since
                //no predicate means infinite.
                }else{
                    currChain = currChain!!.chain
                }
            }
        }
    }

    fun setChain(chainTask:ChainTask):ChainTask{
        chain = chainTask
        return chain!!
    }
}