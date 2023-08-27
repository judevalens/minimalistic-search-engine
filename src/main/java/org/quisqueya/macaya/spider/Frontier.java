package org.quisqueya.macaya.spider;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.msgpack.jackson.dataformat.msgpack.MessagePackFactory;
import org.quisqueya.macaya.JedisPoolManager;
import org.quisqueya.macaya.spider.pojos.QueueJob;
import org.quisqueya.macaya.utils.SpiderUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Frontier extends AbstractBehavior<Frontier.Command> {
    Jedis jedis;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Frontier(ActorContext<Command> context, JedisPoolManager jedisPool) {
        super(context);
        try {
            jedis = jedisPool.getJedis();
        } catch (Exception e) {
            logger.error("Failed to init Jedis, error: {}\n", e.getMessage());
        }
    }

    public interface Command {
    }

    public static class QueueUrl implements Command {
        final String url;

        public QueueUrl(String url) {
            this.url = url;
        }
    }

    String crawlSession = "spider-crawler";

    public static Behavior<Command> create(JedisPoolManager jedisPool) {
        return Behaviors.setup((context) -> new Frontier(context, jedisPool));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(Frontier.QueueUrl.class, this::onQueueUrl).build();
    }

    public Behavior<Command> onQueueUrl(@NotNull QueueUrl queueUrl) {
        System.out.printf("max CPU: %d\n", Runtime.getRuntime().availableProcessors());
        URI uri = URI.create(queueUrl.url+ new Random().nextInt(10000000));
        URI normalizedUri = SpiderUri.spiderNormalize(uri);
        QueueJob job = new QueueJob(uri.getHost(),queueUrl.url, normalizedUri.toString(), new Random().nextInt(10000000));
        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
        try {
            var serializedValue = objectMapper.writeValueAsBytes(job);
            var result = jedis.fcall("queue_crawling_job".getBytes(StandardCharsets.UTF_8), Arrays.asList(RedisKeys.HostNamesZset.getBytes(), RedisKeys.VisitedUrlSet.getBytes(), RedisKeys.SessionID.getBytes()), Collections.singletonList((serializedValue)));
            if (result == null || (Long) result == 0) {
                logger.debug("Failed to queue job {}", job.normalizedUrl);
                return this;
            }
            logger.info("Queued {} url crawl job: {}", result, job.normalizedUrl);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Behaviors.same();
    }

    private void getLastCrawled(String host) {

    }
}
