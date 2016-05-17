package com.quickbite.rx2020.interfaces;

/**
 * Created by Paha on 5/16/2016.
 */
public interface IabInterface {
    String SKU__DONATE_SMALL = "small";
    String SKU__DONATE_MEDIUM = "medium";
    String SKU__DONATE_LARGE = "large";
    String SKU__DONATE_HUGE = "huge";

    // (arbitrary) request code for the purchase flow
    int RC_REQUEST = 10001;
    void donate(String type);
}
