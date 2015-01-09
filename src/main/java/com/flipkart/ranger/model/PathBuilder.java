package com.flipkart.ranger.model;

import com.flipkart.ranger.finder.Service;

public class PathBuilder {
    public static String path(final Service service) {
        return String.format("/%s", service.getServiceName());
    }
}
