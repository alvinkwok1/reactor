package example;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class Main {
    public static void main(String[] args) {
        try {
            InitiationDispatcher.init(2);
            ServerSocketChannel serverSocketChannel = SelectorProvider.provider().openServerSocketChannel();
            serverSocketChannel.configureBlocking(false);
            Handle handle = new Handle(serverSocketChannel);
            EventHandler acceptor = new LoggingAcceptor(handle);
            InitiationDispatcher.register_handler(acceptor, SelectionKey.OP_ACCEPT);

            InitiationDispatcher.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
