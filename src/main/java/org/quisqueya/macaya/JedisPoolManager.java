package org.quisqueya.macaya;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.management.InstanceNotFoundException;
import java.io.*;
import java.net.InetAddress;
import java.util.Scanner;

public class JedisPoolManager {
    private static JedisPoolManager instance;
    private final JedisPool pool;
    private boolean loadedScript = false;

    private JedisPoolManager(String host, int port) {
        pool = new JedisPool(host, port);
    }

    public static JedisPoolManager getInstance(String host, int port) {
        if (instance == null) {
            instance = new JedisPoolManager(host, port);
        }

        return instance;
    }

    public static JedisPoolManager getInstance() {
        return instance;
    }

    public Jedis getJedis() {
        var jedis = pool.getResource();
        if (!loadedScript) {
            loadedScript = true;
            jedis.functionFlush();
            try {
                var scriptPath = getClass().getClassLoader().getResource("lua/url_frontier.lua");
                if (scriptPath == null) {
                    throw new FileNotFoundException("failed to find the script");
                }
                loadScript(jedis, scriptPath.getPath());
            } catch (IOException e) {
                return null;
            }
        }
        return jedis;
    }

    private boolean loadScript(Jedis jedis, String scriptPath) throws IOException {
        var scriptFile = new File(scriptPath);
        var scriptStream = new FileInputStream(scriptFile);
        jedis.functionLoad(scriptStream.readAllBytes());
        return true;
    }
}
