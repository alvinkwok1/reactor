package example;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class InitiationDispatcher {

    Selector selector = Selector.open();

    private static final AtomicInteger inc = new AtomicInteger();

    /**
     * public class part
     */
    private static InitiationDispatcher[] dispatchers;

    private static Thread[] threads;

    public static void init(int size) throws IOException {
        if (size < 1)
            throw new IllegalArgumentException();
        dispatchers = new InitiationDispatcher[size];
        for (int i = 0; i < size; i++) {
            dispatchers[i] = new InitiationDispatcher();
        }
        threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            int idx = i;
            threads[idx] = new Thread(() -> dispatchers[idx].handle_events());
        }
    }

    public static void start() {
        if (threads.length == 1) {
            // current start
            threads[0].run();
        } else {
            for (int i = 0; i < threads.length; i++) {
                threads[i].start();
            }
        }
    }


    public InitiationDispatcher() throws IOException {

    }

    void handle_events() {
        for (; ; ) {
            try {
                if (selector.select() > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        EventHandler handler = (EventHandler) key.attachment();
                        if (key.isReadable()) {
                            handler.handle_event(EventType.OP_READ);
                        } else if (key.isAcceptable()) {
                            handler.handle_event(EventType.OP_ACCEPT);
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean register_handler(EventHandler eventHandler, int ops) {
        Selector selector = next().selector();
        try {
            eventHandler.get_handle().register(selector, SelectionKey.OP_ACCEPT);
            return true;
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean remove_handler(EventHandler eventHandler, int ops) {
        Selector selector = next().selector();
        try {
            eventHandler.get_handle().unRegister(selector);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public Selector selector() {
        return selector;
    }

    public static InitiationDispatcher next() {
        int idx = Math.abs(inc.incrementAndGet()) % dispatchers.length;
        return dispatchers[idx];
    }
}
