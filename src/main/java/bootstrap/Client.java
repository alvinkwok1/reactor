package bootstrap;

import core.*;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws InterruptedException {


        for (int i = 0; i < 1; i++) {
            new Thread(() -> {
                BootStrap sb = new BootStrap();

                // 专用于IO事件处理的线程组
                NioEventLoopGroup workGroup = new NioEventLoopGroup(1);
                sb.group(workGroup)
                    .handler(() -> new EventHandler() {
                        @Override
                        public void handleRead(Channel channel, Object data) {

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
                            channel.write("123".getBytes(StandardCharsets.UTF_8)).addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    future.channel().close();
                                }
                            });
                        }
                    });

                for (; ; ) {
                    ChannelFuture channelFuture = null;
                    try {
                        channelFuture = sb.connect(new InetSocketAddress(2000)).sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        channelFuture.channel().closeFuture().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }
}
