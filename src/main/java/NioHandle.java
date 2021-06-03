import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class NioHandle extends AbstractNioHandle {
    private static SocketChannel newSocket(){
        try {
            return SelectorProvider.provider().openSocketChannel();
        }catch (IOException e) {
            throw new HandleException("can't open socket");
        }
    }

    public NioHandle()  {
        super(newSocket());
    }

    public NioHandle(SelectableChannel channel) {
        super(channel);
    }

    @Override
    protected void doRegister() {
        try {
            // 注册selectKey
            int ops = SelectionKey.OP_READ;
            this.selectionKey = channel.register(eventLoop().selector(), ops, this);
        } catch (Exception e) {
            // 触发异常
            eventHandler.handleException(this, e);
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
    public void write(byte[] data) {
        int offset = 0;
        while (offset < data.length) {
            int wl = Math.min(data.length - offset, 1024);
            wrBuffer.put(data,offset,wl);
            offset +=wl;
            wrBuffer.flip();
            try {
                javaChannel().write(wrBuffer);
            } catch (IOException e) {
                eventHandler.handleException(this,e);
                break;
            }
        }
    }

    @Override
    public void finishConnect() {
        try {
            javaChannel().finishConnect();
            // 注册读事件
            channel.register(eventLoop().selector(),SelectionKey.OP_READ,this);
            eventHandler.handleInActive(this);
        }catch (Exception e) {
            eventHandler.handleException(this,e);
        }
    }

    @Override
    public boolean isActive() {
        SocketChannel ch = javaChannel();
        return ch.isOpen() && ch.isConnected();
    }
}
