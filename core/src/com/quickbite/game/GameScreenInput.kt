package com.quickbite.game

import com.badlogic.gdx.InputProcessor
import java.util.*

/**
 * Created by Paha on 2/6/2016.
 */
class GameScreenInput : InputProcessor{
    val keyEventMap:HashMap<Int, () -> Unit> = hashMapOf()

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        val func = keyEventMap[keycode]
        if(func != null) func()
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }
}