package com.frocate.shorturl.test;

public interface ShortUrlService
{
    String shorten(String url);
    String expand(String shortUrl);
    boolean nodeReady();
}
