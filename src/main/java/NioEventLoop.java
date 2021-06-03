import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class NioEventLoop implements EventLoop {

    private final Queue<Runnable> taskQueue;

    private final NioEventLoopGroup group;

    private final Thread thread;

    /**
     * 用于事件循环的selector
     */
    private Selector selector;

    public NioEventLoop(NioEventLoopGroup group) {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.group = group;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException("initial selector failed");
        }
        this.thread = Thread.currentThread();
    }

    /**
     * 事件循环
     */
    @Override
    public void handleEvents() throws IOException {
        int selectCnt = 0;
        for (; ; ) {
            try {
                if (!hasTask()) {
                    selectCnt = selector.select(100);
                }
                if (selectCnt > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
                        Handle mainHandle = (Handle) selectionKey.attachment();
                        // 获取对应的Channel
                        if (selectionKey.isAcceptable()) {
                            // 触发接受事件
                            // 创建新的socket，然后加入到，从组中选择一个Dispatcher注册
                            mainHandle.read();
                            // 触发read事件
                        } else if (selectionKey.isReadable()) {
                            // 触发读事件
                            mainHandle.read();
                        } else if (selectionKey.isWritable()) {
                            // 触发写事件,刷出缓冲区数据
                            // 触发读事件
                            mainHandle.flush();
                        } else if (selectionKey.isConnectable()) {
                            int ops = selectionKey.interestOps();
                            ops &= ~SelectionKey.OP_CONNECT;
                            selectionKey.interestOps(ops);
                            mainHandle.finishConnect();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 执行剩余的所有任务
                Runnable task;
                while ((task = taskQueue.poll()) != null) {
                    task.run();
                }
            }

        }
    }

    @Override
    public boolean register(Handle handle) {
        // 绑定handle的eventLoop,同时注册感兴趣的事件
        handle.register(this);
        return true;
    }

    /**
     * 移除事件处理器
     *
     * @param key 待注册资源
     * @return 移除成功返回true，失败返回false
     */
    @Override
    public boolean cancel(SelectionKey key) {
        key.cancel();
        return true;
    }

    @Override
    public Selector selector() {
        return selector;
    }

    @Override
    public void execute(Runnable task) {
        this.taskQueue.offer(task);
        this.selector.wakeup();
    }

    public boolean inEventLoop() {
        return inEventLoop(Thread.currentThread());
    }

    private boolean hasTask() {
        return !taskQueue.isEmpty();
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return thread == this.thread;
    }
}
