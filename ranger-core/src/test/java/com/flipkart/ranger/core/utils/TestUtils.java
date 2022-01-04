/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.ranger.core.utils;

import com.flipkart.ranger.core.finder.ServiceFinder;
import com.flipkart.ranger.core.finderhub.ServiceFinderHub;
import com.flipkart.ranger.core.model.ServiceRegistry;
import lombok.experimental.UtilityClass;
import org.awaitility.core.ThrowingRunnable;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 *
 */
@UtilityClass
public class TestUtils {

    /*
        If we know the upper bound condition, please use the until with the upper bound.
        Only for cases, where you have to wait till the refreshInterval periods, don't want to introduce
        refreshed and other boolean flags throughout the code.
     */
    public static void sleepUntil(int numSeconds) {
        await().pollDelay(numSeconds, TimeUnit.SECONDS).until(() -> true);
    }

    public static void sleepUntil(int numSeconds, Callable<Boolean> conditionEvaluator) {
        await().pollDelay(numSeconds, TimeUnit.SECONDS).until(conditionEvaluator);
    }

    /*
        Use this when you have to alter the numSeconds in any of the specific assertions. For finder and hub, the values are appropriately coded
        keeping the start intervals in mind.
     */
    public static void sleepUntil(int numSeconds, ThrowingRunnable assertion){
        await().pollDelay(Duration.ofSeconds(numSeconds)).untilAsserted(assertion);
    }

    public static <T, R extends ServiceRegistry<T>> void sleepUntilFinderIsActive(ServiceFinder<T, R> finder){
        sleepUntil(3, () -> finder.getStartSignal().start());
    }

    public static <T, R extends ServiceRegistry<T>> void sleepUntilHubIsActive(ServiceFinderHub<T, R> hub){
        sleepUntil(3, () -> hub.getServiceDataSource().start());
        sleepUntil(3, () -> hub.getFinders().get().values().forEach(finder -> finder.getStartSignal().start()));
    }
}
