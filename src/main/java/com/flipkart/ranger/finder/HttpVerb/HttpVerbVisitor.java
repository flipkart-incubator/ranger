package com.flipkart.ranger.finder.HttpVerb;

public interface HttpVerbVisitor<T> {
    T visit(GetHttpVerb getHttpVerb) throws Exception;
    T visit(PostHttpVerb postHttpVerb) throws Exception;
}
