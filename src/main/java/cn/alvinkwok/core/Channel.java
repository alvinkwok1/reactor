package cn.alvinkwok.core;

import java.net.SocketAddress;

public interface Channel {
    /**
     * 触发读事件
     */
    void read();

    /**
     * 写数据
     */
    ChannelFuture write(byte[] data);

    /**
     * 刷出数据
     */
    void flush();

    /**
     * 建立连接
     */
    ChannelFuture connect(SocketAddress socketAddress,ChannelPromise promise);

    /**
     * 建立连接结束后的操作流程
     */
    void finishConnect();

    /**
     * 关闭连接
     */
    ChannelFuture close();

    /**
     * 用于返回关闭连接的closeFuture
     * @return
     */
    ChannelFuture closeFuture();

    /**
     * bind端口
     */
    ChannelFuture bind(SocketAddress localAddress,ChannelPromise promise);

    EventLoop eventLoop();

    /**
     * 将当前的资源注册到eventLoop上
     */
    void register(EventLoop eventLoop,ChannelPromise promise);

    void deRegister(ChannelPromise promise);

    boolean isActive();

    boolean isOpen();

    /**
     * 设置事件处理器
     * @param eventHandler
     */
    void eventHandler(EventHandler eventHandler);

    boolean inEventLoop();
}
