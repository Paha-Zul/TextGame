package com.quickbite.rx2020.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.quickbite.rx2020.TextGame;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(480, 320);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new TextGame(null);
        }

        @Override
        public ApplicationListener createApplicationListener() {
                //TODO Probably needs to not be null.
                return null;
        }
}