package cn.alvinkwok.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class Handle {
    private final SelectableChannel selectableChannel;

    private SelectionKey selectionKey;

    public Handle(SelectableChannel selectableChannel) {
        this.selectableChannel = selectableChannel;
    }

    public void register(Selector selector, int ops, Object attachment) throws ClosedChannelException {
        if (selectableChannel.isOpen()) {
            selectionKey = selectableChannel.register(selector, ops, attachment);
            selector.wakeup();
        }
    }

    public void unRegister(Selector selector) {
        selectionKey.cancel();
        selector.wakeup();
    }

    public Handle accept() throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectableChannel;
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);
        return new Handle(channel);
    }

    public byte[] read() throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectableChannel;
        byte[] rcv = new byte[100 * 1024];
        ByteBuffer bf = ByteBuffer.allocate(1024);
        int len, pos = 0;
        while ((len = socketChannel.read(bf)) > 0) {
            bf.flip();
            System.arraycopy(bf.array(), 0, rcv, pos, len);
            pos += len;
            bf.clear();
        }
        if (len == -1) {
            return null;
        }
        byte[] result = new byte[pos];
        System.arraycopy(rcv, 0, result, 0, pos);
        return result;
    }
}
