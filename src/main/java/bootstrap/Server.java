package bootstrap;

import core.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) throws IOException {

        ServerBootStrap sb = new ServerBootStrap();

        // 专用于连接的线程池组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 专用于IO事件处理的线程组
        NioEventLoopGroup workGroup = new NioEventLoopGroup(20);
        sb.group(bossGroup,workGroup)
            .childHandler(new InitialEventHandler() {
                @Override
                public EventHandler getEventHandler() {
                   return new EventHandler() {
                        @Override
                        public void handleRead(Channel channel, Object data) {
                            // 完成注册
                            Channel childChannel = (Channel) data;
                            workGroup.register(childChannel);
                        }

                        @Override
                        public void handleException(Channel channel, Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void handleUnActive(Channel channel) {

                        }

                        @Override
                        public void handleInActive(Channel channel) {

                        }
                    };
                }
            });
        try {
            ChannelFuture cf = sb.bind(new InetSocketAddress(2000)).sync();
        } catch (InterruptedException e) {
            bossGroup.shutdown();
            workGroup.shutdown();

        }
    }

}
