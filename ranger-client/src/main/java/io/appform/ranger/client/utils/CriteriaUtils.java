/*
 * Copyright 2015 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.appform.ranger.client.utils;

import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class CriteriaUtils {

    /*
        We use this merge only when the explicit arg criteria is passed to the clients.
        The idea is to check if this has to be merged with the initialCriteria or not.

        So the default return is always argCriteria, in case the initial criteria is null!

        So when useInitialCriteria is true, the initial criteria defined will always be used, no matter what,
        it is a predicate and. (Not to be confused with or)
     */
    public static <T>Predicate<T> getCriteria(
            boolean useInitialCriteria,
            Predicate<T> initialCriteria,
            Predicate<T> argCriteria
    ){
        return null != initialCriteria && null != argCriteria && useInitialCriteria ?
           initialCriteria.and(argCriteria) : argCriteria;
    }
}
