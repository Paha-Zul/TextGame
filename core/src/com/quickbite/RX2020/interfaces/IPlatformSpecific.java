package com.quickbite.rx2020.interfaces;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Paha on 5/18/2016.
 */
public interface IPlatformSpecific extends IGPGServices, IabInterface, IUrlOpener {
    ArrayList<String> testDevices = new ArrayList<String>(
            Arrays.asList("93c5883d462d97e9", "b32d1a323299672f", "a126303594649583"));

    void displayText(final String text, String duration);
    String getCurrDeviceID();
    boolean isTestDevice();
    void outputToLog(String fileName, String[] text);
}
