package core;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface EventLoop {
    /**
     * 事件循环
     */
    void handleEvents() throws IOException;

    /**
     * 注册事件处理器
     * @param channel 资源处理器
     * @return 注册成功返回true，失败返回false
     */
    ChannelFuture register(ChannelPromise promise);

    /**
     * 移除事件处理器
     * @param key 资源处理器
     * @return 移除成功返回true，失败返回false
     */
    boolean cancel(SelectionKey key);

    Selector selector();

    void execute(Runnable task);


    boolean inEventLoop(Thread thread);

    boolean inEventLoop();

    void shutdown();

}
