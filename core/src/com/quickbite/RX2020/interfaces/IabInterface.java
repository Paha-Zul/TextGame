package com.quickbite.rx2020.interfaces;

/**
 * Created by Paha on 5/16/2016.
 */
public interface IabInterface {
    String SKU_DONATE_SMALL = "small";
    String SKU_DONATE_MEDIUM = "medium";
    String SKU_DONATE_LARGE = "large";
    String SKU_DONATE_HUGE = "huge";
    String SKU_DONATE_ERROR = "error";

    // (arbitrary) request code for the purchase flow
    int RC_REQUEST = 10001;
    void donate(int amount);
}
