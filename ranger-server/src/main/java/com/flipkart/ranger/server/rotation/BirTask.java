package com.flipkart.ranger.server.rotation;

import io.dropwizard.servlets.tasks.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Slf4j
public class BirTask extends Task {

    private RotationStatus rotationStatus;

    public BirTask(RotationStatus rotationStatus) {
        super("ranger-bir");
        this.rotationStatus = rotationStatus;
    }

    @Override
    public void execute(Map<String, List<String>> map, PrintWriter printWriter) {
        rotationStatus.bir();
        log.info("Taking node back into rotation on ranger");
    }
}
