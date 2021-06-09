package example;

import java.io.IOException;
import java.nio.channels.*;

public class Handle {
    private final SelectableChannel selectableChannel;

    private SelectionKey selectionKey;

    public Handle(SelectableChannel selectableChannel) {
        this.selectableChannel = selectableChannel;
    }

    public void register(Selector selector, int eventType) throws ClosedChannelException {
        if (selectableChannel.isOpen()) {
            selectionKey = selectableChannel.register(selector, eventType, this);
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
        return new Handle(channel);
    }
}
