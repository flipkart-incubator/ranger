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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 *
 */
@UtilityClass
public class TestUtils {

    static final int HUB_ACTIVE_DURATION_SECONDS = 3;
    static final int FINDER_ACTIVE_DURATION_SECONDS = 3;

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

    public static <T, R extends ServiceRegistry<T>> void sleepUntilFinderIsActive(ServiceFinder<T, R> finder){
        await().pollDelay(FINDER_ACTIVE_DURATION_SECONDS, TimeUnit.SECONDS).untilAsserted(finder::start);
    }

    public static <T, R extends ServiceRegistry<T>> void sleepUntilHubIsActive(ServiceFinderHub<T, R> hub){
        await().pollDelay(HUB_ACTIVE_DURATION_SECONDS, TimeUnit.SECONDS).untilAsserted(() -> hub.getServiceDataSource().start());
        await().pollDelay(HUB_ACTIVE_DURATION_SECONDS, TimeUnit.SECONDS).untilAsserted(() -> hub.getFinders().get().values().forEach(ServiceFinder::start));
    }
}
