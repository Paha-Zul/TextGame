package com.quickbite.rx2020

import java.util.*

/**
 * Created by Paha on 2/10/2016.
 * A fun little chain task class where a predicate and function is supplied. When the predicate is true, the chain task
 * will try to go to the next task. If null, nothing is called within update()
 */
class ChainTask(var predicate:(() -> Boolean)? , var func:(()->Unit)? = null, var finish:(()->Unit)? = null){

    companion object : IUpdateable{
        private val everyFrameTaskList:LinkedList<ChainTask> = LinkedList()
        private val newTaskList:LinkedList<ChainTask> = LinkedList() //Prevents concurrent modifications.

        private val hourlyTaskList:LinkedList<ChainTask> = LinkedList()
        private val newHourlyTaskList:LinkedList<ChainTask> = LinkedList() //Prevents concurrent modifications.

        fun addTaskToEveryFrameList(task:ChainTask) = newTaskList.add(task)
        fun addTaskToHourlyList(task:ChainTask) = newHourlyTaskList.add(task)

        /**
         * Updates from the main TextGame screen. Can be used in any part of the game.
         */
        override fun update(delta:Float){
            newTaskList.forEach { task -> everyFrameTaskList.add(task) }
            newTaskList.clear()

            val iter = everyFrameTaskList.iterator()
            while(iter.hasNext()){
                val task = iter.next()
                if(task.done)
                    iter.remove()
                else
                    task.update()
            }
        }

        /**
         * Only updated during the GameScreen. Used for hourly ticks.
         */
        override fun updateHourly(delta: Float) {
            newHourlyTaskList.forEach { task -> hourlyTaskList.add(task) }
            newHourlyTaskList.clear()

            val iter = hourlyTaskList.iterator()
            while(iter.hasNext()){
                val task = iter.next()
                if(task.done)
                    iter.remove()
                else
                    task.update()
            }
        }
    }

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

    /**
     * Manually sets this chain as done. This is used to forcefully remove the task from any lists or stop execution.
     */
    fun setDone(){
        currChain = null
    }
}