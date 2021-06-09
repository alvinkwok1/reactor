package cn.alvinkwok.core;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class AbstractNioChannel implements Channel {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    protected EventHandler eventHandler = EventHandlerFactory.getEventHandler();

    protected SelectableChannel channel;

    protected SelectionKey selectionKey;

    protected EventLoop eventLoop;

    protected ChannelPromise connectPromise;

    protected ChannelPromise closePromise = newPromise();

    protected ByteBuffer rcvBuffer = ByteBuffer.allocate(1024);
    protected ByteBuffer wrBuffer = ByteBuffer.allocate(1024);

    public AbstractNioChannel() {
    }

    public AbstractNioChannel(SelectableChannel channel) {
        try {
            this.channel = channel;
            this.channel.configureBlocking(false);
        } catch (IOException e) {
            throw new HandleException("occur IO Exception", e);
        }
    }

    /**
     * 触发读事件
     */
    @Override
    public void read() {
        // 读取数据，触发事件
        if (!isActive()) {
            return;
        }
        try {
            doRead();
        }catch (final Exception e) {
            eventHandler.handleException(this,e);
            close();
        }
    }

    /**
     * 写数据
     *
     * @param data
     */
    @Override
    public ChannelFuture write(byte[] data) {
        return null;
    }

    /**
     * 刷出数据
     */
    @Override
    public void flush() {
        // 往
    }

    /**
     * 建立连接
     */
    @Override
    public ChannelFuture connect(SocketAddress socketAddress,ChannelPromise channelPromise) {
        if (eventLoop.inEventLoop()){
            doConnect(socketAddress,channelPromise);
        } else {
            eventLoop.execute(()->doConnect(socketAddress,channelPromise));
        }
        return channelPromise;
    }

    protected void doConnect(SocketAddress socketAddress,ChannelPromise promise) {
        try {
            SocketChannel socketChannel = (SocketChannel) this.channel;
            this.connectPromise = promise;
            // 进行连接
            socketChannel.configureBlocking(false);
            boolean connected = socketChannel.connect(socketAddress);
            // 注册连接事件
            socketChannel.register(eventLoop.selector(), SelectionKey.OP_CONNECT, this);
            if (connected) {
                if (isActive()) {
                    // 通知已经连接建立成功
                    promise.setSuccess();
                    eventHandler.handleInActive(this);
                }
            }
        }catch (Exception e) {
            promise.setFailure(e);
        }
    }

    /**
     * 建立连接结束后的操作流程
     */
    @Override
    public void finishConnect() {

    }

    /**
     * 关闭连接
     */
    @Override
    public ChannelPromise close() {
       if (eventLoop.inEventLoop(Thread.currentThread())) {
           doClose(closePromise);
       } else {
           eventLoop().execute(()->doClose(closePromise));
       }
       return closePromise;
    }

    @Override
    public ChannelFuture closeFuture() {
        return closePromise;
    }

    public void doClose(ChannelPromise promise) {
        try {
            javaChannel().close();
            eventHandler.handleUnActive(this);
            // 触发解除注册
            deRegister(promise);
        } catch (IOException e) {
            eventHandler.handleException(this, e);
        }
    }

    /**
     * bind端口
     */
    @Override
    public ChannelFuture bind(SocketAddress localAddress,ChannelPromise promise) {
        doBind(localAddress,promise);
        return promise;
    }

    protected void doBind(SocketAddress localAddress,ChannelPromise promise) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) this.channel;
        try {
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(localAddress);
            logger.info("bind address " + localAddress.toString());
            // 注册接受事件
            channel.register(eventLoop.selector(), SelectionKey.OP_ACCEPT, this);
        } catch (IOException e) {
            promise.setFailure(e);
        }
    }

    @Override
    public EventLoop eventLoop() {
        return eventLoop;
    }

    /**
     * 将当前的资源注册到eventLoop上
     *
     * @param eventLoop
     */
    @Override
    public void register(EventLoop eventLoop,ChannelPromise promise) {
        if (eventLoop.inEventLoop()) {
            doRegister(eventLoop,promise);
        } else {
            eventLoop.execute(()->doRegister(eventLoop,promise));
        }
    }



    protected void doRegister(EventLoop eventLoop,ChannelPromise promise) {
        try {
            this.eventLoop = eventLoop;
            promise.setSuccess(null);
        } catch (Exception e) {
            promise.setFailure(e);
        }
    }

    @Override
    public void deRegister(ChannelPromise promise) {
        if (inEventLoop()) {
            doDeRegister(promise);
        } else {
            eventLoop.execute(()->{
                doDeRegister(promise);
            });
        }
    }

    public void doDeRegister(ChannelPromise promise) {
        try {
            // 将channel取消注册
            eventLoop().cancel(selectionKey);
            promise.setSuccess();
        }catch (Throwable e) {
            promise.setFailure(e);
        }
    }
    protected SelectableChannel javaChannel() {
        return channel;
    }

    protected void doRead() {

    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public boolean isActive() {
        return false;
    }

    /**
     * 设置事件处理器
     *
     * @param eventHandler
     */
    @Override
    public void eventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public boolean inEventLoop() {
        return eventLoop.inEventLoop(Thread.currentThread());
    }

    public final ChannelPromise newPromise() {
        return new DefaultChannelPromise(this);
    }

}
