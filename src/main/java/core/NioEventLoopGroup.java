package core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NioEventLoopGroup implements EventGroup {
    List<NioEventLoop> dispatchers;
    List<Thread> threads;

    private AtomicInteger inc = new AtomicInteger(10);

    private int size;


    public NioEventLoopGroup(int size) {
        this.size = size;
        run();
    }

    public void run() {
        // 初始化Dispatcher
        dispatchers = new ArrayList<>(size);
        threads = new ArrayList<>(size);
        for (int i=0;i<size;i++) {
            NioEventLoop dispatcher = new NioEventLoop(this);
            dispatchers.add(dispatcher);
            // 注册到线程
            Thread thread = new Thread(() -> {
                try {
                    dispatcher.handleEvents();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            threads.add(thread);
        }
    }

    public ChannelFuture register(Channel channel) {
        return register(new DefaultChannelPromise(channel));
    }

    private ChannelFuture register(final DefaultChannelPromise promise) {
        // 选择一个dispatcher然后注册
        int idx = Math.abs(inc.incrementAndGet()) % size;
        return dispatchers.get(idx).register(promise);
    }

    public void waitDown(){
        for (Thread thread : threads) {
            // 等待结束
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void shutdown() {
        for (int i=0;i<size;i++) {
            dispatchers.get(i).shutdown();
        }
    }
}
