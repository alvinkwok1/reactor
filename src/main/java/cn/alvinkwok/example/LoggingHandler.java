package cn.alvinkwok.example;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class LoggingHandler implements EventHandler {

    private Handle handle;

    public LoggingHandler(Handle handle) {
        this.handle = handle;
    }

    @Override
    public void handle_event(int readyOps) {
        if ((readyOps & SelectionKey.OP_READ) != 0) {
            try {
                byte[] data = handle.read();
                if (data != null) {
                    // write to other device
                    System.out.println(new String(data));
                } else {
                    // socket is close
                    InitiationDispatcher.getInstance().remove_handler(this, SelectionKey.OP_READ);
                    System.out.println("client close!");
                }
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
