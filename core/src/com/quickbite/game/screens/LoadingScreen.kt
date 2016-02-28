package com.quickbite.game.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.quickbite.game.ChainTask
import com.quickbite.game.GH
import com.quickbite.game.Game

/**
 * Created by Paha on 2/27/2016.
 */

class LoadingScreen(val game: Game): Screen {
    lateinit var  chain: ChainTask
    val logo = Texture(Gdx.files.internal("art/Logo.png"), true)
    var opacity:Float = 0f
    var counter = 0
    var done = false

    override fun show() {
        logo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        chain = ChainTask({opacity >= 1}, {opacity = GH.lerpValue(opacity, 0f, 1f, 0.5f)})
        chain.setChain(ChainTask({counter >= 30}, {counter++})).
                setChain(ChainTask({opacity <= 0}, {opacity = GH.lerpValue(opacity, 1f, 0f, 0.5f)}))
        .setChain(ChainTask({done}, {}, {game.screen = MainMenuScreen(game)}))
    }

    override fun hide() {
    }

    override fun resize(p0: Int, p1: Int) {
    }

    override fun pause() {
    }

    override fun render(delta: Float) {
        chain.update()
        done = Game.manager.update()
        val color = Game.batch.color

        Game.batch.begin()
        Game.batch.setColor(color.r, color.g, color.b, opacity)

        Game.batch.draw(logo, -150f, -150f, 300f, 300f)

        Game.batch.color = Color.WHITE
        Game.batch.end()
    }

    override fun resume() {
    }

    override fun dispose() {
    }
}
