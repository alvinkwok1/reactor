package cn.alvinkwok.core;

public interface ChannelPromise extends ChannelFuture, Promise<Void> {
    @Override
    Channel channel();

    @Override
    ChannelPromise setSuccess(Void result);

    ChannelPromise setSuccess();

    @Override
    ChannelPromise setFailure(Throwable cause);

    @Override
    ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener);

    @Override
    ChannelPromise sync() throws InterruptedException;

    @Override
    ChannelPromise await() throws InterruptedException;
}
