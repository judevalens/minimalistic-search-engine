package org.quisqueya.macaya.spider;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class RootBehavior extends AbstractBehavior<Void> {
    private RootBehavior(ActorContext<Void> context) {
        super(context);
    }
    public static Behavior<Void> create() {
        return Behaviors.setup(RootBehavior::new);
    }
    @Override
    public Receive<Void> createReceive() {
        return null;
    }
}
