package core;

import bootstrap.Server;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerBootStrap {

    private EventGroup bossGroup;

    private EventGroup workerGroup;

    private EventHandler bossHandler;

    private InitialEventHandler childHandler;

    private ServerBootStrap self() {
        return this;
    }

    public ServerBootStrap group(EventGroup boss, EventGroup workerGroup) {
        this.bossGroup = boss;
        this.workerGroup = workerGroup;
        return self();
    }

    public ServerBootStrap childHandler(InitialEventHandler initialEventHandler) {
        this.childHandler = initialEventHandler;
        return this;
    }


    public ChannelFuture bind(InetSocketAddress address) {
        // 启动线程
        bossGroup.run();
        workerGroup.run();
        // 初始化Channel
        // 初始化一个服务端监听器
        NioServerChannel nioServerChannel = new NioServerChannel();
        nioServerChannel.eventHandler(childHandler.getEventHandler());
        // 注册到组
        ChannelFuture regFuture = bossGroup.register(nioServerChannel);
        if (regFuture.cause() != null) {
            nioServerChannel.close();
            return regFuture;
        }
        if (regFuture.isDone()) {
            ChannelPromise promise = nioServerChannel.newPromise();
            doBind(regFuture, nioServerChannel, address, promise);
            return promise;
        } else {
            ChannelPromise promise = nioServerChannel.newPromise();
            regFuture.addListener((ChannelFutureListener) future -> {
                Throwable cause = future.cause();
                if (cause != null) {
                    promise.setFailure(cause);
                } else {
                    doBind(regFuture, nioServerChannel, address, promise);
                }
            });
            return promise;
        }
    }

    private void doBind(final ChannelFuture channelFuture, Channel channel, InetSocketAddress socketAddress, ChannelPromise promise) {
        channel.eventLoop().execute(() -> {
            if (channelFuture.isSuccess()) {
                channel.bind(socketAddress, promise).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            future.channel().close();
                        }
                    }
                });
            }
        });
    }
}
