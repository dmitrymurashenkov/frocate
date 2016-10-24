package com.frocate.shorturl.mock;

public interface Node
{
    String getHost();
    int getPort();
    String shorten(String url);
    String expand(String shortUrl);
}
