package com.quickbite.rx2020;

/**
 * Created by Paha on 1/24/2016.
 */
public class GH {

    /**
     * Lerps a float value from start to target.
     * @param curr The current value of the lerp.
     * @param start The start value of the lerp.
     * @param target The target value of the lerp.
     * @param seconds The time in seconds for the lerp to happen.
     * @return The value of the lerp after the calculated tick amount.
     */
    public static float lerpValue(float curr, float start, float target, float seconds){
        float amt = (Math.abs(start - target)/seconds)/60f;
        if(start < target) {
            curr += amt;
            if(curr >= target) curr = target;
        }else {
            curr -= amt;
            if(curr <= target) curr = target;
        }

        return curr;
    }
}
