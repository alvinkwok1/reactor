package cn.alvinkwok.example;

public interface EventHandler {

    void handle_event(int readyOps);

    Handle get_handle();
}
