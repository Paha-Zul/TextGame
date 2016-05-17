package com.quickbite.rx2020.interfaces;

/**
 * Created by Paha on 5/16/2016.
 */
public interface IabInterface {
    String SKU_TEST_PURCHASED = "android.test.purchased";
    String SKU_TEST_CANCELED = "android.test.canceled";
    String SKU_TEST_REFUNDED = "android.test.refunded";
    String SKU_TEST_UNAVAILABLE = "android.test.item_unavailable";

    // (arbitrary) request code for the purchase flow
    int RC_REQUEST = 10001;
    void donate();
}
