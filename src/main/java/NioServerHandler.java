import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class NioServerHandler extends AbstractNioHandle{

    private static ServerSocketChannel newSocket(){
        try {
            return SelectorProvider.provider().openServerSocketChannel();
        }catch (IOException e) {
            throw new HandleException("can't open socket");
        }
    }

    public NioServerHandler() {
        this(newSocket());
    }

    public NioServerHandler(SelectableChannel channel) {
        super(channel);
    }

    @Override
    protected ServerSocketChannel javaChannel() {
        return (ServerSocketChannel) super.javaChannel();
    }

    @Override
    public void read() {
        ServerSocketChannel serverChannel = (ServerSocketChannel) channel;
        try {
            SelectableChannel socketChannel = serverChannel.accept();
            socketChannel.configureBlocking(false);
            Handle handle = new NioHandle(socketChannel);
            // 调用处理逻辑
            eventHandler.handleRead(this,handle);
        } catch (IOException e) {
            // 触发channel的异常事件
        }
    }

    @Override
    public boolean isActive() {
        return isOpen() && javaChannel().socket().isBound();
    }

}
