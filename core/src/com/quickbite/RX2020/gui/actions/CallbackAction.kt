package com.quickbite.rx2020.gui.actions

import com.badlogic.gdx.scenes.scene2d.Action

/**
 * Created by Paha on 5/4/2016.
 */
class CallbackAction(val callback: () -> Unit) : Action() {
    override fun act(p0: Float): Boolean {
        callback()
        return true
    }
}