package org.quisqueya.macaya;

import akka.actor.Actor;
import akka.actor.typed.ActorSystem;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.quisqueya.macaya.spider.Frontier;
import org.quisqueya.macaya.spider.RootBehavior;
import org.quisqueya.macaya.spider.Spider;
import org.quisqueya.macaya.utils.SpiderUri;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {

            //JedisPoolManager jedisPool =  JedisPoolManager.getInstance("localhost",6379);
            //ActorSystem<Frontier.Command> frontier = ActorSystem.create(Frontier.create(jedisPool),"spider");
            //frontier.tell(new Frontier.QueueUrl("https://doc.akka.io/docs/akka/current/general/configuration-reference.html"));
            startCluster();
        }catch (Exception ignored) {
            throw ignored;
        }
    }

    public static void startCluster() {
        Config config;
        if (1 == 2) {
            String nodePort = System.getenv("NODE_PORT");
            String hostName  =System.getenv("HOST_NAME");
            String role  = System.getenv("NODE_ROLE");
            int port = 0;

            if (nodePort != null) {
                port = Integer.parseInt(nodePort);
            }

            if (hostName == null) {
                hostName = "localhost";
            }

            Map<String, Object> overrides = new HashMap<>();
            overrides.put("akka.remote.artery.canonical.port", port);
            overrides.put("akka.remote.artery.canonical.hostname", hostName);
            overrides.put("akka.cluster.roles", Collections.singletonList(role));
             config = ConfigFactory.parseMap(overrides)
                    .withFallback(ConfigFactory.load());
        }else {
             config = ConfigFactory.load("application_single.conf");

        }

        ActorSystem<Void> spiderSystem = ActorSystem.create(RootBehavior.create(),"SpiderCluster",config);
    }
}
