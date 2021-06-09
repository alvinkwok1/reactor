package cn.alvinkwok.core;

public interface ChannelFuture extends Future<Void> {
    Channel channel();

    ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    ChannelFuture sync() throws InterruptedException;

    ChannelFuture await() throws InterruptedException;
}
