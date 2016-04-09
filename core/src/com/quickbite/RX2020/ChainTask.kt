package com.quickbite.rx2020

/**
 * Created by Paha on 2/10/2016.
 * A fun little chain task class where a predicate and function is supplied. When the predicate is true, the chain task
 * will try to go to the next task. If null, nothing is called within update()
 */
class ChainTask(var predicate:(() -> Boolean)? , var func:(()->Unit)? = null, var finish:(()->Unit)? = null) {
    private var currChain:ChainTask? = this
    var chain:ChainTask? = null
        get
        private set

    var done:Boolean = false
        get() = currChain == null

    fun update(){
        if(!done) {

            //Check the predicate to make sure we can call the function
            if(currChain!!.predicate != null) {

                //If the predicate passes, try and call the function.
                if(currChain!!.predicate!!()){
                    if (currChain!!.func == null) { //If the function is null, get the next chain.
                        currChain = currChain!!.chain
                    } else
                        (currChain!!.func!!)() //Call the function

                //If the predicate didn't pass, get the next chain.
                }else{
                    currChain?.finish?.invoke()
                    if (currChain!!.chain != null)
                        currChain = currChain!!.chain
                }

            //If the predicate is null, Try to fire the function once and move on!
            }else{
                currChain!!.func?.invoke()
                currChain = currChain!!.chain
            }
        }
    }

    fun setChain(chainTask:ChainTask):ChainTask{
        chain = chainTask
        return chain!!
    }
}