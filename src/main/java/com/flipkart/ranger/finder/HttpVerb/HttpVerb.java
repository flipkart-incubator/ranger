package com.flipkart.ranger.finder.HttpVerb;

public abstract class HttpVerb {
    public enum HttpVerbType {
        GET,
        POST
    }

    private HttpVerbType httpVerbType;

    public HttpVerb(HttpVerbType httpVerbType) {
        this.httpVerbType = httpVerbType;
    }

    public HttpVerbType getHttpVerbType() {
        return httpVerbType;
    }

    public abstract <T> T accept(HttpVerbVisitor<T> httpVerbVisitor) throws Exception;
}
