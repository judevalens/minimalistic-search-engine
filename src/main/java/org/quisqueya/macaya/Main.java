package org.quisqueya.macaya;

import akka.actor.typed.ActorSystem;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.quisqueya.macaya.spider.RootBehavior;
import org.quisqueya.macaya.spider.Spider;
import org.quisqueya.macaya.utils.SpiderUri;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("testing");
            SpiderUri testUri = new SpiderUri(new URI("http://example.com/display?lang=en"));
            testUri.spiderNormalize();
        }catch (Exception ignored) {
        }
    }

    public static void startCluster() {
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

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load());

        ActorSystem<Void> spiderSystem = ActorSystem.create(RootBehavior.create(),"SpiderCluster",config);
    }
}
