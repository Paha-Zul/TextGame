package com.quickbite.rx2020.interfaces;

/**
 * Created by Paha on 5/16/2016.
 */
public interface IabInterface {
    String SKU_DONATE_SMALL = "small_donation";
    String SKU_DONATE_MEDIUM = "medium_donation";
    String SKU_DONATE_LARGE = "large_donation";
    String SKU_DONATE_HUGE = "huge_donation";
    String SKU_DONATE_ERROR = "error_donation";

    // (arbitrary) request code for the purchase flow
    int RC_REQUEST = 10001;
    void donate(int amount);
}
