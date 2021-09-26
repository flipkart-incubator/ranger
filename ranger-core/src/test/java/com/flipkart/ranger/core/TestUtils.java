package com.flipkart.ranger.core;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 *
 */
public class TestUtils {
    public static void sleepForSeconds(int numSeconds) {
        await().pollDelay(numSeconds, TimeUnit.SECONDS).until(() -> true);
    }
}
