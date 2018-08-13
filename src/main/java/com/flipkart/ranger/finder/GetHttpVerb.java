package com.flipkart.ranger.finder;

public class GetHttpVerb extends HttpVerb {
    public GetHttpVerb() {
        super(HttpVerbType.GET);
    }

    public <T> T accept(HttpVerbVisitor<T> httpVerbVisitor) throws Exception{
        return httpVerbVisitor.visit(this);
    }
}
