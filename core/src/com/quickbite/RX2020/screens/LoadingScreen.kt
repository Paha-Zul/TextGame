package com.quickbite.rx2020.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.quickbite.rx2020.ChainTask
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.DataManager
import com.quickbite.rx2020.util.GH

/**
 * Created by Paha on 2/27/2016.
 */

class LoadingScreen(val game: TextGame): Screen {
    lateinit var  chain: ChainTask
    val logo = Texture(Gdx.files.internal("art/load/logoWhiteSmall.png"), true)
    var opacity:Float = 0f
    var counter = 0
    var done = false
    var logoDone = false
    var trigger = false
    var scale = 1f
    var readyToLoad = false


    override fun show() {
        logo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)

        chain = ChainTask({counter<20}, {counter++}, {counter=0})
        chain.setChain(ChainTask(
                {opacity < 1},
                {opacity = GH.lerpValue(opacity, 0f, 1f, 1f)},
                {
                    initDataManager();
                    loadManager()
                })) //Fade in
            .setChain(ChainTask({counter < 60}, {counter++; DataManager.updateLoadData()}, {counter=0})) //Wait
            .setChain(ChainTask({opacity > 0}, {opacity = GH.lerpValue(opacity, 1f, 0f, 0.8f)})) //Fade out
            .setChain(ChainTask({!done && counter < 20}, {counter++}, {
                logoDone = true
            })) //Set the logo as done.
    }

    private fun loadManager(){
        TextGame.manager.loadALlPictures(Gdx.files.internal("art/load/"))
        TextGame.manager.loadAllFonts(Gdx.files.internal("fonts/"))
        TextGame.manager.loadAllImageSheets(Gdx.files.internal("art/sheets/"))

        readyToLoad = true
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

        if(readyToLoad) {
            done = TextGame.manager.update()
            if (done && !trigger) {
                TextGame.smallGuiAtlas = TextGame.manager.get("smallUI", TextureAtlas::class.java)
                trigger = true
            }

            if(done && logoDone) {
                game.screen = MainMenuScreen(game)
            }
        }

        val color = TextGame.batch.color

        TextGame.batch.begin()
        TextGame.batch.setColor(color.r, color.g, color.b, opacity)

        TextGame.batch.draw(logo, -150f*scale, -150f*scale, 300f*scale, 300f*scale)

        TextGame.batch.color = Color.WHITE
        TextGame.batch.end()
    }

    override fun resume() {

    }

    override fun dispose() {

    }

    private fun initDataManager() {
        DataManager.eventDir = Gdx.files.internal("files/events/")
        DataManager.namesDir=Gdx.files.internal("files/text/names.json")
        DataManager.activitiesDir=Gdx.files.internal("files/searchActivities.json")
        DataManager.rewardsDir=Gdx.files.internal("files/rewards.json")
        DataManager.itemsDir=Gdx.files.internal("files/items.json")
        DataManager.endDir=Gdx.files.internal("files/end.json")
        DataManager.traitsDir=Gdx.files.internal("files/traits.toml")
        DataManager.guiDataDir=Gdx.files.internal("files/guiData.toml")
    }
}
