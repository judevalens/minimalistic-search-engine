package org.quisqueya.macaya.spider;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.net.URI;

public class Frontier extends AbstractBehavior<Frontier.Command> {
    Jedis jedis;
    private final Logger logger
            = LoggerFactory.getLogger(this.getClass());
    private Frontier(ActorContext<Command> context, JedisPool jedisPool) {
        super(context);
        try {
            jedis = jedisPool.getResource();
        }catch (Exception e) {
           logger.error("Failed to init Jedis, error: {}\n",e.getMessage());
        }
    }

    public interface Command {}
    public static class QueueUrl {
        final String url;

        public QueueUrl(String url) {
            this.url = url;
        }
    }

    String crawlSession = "spider-crawler";

    public Behavior<Command> create(JedisPool jedisPool) {
        return Behaviors.setup((context)-> new Frontier(context,jedisPool));
    }

    @Override
    public Receive<Command> createReceive() {
        return null;
    }

    private Behavior<Command> onQueueUrl(QueueUrl queueUrl) {
        URI uri = URI.create(queueUrl.url);
        jedis.sismember(crawlSession, queueUrl.url);
        return Behaviors.same();
    }

    private void getLastCrawled(String host) {

    }
}
