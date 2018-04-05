package com.quickbite.rx2020.gui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.quickbite.rx2020.TextGame
import com.quickbite.rx2020.managers.GameStats
import com.quickbite.rx2020.screens.MainMenuScreen
import com.quickbite.rx2020.util.SaveLoad

object SettingsGUI {
    private val settingsTable = Table()

    fun buildSettingsTable():Table{
        GameStats.game.pauseGame()
        settingsTable.clear()

        settingsTable.setSize(200f, 200f)
        settingsTable.setOrigin(Align.center)
        settingsTable.setPosition(TextGame.viewport.worldWidth/2f - 100f, TextGame.viewport.worldHeight/2f - 100f)
        settingsTable.background = TextureRegionDrawable(TextureRegion(TextGame.smallGuiAtlas.findRegion("pixelBlack")))

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = TextGame.manager.get("spaceFont2", BitmapFont::class.java)
        buttonStyle.fontColor = Color.WHITE

        val saveAndQuitButton = TextButton("Save and Quit", buttonStyle)
        saveAndQuitButton.label.setFontScale(0.15f)

        val saveAndExitButton = TextButton("Save and Exit", buttonStyle)
        saveAndExitButton.label.setFontScale(0.15f)

        val returnButton = TextButton("Return", buttonStyle)
        returnButton.label.setFontScale(0.15f)

        settingsTable.add(saveAndQuitButton).height(60f)
        settingsTable.row()
        settingsTable.add(saveAndExitButton).height(60f)
        settingsTable.row()
        settingsTable.add(returnButton).height(60f)

        saveAndQuitButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                SaveLoad.saveGame(false, GameStats.game)
                GameScreenGUIManager.clear()
                GameStats.game.game.screen = MainMenuScreen(GameStats.game.game)
            }
        })

        saveAndExitButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                SaveLoad.saveGame(false, GameStats.game)
                GameScreenGUIManager.clear()
                GameStats.game.game.screen = MainMenuScreen(GameStats.game.game)
                Gdx.app.exit()
            }
        })

        returnButton.addListener(object: ChangeListener(){
            override fun changed(p0: ChangeEvent?, p1: Actor?) {
                GameScreenGUIManager.closeSettings()
            }
        })

        return settingsTable
    }

    fun close() = settingsTable.remove()
}