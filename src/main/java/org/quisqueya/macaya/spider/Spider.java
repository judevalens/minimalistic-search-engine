package org.quisqueya.macaya.spider;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;
import akka.cluster.typed.Cluster;
import okhttp3.OkHttpClient;
import org.quisqueya.macaya.JedisPoolManager;
import org.slf4j.Logger;

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

    private Spider(ActorContext<Command> context, Logger logger) {
        super(context);
        this.logger = logger;
        Cluster cluster = Cluster.get(context.getSystem());
        System.out.printf("Master node is up: %s! address: %s\n", cluster.state().isMemberUp(cluster.selfMember().address()), cluster.selfMember().address().toString());
        var nodeIsUp = cluster.state().isMemberUp(cluster.selfMember().address());
        //TODO should have to listen to the cluster listener
        if (nodeIsUp) {
            getContext().getSelf().tell(new SpiderConf(3, 1, 1));
        }

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
        PoolRouter<Fetcher.Command> fetcherPoolRouter = Routers.pool(conf.nFetcher, Behaviors.supervise(Fetcher.create(logger,httpClient,jedisPool.getJedis())).onFailure(SupervisorStrategy.restart())).withRoundRobinRouting();
        ActorRef<Fetcher.Command> fetcherRouter = getContext().spawn(fetcherPoolRouter, "fetcher-pool");
        for (int i = 0; i < conf.nFetcher; i++) {
            fetcherRouter.tell(new Fetcher.Conf(hostNamesKey, coolDown));
        }

        urlFrontier.tell(new Frontier.QueueUrl("https://doc.akka.io/docs/akka/current/scheduler.html"));
        return this;
    }

}
