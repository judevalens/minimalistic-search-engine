package org.quisqueya.macaya;

import akka.actor.typed.ActorSystem;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.quisqueya.macaya.spider.RootBehavior;
import org.quisqueya.macaya.spider.Spider;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String nodePort = System.getenv("NODE_PORT");
        String hostName  =System.getenv("HOST_NAME");
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

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load());

        System.out.println("hello world");
        ActorSystem<Void> spiderSystem = ActorSystem.create(RootBehavior.create(),"SpiderCluster",config);
        System.out.println("End of spider");
        Cluster cluster = Cluster.get(spiderSystem);

        System.out.printf("my address is: %s\n",cluster.selfMember().address());

    }
}
