package core;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
public class NioServerChannel extends AbstractNioChannel {

    private static ServerSocketChannel newSocket(){
        try {
            return SelectorProvider.provider().openServerSocketChannel();
        }catch (IOException e) {
            throw new HandleException("can't open socket");
        }
    }

    public NioServerChannel() {
        this(newSocket());
    }

    public NioServerChannel(SelectableChannel channel) {
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
            Channel channel = new NioChannel(socketChannel);
            // 调用处理逻辑
            eventHandler.handleRead(this, channel);
        } catch (IOException e) {
            // 触发channel的异常事件
        }
    }

    @Override
    public boolean isActive() {
        return isOpen() && javaChannel().socket().isBound();
    }


}
