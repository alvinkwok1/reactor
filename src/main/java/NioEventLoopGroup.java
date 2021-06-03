import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NioEventLoopGroup {
    List<NioEventLoop> dispatchers;
    List<Thread> threads;

    public void run(int size) {
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

    void register(Handle handle) {
        // 选择一个dispatcher然后注册
        Random random = new Random();
        int idx = random.nextInt(dispatchers.size());
        dispatchers.get(idx).register(handle);
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
}
