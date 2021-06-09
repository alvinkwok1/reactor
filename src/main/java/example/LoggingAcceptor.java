package example;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class LoggingAcceptor implements EventHandler {

    private Handle handle;

    public LoggingAcceptor(Handle handle) {
        this.handle = handle;
    }

    @Override
    public void handle_event(int readyOps) {
        if ((readyOps & SelectionKey.OP_ACCEPT) != 0) {
            try {
                // accept new connection
                Handle childHandle = handle.accept();
                // create new Event Handler
                EventHandler eventHandler = new LoggingHandler(childHandle);
                // register to Initiation Dispatcher
                InitiationDispatcher.getInstance().register_handler(eventHandler,SelectionKey.OP_READ);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Handle get_handle() {
        return handle;
    }
}
