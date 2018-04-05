package com.quickbite.rx2020.gui

import com.badlogic.gdx.scenes.scene2d.ui.Label

class CustomLabel(text: CharSequence?, style: LabelStyle?): Label(text, style) {
    override fun toString(): String {
        return this.text.toString()
    }
}