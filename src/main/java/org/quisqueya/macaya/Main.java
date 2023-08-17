package org.quisqueya.macaya;

import akka.actor.typed.ActorSystem;
import org.quisqueya.macaya.spider.Spider;

public class Main {
    public static void main(String[] args) {
        System.out.println("hello world");
        ActorSystem<Spider.Command> spider = ActorSystem.create(Spider.create(),"master");
        System.out.println("End of spider");
        spider.terminate();
    }
}
