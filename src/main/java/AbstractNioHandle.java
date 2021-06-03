
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class AbstractNioHandle implements Handle {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    protected EventHandler eventHandler = EventHandlerFactory.getEventHandler();

    protected SelectableChannel channel;

    protected SelectionKey selectionKey;

    private EventLoop eventLoop;

    protected ByteBuffer rcvBuffer = ByteBuffer.allocate(1024);
    protected ByteBuffer wrBuffer = ByteBuffer.allocate(1024);

    public AbstractNioHandle() {
    }

    public AbstractNioHandle(SelectableChannel channel) {
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
        doRead();
    }

    /**
     * 写数据
     *
     * @param data
     */
    @Override
    public void write(byte[] data) {

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
    public void connect(SocketAddress socketAddress) {
        if (eventLoop.inEventLoop(Thread.currentThread())){
            doConnect(socketAddress);
        } else {
            eventLoop.execute(()->doConnect(socketAddress));
        }
    }

    protected void doConnect(SocketAddress socketAddress) {
        try {
            SocketChannel socketChannel = (SocketChannel) this.channel;
            // 进行连接
            socketChannel.configureBlocking(false);
            socketChannel.connect(socketAddress);
            // 注册连接事件
            socketChannel.register(eventLoop.selector(), SelectionKey.OP_CONNECT, this);
            if (isActive()) {
                eventHandler.handleInActive(this);
            }
        }catch (Exception e) {
            eventHandler.handleException(this,e);
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
    public void close() {
       if (eventLoop.inEventLoop(Thread.currentThread())) {
           doClose();
       } else {
           eventLoop().execute(()->doClose());
       }
    }

    public void doClose() {
        try {
            javaChannel().close();
            eventHandler.handleUnActive(this);
            // 将channel取消注册
            eventLoop().cancel(selectionKey);
        } catch (IOException e) {
            eventHandler.handleException(this, e);
        }
    }

    /**
     * bind端口
     */
    @Override
    public void bind(SocketAddress localAddress) throws IOException {
        if (eventLoop.inEventLoop(Thread.currentThread())) {
            doBind(localAddress);
        } else {
            eventLoop.execute(() -> doBind(localAddress));
        }

    }

    protected void doBind(SocketAddress localAddress) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) this.channel;
        try {
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(localAddress);
            logger.info("bind address " + localAddress.toString());
            // 注册接受事件
            channel.register(eventLoop.selector(), SelectionKey.OP_ACCEPT, this);
        } catch (IOException e) {
            eventHandler.handleException(this, e);
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
    public void register(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
        doRegister();
        if (isActive()) {
            eventHandler.handleInActive(this);
        }
    }

    protected void doRegister() {
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
}
