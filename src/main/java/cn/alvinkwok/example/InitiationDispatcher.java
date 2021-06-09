package cn.alvinkwok.example;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class InitiationDispatcher {

    private Selector selector;

    private static InitiationDispatcher self = new InitiationDispatcher();

    private InitiationDispatcher() {
    }

    public static InitiationDispatcher getInstance() {
        return self;
    }

    void handle_events() throws IOException {
        // open selector
        openSelector();
        // do event loop
        for (; ; ) {
            try {
                // wait event occur
                if (selector.select() > 0) {
                    // get notified handle
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    // for handle
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        // callback Event Handler
                        EventHandler handler = (EventHandler) key.attachment();
                        handler.handle_event(key.readyOps());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    // register Event Handler to dispatcher
    public boolean register_handler(EventHandler eventHandler, int ops) throws IOException {
        openSelector();
        eventHandler.get_handle().register(selector(), ops, eventHandler);
        return true;

    }

    // remove Event Handler  from dispatcher
    public boolean remove_handler(EventHandler eventHandler, int ops) {
        if (selector == null) {
            return false;
        }
        try {
            eventHandler.get_handle().unRegister(selector());
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private Selector selector() {
        return selector;
    }

    private void openSelector() throws IOException {
        if (selector == null || !selector.isOpen()) {
            // close old selector
            if (selector != null) {
                selector.close();
            }
            selector = Selector.open();
        }
    }
}
