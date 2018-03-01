package com.quickbite.rx2020.objects

import com.quickbite.rx2020.managers.DataManager

data class Trait(val traitDef:DataManager.TraitJson, val startTime:Float, val duration:Float) {
}