package com.quickbite.rx2020.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.quickbite.rx2020.TextGame;
import com.quickbite.rx2020.interfaces.IGPGServices;
import com.quickbite.rx2020.util.Logger;
import org.jetbrains.annotations.NotNull;

public class DesktopLauncher implements IGPGServices{
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 800;
		config.height = 480;
		config.resizable = false;

		DesktopLauncher instance = new DesktopLauncher();
		new LwjglApplication(new TextGame(instance), config);
	}

	@Override
	public void openURL(@NotNull String link) {
		Gdx.net.openURI(link);
	}

	@Override
	public void donate() {
		Logger.log("DesktopLauncher", "Yay, we are donating!", Logger.LogLevel.Info);
	}

	@Override
	public String getCurrDeviceID() {
		return "Desktop";
	}

	@Override
	public boolean isTestDevice() {
		return true;
	}
}
