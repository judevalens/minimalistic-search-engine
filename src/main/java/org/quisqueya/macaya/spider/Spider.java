package org.quisqueya.macaya.spider;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Spider extends AbstractBehavior<Spider.Command> {
    public interface Command {
    }

    public static class SpiderConf implements Command {
        public SpiderConf() {
        }
    }

    private static final String FILE_TEST_URL = "/home/jude/Downloads/norvig.com_big.txt";
    private SpiderConf conf;

    private Spider(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Spider::new);
    }

    @Override
    public Receive<Command> createReceive() {

        return newReceiveBuilder()
                .onMessage(SpiderConf.class, this::onConf)
                .build();
    }

    public Behavior<Command> onConf(SpiderConf conf) {
        this.conf = conf;
        return this;
    }

}
