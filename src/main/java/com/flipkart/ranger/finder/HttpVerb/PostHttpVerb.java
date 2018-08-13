package com.flipkart.ranger.finder.HttpVerb;

import java.util.List;

public class PostHttpVerb extends HttpVerb {
    List body;
    public PostHttpVerb(List body) {
        super(HttpVerbType.POST);
        this.body = body;
    }

    public List getBody() {
        return body;
    }

    public <T> T accept(HttpVerbVisitor<T> httpVerbVisitor) throws Exception{
        return httpVerbVisitor.visit(this);
    }
}
