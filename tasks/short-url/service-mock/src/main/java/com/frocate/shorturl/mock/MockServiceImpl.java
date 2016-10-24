package com.frocate.shorturl.mock;

import org.eclipse.jetty.client.HttpClient;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerString;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.VolumeFactory;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static javafx.scene.input.KeyCode.K;
import static javafx.scene.input.KeyCode.V;

public class MockServiceImpl implements MockService
{
    private final HttpClient http;
    private final List<Node> nodes = Collections.synchronizedList(new ArrayList<>());
    private final Config config;
    private final AtomicLong counter = new AtomicLong();
//    private final ConcurrentMap<String, String> fullToShort;
//    private final ConcurrentMap<String, String> shortToFull;
    private final Storage fullToShort = new Storage("fullToShort", 50);
    private final Storage shortToFull = new Storage("shortToFull", 50);

    public MockServiceImpl(Config config) throws Exception
    {
        http = new HttpClient();
        http.setRequestBufferSize(20*1024);
        http.setMaxConnectionsPerDestination(1000);
        http.start();
        this.config = config;
        this.nodes.addAll(parseNodes());
    }

    static class Storage
    {
        private final ConcurrentMap<String, String>[] maps;

        public Storage(String name, int concurrencyLevel)
        {
            maps = new ConcurrentMap[concurrencyLevel];
            for (int i = 0; i < maps.length; i++)
            {
                String dbFile = name + "-" + i + ".db";
                DB db = DBMaker.fileDB(dbFile).fileMmapEnableIfSupported().make();
                maps[i] = db.hashMap(name + "-" + i, Serializer.STRING, Serializer.STRING).create();
            }
        }

        public String get(String key)
        {
            return getMapForKey(key).get(key);
        }

        public String put(String key, String value)
        {
            return getMapForKey(key).put(key, value);
        }

        public String computeIfAbsent(String key, Function<String, String> mappingFunction)
        {
            return getMapForKey(key).computeIfAbsent(key, mappingFunction);
        }

        public String putIfAbsent(String key, String value)
        {
            return getMapForKey(key).putIfAbsent(key, value);
        }

        private ConcurrentMap<String, String> getMapForKey(String key)
        {
            return maps[Math.abs(key.hashCode()) % maps.length];
        }
    }

    @Override
    public String shorten(String url)
    {
        return getNodeForLongUrl(url).shorten(url);
    }

    @Override
    public String expand(String shortUrl)
    {
        return getNodeForShortUrl(shortUrl).expand(shortUrl);
    }

    private String doShorten(String url)
    {
        String shortUrl = fullToShort.computeIfAbsent(url, (key) -> config.getThisNodeIndex() + "" + counter.incrementAndGet());
//        String shortUrl = fullToShort.get(url);
//        if (shortUrl == null)
//        {
//            fullToShort.putIfAbsent(url, config.getThisNodeIndex() + "" + counter.incrementAndGet());
//            shortUrl = fullToShort.get(url);
//        }
        shortToFull.put(shortUrl, url);
        return shortUrl;
    }

    private String doExpand(String shortUrl)
    {
        String originalUrl = shortToFull.get(shortUrl);
        if (originalUrl == null)
        {
            throw new RuntimeException("Unknown short url: " + shortUrl);
        }
        return originalUrl;
    }

    private Node getNodeForLongUrl(String longUrl)
    {
        int nodeIndex = Math.abs(longUrl.hashCode()) % getNodes().size();
        return getNodes().get(nodeIndex);
    }

    private Node getNodeForShortUrl(String shortUrl)
    {
        int nodeIndex = Integer.parseInt(shortUrl.substring(0, 1));
        return getNodes().get(nodeIndex);
    }

    private List<Node> getNodes()
    {
        return nodes;
    }

    private List<Node> parseNodes()
    {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < config.getNodesCount(); i++)
        {
            if (i == config.getThisNodeIndex())
            {
                nodes.add(new LocalNodeImpl(config.getThisNodeIp(), config.getThisNodePort())
                {
                    @Override
                    public String shorten(String url)
                    {
                        return doShorten(url);
                    }

                    @Override
                    public String expand(String shortUrl)
                    {
                        return doExpand(shortUrl);
                    }
                });
            }
            else
            {
                nodes.add(new HttpNodeImpl(config.getNodeIp(i), config.getNodePort(i), http));
            }
        }
        return nodes;
    }
}
