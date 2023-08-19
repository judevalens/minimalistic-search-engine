package org.quisqueya.macaya.spider;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.typed.Cluster;
import org.apache.hadoop.mapred.Master;

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

    private RootBehavior(ActorContext<Void> context) {
        super(context);
        Cluster cluster = Cluster.get(context.getSystem());
        context.spawn(Spider.create(),"Spider");
    }

    public static Behavior<Void> create() {
        return Behaviors.setup(RootBehavior::new);
    }

    @Override
    public Receive<Void> createReceive() {
        return null;
    }
}
