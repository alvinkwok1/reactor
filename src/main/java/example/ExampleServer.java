package example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

public class ExampleServer {
    public static void main(String[] args) {
        try {
            // open socket listener
            ServerSocketChannel serverSocketChannel = SelectorProvider.provider().openServerSocketChannel();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(2000));
            // create a handle
            Handle handle = new Handle(serverSocketChannel);
            // create a event_handler
            EventHandler acceptor = new LoggingAcceptor(handle);
            // register a handler
            InitiationDispatcher.getInstance().register_handler(acceptor, SelectionKey.OP_ACCEPT);
            // open event loop
            InitiationDispatcher.getInstance().handle_events();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
