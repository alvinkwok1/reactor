package cn.alvinkwok.core;

public interface EventGroup {
    void run();

    ChannelFuture register(Channel channel);

    void shutdown();
}
