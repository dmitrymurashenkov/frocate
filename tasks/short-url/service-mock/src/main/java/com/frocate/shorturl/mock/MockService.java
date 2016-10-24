package com.frocate.shorturl.mock;

public interface MockService
{
    String shorten(String url);
    String expand(String shortUrl);
}
