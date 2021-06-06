package core;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BootStrap {
    private EventGroup workerGroup;

    private InitialEventHandler initialEventHandler;

    public BootStrap group(EventGroup eventGroup) {
        this.workerGroup = eventGroup;
        return this;
    }

    public BootStrap handler(InitialEventHandler initialEventHandler) {
        this.initialEventHandler = initialEventHandler;
        return this;
    }

    public ChannelFuture connect(InetSocketAddress socketAddress) {
        // 初始化channel
        // 初始化一个服务端监听器
        NioChannel nioChannel = new NioChannel();
        nioChannel.eventHandler(initialEventHandler.getEventHandler());
        // 注册到组
        ChannelFuture regFuture = workerGroup.register(nioChannel);

        if (regFuture.cause() != null) {
            nioChannel.close();
            return regFuture;
        }
        if (regFuture.isDone()) {
            ChannelPromise promise = nioChannel.newPromise();
            doConnect(regFuture, nioChannel, socketAddress, promise);
            return promise;
        } else {
            ChannelPromise promise = nioChannel.newPromise();
            regFuture.addListener((ChannelFutureListener) future -> {
                Throwable cause = future.cause();
                if (cause != null) {
                    promise.setFailure(cause);
                } else {
                    doConnect(regFuture, nioChannel, socketAddress, promise);
                }
            });
            return promise;
        }
    }

    private void doConnect(ChannelFuture channelFuture, Channel channel, InetSocketAddress address, ChannelPromise promise) {
        channel.eventLoop().execute(()->{
            channel.connect(address,promise);
            promise.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    channel.close();
                }
            });
        });
    }
}
