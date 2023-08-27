package org.quisqueya.macaya.spider;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;
import akka.cluster.typed.Cluster;
import okhttp3.OkHttpClient;
import org.quisqueya.macaya.JedisPoolManager;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Spider extends AbstractBehavior<Spider.Command> {
    public interface Command {
    }

    public static class SpiderConf implements Command {
        final int nFetcher;
        final int nUrlFrontier;
        final int nParser;

        public SpiderConf(int nFetcher, int nUrlFrontier, int nParser) {
            this.nFetcher = nFetcher;
            this.nUrlFrontier = nUrlFrontier;
            this.nParser = nParser;
        }
    }

    private static final String FILE_TEST_URL = "/home/jude/Downloads/norvig.com_big.txt";
    final JedisPoolManager jedisPool = JedisPoolManager.getInstance("localhost", 6379);
    final OkHttpClient httpClient = new OkHttpClient();
    private SpiderConf conf;
    private final Logger logger;
    private boolean isInitialized = false;
    private final long coolDown = TimeUnit.SECONDS.toMillis(1);

    private final HashMap<String, String> keys;
    String crawlSession = "spider-crawler";

    private Spider(ActorContext<Command> context, Logger logger) {
        super(context);
        this.logger = logger;
        Cluster cluster = Cluster.get(context.getSystem());
        System.out.printf("Master node is up: %s! address: %s\n", cluster.state().isMemberUp(cluster.selfMember().address()), cluster.selfMember().address().toString());
        var nodeIsUp = cluster.state().isMemberUp(cluster.selfMember().address());
        //TODO should have to listen to the cluster listener
        if (nodeIsUp) {
            getContext().getSelf().tell(new SpiderConf(1, 1, 1));
        }
        keys = genKeys();

    }

    public static Behavior<Command> create(Logger logger) {
        return Behaviors.setup((context) -> new Spider(context, logger));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(SpiderConf.class, this::onConf).build();
    }

    public Behavior<Command> onConf(SpiderConf conf) {
        if (isInitialized) {
            logger.info("Spider: {} is already initialized", getContext().getSelf().path());
            return this;
        }
        this.conf = conf;
        ActorRef<Frontier.Command> urlFrontier = getContext().spawn(Frontier.create(jedisPool), "url_frontier");
        PoolRouter<Fetcher.Command> fetcherPoolRouter = Routers
                .pool(conf.nFetcher, Behaviors.supervise(Fetcher.create(logger, httpClient, jedisPool.getJedis())).onFailure(SupervisorStrategy.restart()))
                .withRoundRobinRouting();
        ActorRef<Fetcher.Command> fetcherRouter = getContext().spawn(fetcherPoolRouter, "fetcher-pool");
        for (int i = 0; i < conf.nFetcher; i++) {
            fetcherRouter.tell(new Fetcher.Conf(keys, coolDown));
        }
        urlFrontier.tell(new Frontier.QueueUrl("https://redis.com/try-free/?utm_source=redisinsight&utm_medium=app&utm_campaign=redisinsight_triggers_and_functions"));
        return this;
    }

    private HashMap<String, String> genKeys() {
        var keys = new HashMap<String, String>();

        keys.put(RedisKeys.HostNamesZset, crawlSession + "_hostname_zset");
        keys.put(RedisKeys.VisitedUrlSet, crawlSession + "_hostname_url_list");

        return keys;
    }
}
