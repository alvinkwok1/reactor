import java.io.IOException;
import java.net.SocketAddress;

public interface Handle {
    /**
     * 触发读事件
     */
    void read();

    /**
     * 写数据
     */
    void write(byte[] data);

    /**
     * 刷出数据
     */
    void flush();

    /**
     * 建立连接
     */
    void connect(SocketAddress socketAddress) throws IOException;

    /**
     * 建立连接结束后的操作流程
     */
    void finishConnect();

    /**
     * 关闭连接
     */
    void close();

    /**
     * bind端口
     */
    void bind(SocketAddress localAddress) throws IOException;

    EventLoop eventLoop();

    boolean inEventLoop();

    /**
     * 将当前的资源注册到eventLoop上
     */
    void register(EventLoop eventLoop);

    boolean isActive();

    boolean isOpen();

    /**
     * 设置事件处理器
     * @param eventHandler
     */
    void eventHandler(EventHandler eventHandler);

}
