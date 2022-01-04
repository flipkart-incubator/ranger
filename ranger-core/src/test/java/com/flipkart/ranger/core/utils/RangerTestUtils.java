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
import com.flipkart.ranger.core.model.Service;
import com.flipkart.ranger.core.model.ServiceRegistry;
import com.flipkart.ranger.core.units.TestNodeData;
import lombok.experimental.UtilityClass;
import org.awaitility.core.ThrowingRunnable;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.awaitility.Awaitility.await;

/**
 *
 */
@UtilityClass
public class RangerTestUtils {

    static String TEST_NAMESPACE = "test";
    static String TEST_SERVICE = "test-service";

    public static Predicate<TestNodeData> getCriteria(final int shardId){
        return nodeData -> nodeData.getShardId() == shardId;
    }

    public static Service getService(){
        return getService(TEST_NAMESPACE, TEST_SERVICE);
    }

    public static Service getService(String namespace, String serviceName){
        return Service.builder()
                .serviceName(serviceName)
                .namespace(namespace)
                .build();
    }

    /*
        Node list is notEmpty only after the first updateRegistry has happened. So this should suffice for finder.start.
        However, we have to use the sleepUntil with () -> true for when we need periodic wait (deterministic)
     */
    public static <T, R extends ServiceRegistry<T>> boolean validate(ServiceFinder<T, R> finder){
        return finder.getServiceRegistry() != null &&
                finder.getServiceRegistry().nodeList() != null &&
                !finder.getServiceRegistry().nodeList().isEmpty();
    }

    /*
        If we know the upper bound condition, please use the until with the upper bound.
        Only for cases, where you have to wait till the refreshInterval periods, don't want to introduce
        refreshed and other boolean flags throughout the code.
     */
    public static void sleepUntil(int numSeconds) {
        await().pollDelay(numSeconds, TimeUnit.SECONDS).until(() -> true);
    }

    /*
        Use this when you have to alter the numSeconds in any of the specific assertions. For finder and hub, the values are appropriately coded
        keeping the start intervals in mind.
     */
    public static void sleepUntil(int numSeconds, Callable<Boolean> conditionEvaluator) {
        await().pollDelay(numSeconds, TimeUnit.SECONDS).until(conditionEvaluator);
    }

    /*
        Only applicable for initial node population using finder. Works when you intend to start the finder with nodes in 'em.
     */
    public static <T, R extends ServiceRegistry<T>> void sleepUntilFinderStarts(ServiceFinder<T, R> finder){
        sleepUntil(3, () -> validate(finder));
    }

    /*
        Only applicable for initial node population using hub of finders. Works when you intend to start the finder hub with nodes in 'em.
     */
    public static <T, R extends ServiceRegistry<T>> void sleepUntilHubStarts(ServiceFinderHub<T, R> hub){
        sleepUntil(3, () -> hub.getFinders().get().values().stream().allMatch(RangerTestUtils::validate));
    }
}
