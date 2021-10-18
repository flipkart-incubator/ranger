package com.flipkart.ranger.server.rotation;

import java.util.concurrent.atomic.AtomicBoolean;

public class RotationStatus {
    private AtomicBoolean rotationStatus;

    public RotationStatus(boolean initialStatus) {
        rotationStatus = new AtomicBoolean(initialStatus);
    }

    public void oor() {
        rotationStatus.set(false);
    }

    public void bir() {
        rotationStatus.set(true);
    }

    public boolean status() {
        return rotationStatus.get();
    }
}
