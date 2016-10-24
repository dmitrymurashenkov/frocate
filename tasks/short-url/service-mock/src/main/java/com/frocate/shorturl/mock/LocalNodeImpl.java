package com.frocate.shorturl.mock;

public abstract class LocalNodeImpl implements Node
{
    private final String host;
    private final int port;

    public LocalNodeImpl(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    @Override
    public int getPort()
    {
        return 0;
    }

    @Override
    public String getHost()
    {
        return null;
    }
}
