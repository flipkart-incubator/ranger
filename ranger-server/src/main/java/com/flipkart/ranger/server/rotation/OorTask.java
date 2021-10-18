package com.flipkart.ranger.server.rotation;


import io.dropwizard.servlets.tasks.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Slf4j
public class OorTask extends Task {

    private RotationStatus rotationStatus;

    public OorTask(RotationStatus rotationStatus) {
        super("ranger-oor");
        this.rotationStatus = rotationStatus;
    }

    @Override
    public void execute(Map<String, List<String>> map, PrintWriter printWriter) {
        rotationStatus.oor();
        log.info("Taking node out of rotation on ranger");
    }
}
