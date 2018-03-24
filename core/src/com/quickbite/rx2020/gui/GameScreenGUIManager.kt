package com.quickbite.rx2020.gui

object GameScreenGUIManager {

    fun init(){
        val groupTable = GroupGUI.init()
        val partsTable = ROVPartsGUI.init()
        val supplyTable = SupplyGUI.init()

    }
}