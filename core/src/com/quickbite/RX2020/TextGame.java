package com.quickbite.rx2020;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.quickbite.rx2020.managers.EasyAssetManager;
import com.quickbite.rx2020.screens.LoadingScreen;

public class TextGame extends com.badlogic.gdx.Game {
    public static OrthographicCamera camera;
	public static SpriteBatch batch;
	public static Viewport viewport;
    public static Stage stage;
    public static EasyAssetManager manager;
	public static int tick=0;

	public static TextureAtlas smallGuiAtlas;

	public static Boolean testMode = false;

	@Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		camera = new OrthographicCamera(800, 480);
		viewport = new StretchViewport(800, 480, camera);
		batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);

        stage = new Stage(viewport);

		manager = new EasyAssetManager();

		final Thread mainThread = Thread.currentThread();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				StackTraceElement[] list = mainThread.getStackTrace();
				for(StackTraceElement element : list)
					Logger.log("Crash", element.toString(), Logger.LogLevel.Warning);
				com.quickbite.rx2020.Logger.writeLog("log.txt");
			}
		}));

        Gdx.input.setInputProcessor(stage);
		setScreen(new LoadingScreen(this));
	}

	@Override
	public void render () {
		try {
			Gdx.gl.glClearColor(1, 1, 1, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			super.render();
			stage.act();
			tick++;

		}catch(Exception e){
			e.printStackTrace();
			String message = e.getMessage();
			if(message == null) message = "Hmm?";
			com.quickbite.rx2020.Logger.log("Crashing", message, com.quickbite.rx2020.Logger.LogLevel.Error);
			com.quickbite.rx2020.Logger.writeLog("log.txt");
		}
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
