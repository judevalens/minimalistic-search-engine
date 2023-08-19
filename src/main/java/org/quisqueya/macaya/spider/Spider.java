package org.quisqueya.macaya.spider;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.cluster.typed.Cluster;

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
    private SpiderConf conf;

    private Spider(ActorContext<Command> context) {
        super(context);
        Cluster cluster = Cluster.get(context.getSystem());
        System.out.printf("Master node is up: %s! address: %s\n",cluster.state().isMemberUp(cluster.selfMember().address())
        ,cluster.selfMember().address().toString());

    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Spider::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return   newReceiveBuilder()
                .onMessage(SpiderConf.class, this::onConf)
                .build();
    }

    public Behavior<Command> onConf(SpiderConf conf) {
        this.conf = conf;
        return this;
    }

}
