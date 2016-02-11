package com.quickbite.game

/**
 * Created by Paha on 2/10/2016.
 */
class ChainTask(val predicate:() -> Boolean, val func:()->Unit) {
    private var currChain:ChainTask? = this
    private var chain:ChainTask? = null

    var done:Boolean = false
        get() = currChain == null

    fun update(delta:Float){
        if(!done) {
            currChain!!.func()
            if(currChain!!.predicate()){
                currChain = currChain!!.chain
            }
        }
    }

    fun setChain(chainTask:ChainTask):ChainTask{
        chain = chainTask
        return chain!!
    }
}