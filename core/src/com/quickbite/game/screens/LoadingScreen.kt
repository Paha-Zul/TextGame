package com.quickbite.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.quickbite.game.ChainTask
import com.quickbite.game.GH
import com.quickbite.game.Game
import com.quickbite.game.managers.DataManager

/**
 * Created by Paha on 2/27/2016.
 */

class LoadingScreen(val game: Game): Screen {
    lateinit var  chain: ChainTask
    val logo = Texture(Gdx.files.internal("art/Logo.png"), true)
    var opacity:Float = 0f
    var counter = 0
    var done = false
    var scale = 1f

    override fun show() {
        logo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)

        Game.manager.loadALlPictures(Gdx.files.internal("art/"))
        Game.manager.loadAllFonts(Gdx.files.internal("fonts/"))

        chain = ChainTask({counter>=20}, {counter++}, {counter=0})
        chain.setChain(ChainTask({opacity >= 1}, {opacity = GH.lerpValue(opacity, 0f, 1f, 1f)}, {loadDataManager()})). //Fade in
                setChain(ChainTask({counter >= 60}, {counter++}, {counter=0})). //Wait
                setChain(ChainTask({opacity <= 0}, {opacity = GH.lerpValue(opacity, 1f, 0f, 0.8f)})). //Fade out
                setChain(ChainTask({done && counter >= 20}, {counter++}, {game.screen = MainMenuScreen(game)})) //Wait
    }

    override fun hide() {
    }

    override fun resize(p0: Int, p1: Int) {
    }

    override fun pause() {
    }

    override fun render(delta: Float) {
        scale+=0.0005f

        chain.update()
        done = Game.manager.update()
        val color = Game.batch.color

        Game.batch.begin()
        Game.batch.setColor(color.r, color.g, color.b, opacity)

        Game.batch.draw(logo, -150f*scale, -150f*scale, 300f*scale, 300f*scale)

        Game.batch.color = Color.WHITE
        Game.batch.end()
    }

    override fun resume() {

    }

    override fun dispose() {

    }

    private fun loadDataManager() {
        DataManager.loadEvents(Gdx.files.internal("files/events/"))
        DataManager.loadRandomNames(Gdx.files.internal("files/text/firstNames.txt"), Gdx.files.internal("files/text/lastNames.txt"))
        DataManager.loadSearchActivities(Gdx.files.internal("files/searchActivities.json"))
        DataManager.loadItems(Gdx.files.internal("files/items.json"))

    }
}
