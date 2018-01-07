package com.quickbite.rx2020;

import com.quickbite.rx2020.interfaces.IPlatformSpecific;
import org.jetbrains.annotations.NotNull;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

public class IOSLauncher extends IOSApplication.Delegate implements IPlatformSpecific {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        return new IOSApplication(new TextGame(this), config);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }

    @Override
    public void displayText(String text, String duration) {

    }

    @Override
    public String getCurrDeviceID() {
        return null;
    }

    @Override
    public boolean isTestDevice() {
        return false;
    }

    @Override
    public void outputToLog(String fileName, String[] text) {

    }

    @Override
    public void openURL(@NotNull String link) {

    }

    @Override
    public void donate(int amount) {

    }
}