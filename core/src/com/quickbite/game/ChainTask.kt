package com.quickbite.game

/**
 * Created by Paha on 2/10/2016.
 * A fun little chain task class where a predicate and function is supplied. When the predicate is true, the chain task
 * will try to go to the next task. If null, nothing is called within update()
 */
class ChainTask(var predicate:(() -> Boolean)? = null, var func:(()->Unit)? = null, var finish:(()->Unit)? = null) {
    private var currChain:ChainTask? = this
    var chain:ChainTask? = null
        get
        private set

    var done:Boolean = false
        get() = currChain == null

    fun update(){
        if(!done) {
            //If the func is null, just fetch the next chain.
            if(currChain!!.func == null){
                currChain = currChain!!.chain
            }else {
                (currChain!!.func!!)() //Call the function

                //If the predicate is no null, call the predicate to check if we are done.
                if(currChain!!.predicate != null) {
                    if ((currChain!!.predicate!!)()) {
                        currChain?.finish?.invoke()
                        if(currChain!!.chain != null)
                            currChain = currChain!!.chain
                    }

                //If the predicate is null, we'll assume we only wanted to call the function once since
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