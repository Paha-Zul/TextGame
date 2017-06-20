package com.quickbite.rx2020.managers

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import com.quickbite.rx2020.ScrollingBackground
import com.quickbite.rx2020.TextGame

/**
 * Created by Paha on 6/20/2017.
 */
object ScrollingBackgroundManager {
    val scrollingBackgroundList:MutableList<ScrollingBackground> = arrayListOf()

    init{
        //The foreground.
        val sc1: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("ForegroundTreeLayer", Texture::class.java)), 3f, -100f, -TextGame.camera.viewportHeight / 2f)
        val sc2: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("ForegroundTreeLayer", Texture::class.java)), 3f, 800f, -TextGame.camera.viewportHeight / 2f)
        sc1.following = sc2
        sc2.following = sc1
        sc1.resetCallback = { sc1.invisible = MathUtils.random(1, 100) < 75 }
        sc2.resetCallback = { sc2.invisible = MathUtils.random(1, 100) < 75 }
        sc1.invisible = true
        sc2.invisible = true

        //The back-mid ground? We actually want this on top of our midground (ground) cause they are trees
        val sc3: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("BackgroundTreeLayer", Texture::class.java)), 2f, -100f, -TextGame.camera.viewportHeight / 2.6f)
        val sc4: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("BackgroundTreeLayer", Texture::class.java)), 2f, 800f, -TextGame.camera.viewportHeight / 2.6f)
        sc3.following = sc4
        sc4.following = sc3
        sc3.resetCallback = { sc3.invisible = MathUtils.random(1, 100) < 75 }
        sc4.resetCallback = { sc4.invisible = MathUtils.random(1, 100) < 75 }
        sc3.invisible = true
        sc4.invisible = true

        //The midground.
        val sc5: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Midground2", Texture::class.java)), 2f, -100f, -TextGame.camera.viewportHeight / 2f)
        val sc6: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Midground2", Texture::class.java)), 2f, 800f, -TextGame.camera.viewportHeight / 2f)
        sc5.following = sc6
        sc6.following = sc5

        //The background.
        val sc7: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Background2", Texture::class.java)), 0.2f, -100f, -TextGame.camera.viewportHeight / 2f)
        val sc8: ScrollingBackground = ScrollingBackground(Sprite(TextGame.manager.get("Background2", Texture::class.java)), 0.2f, 800f, -TextGame.camera.viewportHeight / 2f)
        sc7.following = sc8
        sc8.following = sc7

        //Add these in reverse for drawing order.
        scrollingBackgroundList.add(sc8)
        scrollingBackgroundList.add(sc7)
        scrollingBackgroundList.add(sc6)
        scrollingBackgroundList.add(sc5)
        scrollingBackgroundList.add(sc4)
        scrollingBackgroundList.add(sc3)
        scrollingBackgroundList.add(sc2)
        scrollingBackgroundList.add(sc1)
    }
}