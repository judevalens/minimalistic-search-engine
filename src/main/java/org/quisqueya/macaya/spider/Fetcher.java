package org.quisqueya.macaya.spider;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.msgpack.jackson.dataformat.msgpack.MessagePackFactory;
import org.quisqueya.macaya.spider.pojos.QueueJob;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Fetcher extends AbstractBehavior<Fetcher.Command> {
    public interface Command {
    }

    final Logger logger;
    final OkHttpClient httpClient;

    final Jedis jedis;
    public final int id;

    private Conf conf;

    private boolean isInitialized = false;

    public static class Fetch implements Command {
    }

    public static class Conf implements Command {
        final String hostNamesKey;
        final int coolDown;

        public Conf(String hostNamesKey, int coolDown) {
            this.hostNamesKey = hostNamesKey;
            this.coolDown = coolDown;
        }
    }

    private Fetcher(ActorContext<Command> context, Logger logger, OkHttpClient httpClient, Jedis jedis) {
        super(context);
        this.logger = logger;
        this.httpClient = httpClient;
        this.jedis = jedis;
        var random = new Random();
        id = random.nextInt(10000000);
        context.getSelf().tell(new Fetch());
    }

    public static Behavior<Fetcher.Command> create(Logger logger, OkHttpClient httpClient, Jedis jedis) {
        return Behaviors.setup((context) -> new Fetcher(context, logger, httpClient, jedis));
    }

    @Override
    public Receive<Fetcher.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Fetch.class, this::onFetch)
                .build();
    }
    public Behavior<Command> onConf(Conf command) {
        isInitialized = true;
        this.conf = command;
        return this;
    }
    public Behavior<Command> onFetch(Fetch command) {
        logger.debug("fetcher #{}, with name: {}, is fetching", getContext().getSelf().toString(), id);
        var urlJob = dequeJob();
        if (urlJob == null) {
            return this;
        }
        logger.info("crawling: {}", urlJob.normalizedUrl);
        return this;
    }
    private QueueJob dequeJob() {
        // find a global time mechanism
        long currentTime = System.currentTimeMillis();
        long targetTs = currentTime - conf.coolDown;
        Object result = jedis.fcall("dequeue_crawl_job", Arrays.asList(conf.hostNamesKey, conf.hostNamesKey), Collections.singletonList(Long.toString(targetTs)));
        if (result == null) {
            logger.info("Failed to dequeue crawl job");
        }
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        try {
            return objectMapper.readValue((byte[]) result, QueueJob.class);
        } catch (IOException e) {
            logger.error("Failed to decode crawling job", e);
            return null;
        }
    }
}
