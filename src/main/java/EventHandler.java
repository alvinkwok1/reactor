public interface EventHandler {
    /**
     * 处理读事件
     * @param handle 与事件处理器绑定的handle
     * @param data 读出的数据
     */
    void handleRead(Handle handle,Object data);

    void handleException(Handle handle,Exception e);

    void handleUnActive(Handle handle);

    void handleInActive(Handle handle);
}
