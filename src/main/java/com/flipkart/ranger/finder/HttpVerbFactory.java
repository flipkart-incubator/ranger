package com.flipkart.ranger.finder;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;

public class HttpVerbFactory {
    public HttpRequestBase getHttpVerb(HttpVerb httpVerb, URI uri) throws Exception{
        return httpVerb.accept(new HttpVerbVisitor<HttpRequestBase>() {
            @Override
            public HttpRequestBase visit(GetHttpVerb getHttpVerb) throws Exception{
                return new HttpGet(uri);
            }

            @Override
            public HttpRequestBase visit(PostHttpVerb postHttpVerb) throws Exception{
                HttpPost httpPost = new HttpPost(uri);
                httpPost.setEntity(new UrlEncodedFormEntity(postHttpVerb.getBody()));
                return httpPost;
            }
        });
    }
}
