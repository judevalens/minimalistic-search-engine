package org.quisqueya.macaya.spider;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Frontier extends AbstractBehavior<Frontier.Command> {

    private Frontier(ActorContext<Command> context) {
        super(context);
    }

    public interface Command {}

    public static class QueueUrl {
        final String url;

        public QueueUrl(String url) {
            this.url = url;
        }
    }

    public Behavior<Command> create() {
        return Behaviors.setup(Frontier::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return null;
    }

    private Behavior<Command> onQueueUrl(QueueUrl queueUrl) {

        return Behaviors.same();
    }
}
