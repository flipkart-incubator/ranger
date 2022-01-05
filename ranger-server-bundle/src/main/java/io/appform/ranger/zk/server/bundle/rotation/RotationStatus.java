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
package io.appform.ranger.zk.server.bundle.rotation;

import java.util.concurrent.atomic.AtomicBoolean;

public class RotationStatus {
    private final AtomicBoolean status;

    public RotationStatus(boolean initialStatus) {
        status = new AtomicBoolean(initialStatus);
    }

    public void oor() {
        status.set(false);
    }

    public void bir() {
        status.set(true);
    }

    public boolean status() {
        return status.get();
    }
}
