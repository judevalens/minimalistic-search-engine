package org.quisqueya.macaya.spider;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.typed.Cluster;
import org.apache.hadoop.mapred.Master;
import org.apache.logging.slf4j.Log4jLogger;
import org.quisqueya.macaya.JedisPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootBehavior extends AbstractBehavior<Void> {
    public enum Role {
        Master("master"),
        Fetcher("fetcher"),
        Parser("parser");
        final String role;
        Role(String role) {
            this.role = role;
        }

        @Override
        public String toString() {
            return role;
        }
    }
    private final Logger logger
            = LoggerFactory.getLogger(this.getClass());

    private RootBehavior(ActorContext<Void> context) {
        super(context);
        Cluster cluster = Cluster.get(context.getSystem());
        context.spawn(Spider.create(logger),"Spider");
    }

    public static Behavior<Void> create() {
        return Behaviors.setup(RootBehavior::new);
    }

    @Override
    public Receive<Void> createReceive() {
        return null;
    }
}
