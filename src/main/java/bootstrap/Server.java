package bootstrap;

import core.*;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

public class Server {
    private static volatile AtomicLong inc = new AtomicLong();

    public static void main(String[] args) {

        ServerBootStrap sb = new ServerBootStrap();


        // 专用于连接的线程池组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 专用于IO事件处理的线程组
        NioEventLoopGroup workGroup = new NioEventLoopGroup(20);
        sb.group(bossGroup, workGroup)
            .childHandler(() -> new EventHandler() {

                @Override
                public void handleRead(Channel channel, Object data) {
                    System.out.println(inc.incrementAndGet());
                }

                @Override
                public void handleException(Channel channel, Throwable e) {

                }

                @Override
                public void handleUnActive(Channel channel) {

                }

                @Override
                public void handleInActive(Channel channel) {

                }
            });
        try {
            sb.bind(new InetSocketAddress(2000)).sync();
        } catch (InterruptedException e) {
            bossGroup.shutdown();
            workGroup.shutdown();
        }
    }

}
