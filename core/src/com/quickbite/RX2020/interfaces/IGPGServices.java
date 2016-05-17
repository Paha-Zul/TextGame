package com.quickbite.rx2020.interfaces;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Paha on 5/16/2016.
 */
public interface IGPGServices extends IUrlOpener, IabInterface{
    ArrayList<String> testDevices = new ArrayList<String>(
            Arrays.asList("93c5883d462d97e9", "b32d1a323299672f"));

    String getCurrDeviceID();
    boolean isTestDevice();
}