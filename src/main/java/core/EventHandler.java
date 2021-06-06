package core;

public interface EventHandler {
    /**
     * 处理读事件
     * @param channel 与事件处理器绑定的handle
     * @param data 读出的数据
     */
    void handleRead(Channel channel, Object data);

    void handleException(Channel channel, Throwable e);

    void handleUnActive(Channel channel);

    void handleInActive(Channel channel);
}
