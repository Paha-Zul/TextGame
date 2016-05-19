package com.quickbite.rx2020.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.quickbite.rx2020.TextGame;
import com.quickbite.rx2020.interfaces.IPlatformSpecific;
import com.quickbite.rx2020.util.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class DesktopLauncher implements IPlatformSpecific {
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
	public void donate(int amount) {
		Logger.log("DesktopLauncher", "Yay, we are donating "+amount+" dollars!", Logger.LogLevel.Info);
	}

	@Override
	public String getCurrDeviceID() {
		return "Desktop";
	}

	@Override
	public boolean isTestDevice() {
		return true;
	}

	@Override
	public void displayText(final String text, String duration) {

	}

    @Override
    public synchronized void outputToLog(String fileName, String[] text) {
        FileHandle handle = Gdx.files.internal(fileName);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(handle.file()));
            for(String t : text){
                writer.write(t);
                writer.newLine();
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
