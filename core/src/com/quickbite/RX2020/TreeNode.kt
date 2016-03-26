package com.quickbite.rx2020

import java.util.*

/**
 * Created by Paha on 2/4/2016.
 */
class TreeNode<T>(val data:T, val name:String) {
    private val children:LinkedList<TreeNode<T>> = LinkedList()

    var parent:TreeNode<T>? = null
        get() = this.parent

    val isLeaf:Boolean
        get() = this.children.size == 0

    fun add(child:TreeNode<T>){
        children.add(child)
        child.parent = this
    }

    fun getChildByName(name:String):TreeNode<T>?{
        for(child in children)
            if(child.name.equals(name))
                return child

        return null
    }
}