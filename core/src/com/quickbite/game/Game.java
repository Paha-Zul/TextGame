package com.quickbite.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quickbite.game.managers.EasyAssetManager;
import com.quickbite.game.screens.LoadingScreen;

public class Game extends com.badlogic.gdx.Game {
    public static OrthographicCamera camera;
	public static SpriteBatch batch;
	public static BitmapFont font, spaceFont;
	public static Viewport viewport;
    public static Stage stage;
    public static EasyAssetManager manager;
	public static int tick=0;

	@Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		camera = new OrthographicCamera(800, 480);
		viewport = new StretchViewport(800, 480, camera);
		batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);

        stage = new Stage(viewport);
		font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
		font.setColor(Color.BLACK);
		spaceFont = new BitmapFont(Gdx.files.internal("fonts/spaceFont.fnt"));
		spaceFont.setColor(Color.BLACK);
		spaceFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		manager = new EasyAssetManager();
		manager.loadALlPictures(Gdx.files.internal("art/"));
		manager.loadAllFonts(Gdx.files.internal("fonts/"));

        Gdx.input.setInputProcessor(stage);
		setScreen(new LoadingScreen(this));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		super.render();
		stage.act();
		tick++;
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}
}
