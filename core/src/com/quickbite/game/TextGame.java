package com.quickbite.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TextGame extends Game {
    public OrthographicCamera camera;
	public static SpriteBatch batch;
	public static BitmapFont font;
	public static Viewport viewport;
    public static Stage stage;
	
	@Override
	public void create () {
        camera = new OrthographicCamera(800, 480);
		viewport = new StretchViewport(800, 480, camera);
		batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        stage = new Stage(viewport);
		font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        font.setColor(Color.BLACK);

        Gdx.input.setInputProcessor(stage);
        setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		super.render();
	}
}
