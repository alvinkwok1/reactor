package cn.alvinkwok.core;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class NioChannel extends AbstractNioChannel {
    private static SocketChannel newSocket() {
        try {
            return SelectorProvider.provider().openSocketChannel();
        } catch (IOException e) {
            throw new HandleException("can't open socket");
        }
    }

    public NioChannel() {
        super(newSocket());
    }

    public NioChannel(SelectableChannel channel) {
        super(channel);
    }

    @Override
    protected void doRegister(EventLoop eventLoop, ChannelPromise promise) {
        this.eventLoop = eventLoop;
        // 注册selectKey
        int ops = SelectionKey.OP_READ;
        try {
            this.selectionKey = channel.register(eventLoop().selector(), ops, this);
            promise.setSuccess(null);
        } catch (ClosedChannelException e) {
            promise.setFailure(e);
        }
    }

    @Override
    protected SocketChannel javaChannel() {
        return (SocketChannel) super.javaChannel();
    }

    @Override
    protected void doRead() {
        try {
            int size = javaChannel().read(rcvBuffer);
            if (size < 0) {
                // 连接被关闭
                close();
                return;
            } else if (size == 0) {
                return;
            }
            rcvBuffer.flip();
            // 转移到数组中
            byte[] data = new byte[size];
            rcvBuffer.get(data);
            rcvBuffer.clear();
            eventHandler.handleRead(this, data);
        } catch (Exception e) {
            eventHandler.handleException(this, e);
            close();
        }
    }

    @Override
    public ChannelFuture write(byte[] data) {
        ChannelPromise promise = newPromise();
        eventLoop.execute(() -> {
            int offset = 0;
            while (offset < data.length) {
                int wl = Math.min(data.length - offset, 1024);
                wrBuffer.put(data, offset, wl);
                offset += wl;
                wrBuffer.flip();
                try {
                    javaChannel().write(wrBuffer);
                } catch (IOException e) {
                    eventHandler.handleException(this, e);
                    promise.setFailure(e);
                    break;
                }
            }
            if (promise.cause() == null) {
                promise.setSuccess(null);
            }
        });
        return promise;
    }

    @Override
    public void finishConnect() {
        try {
            javaChannel().finishConnect();
            // 注册读事件
            channel.register(eventLoop().selector(), SelectionKey.OP_READ, this);
            if (isActive()) {
                this.connectPromise.setSuccess();
                // 触发inactive时间
                eventHandler.handleInActive(this);
            }
        } catch (Exception e) {
            eventHandler.handleException(this, e);
            connectPromise.setFailure(e);
        }
    }

    @Override
    public boolean isActive() {
        SocketChannel ch = javaChannel();
        return ch.isOpen() && ch.isConnected();
    }
}
