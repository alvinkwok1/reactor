package example;

import java.io.IOException;

public class LoggingAcceptor implements EventHandler {

    private Handle handle;

    public LoggingAcceptor(Handle handle) {
        this.handle = handle;
    }

    @Override
    public void handle_event(EventType eventType) {
        // accept new connection
        try {
            Handle childHandle = handle.accept();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Handle get_handle() {
        return handle;
    }
}
