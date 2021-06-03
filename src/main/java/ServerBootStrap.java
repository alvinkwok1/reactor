import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class ServerBootStrap {
    public static void main(String[] args) throws IOException {
        // 专用于连接的线程池组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        // 专用于IO事件处理的线程组
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        // 初始化线程
        bossGroup.run(1);
        // 初始化一个服务端监听器
        NioServerHandler nioServerHandler = new NioServerHandler();
        // 设置连接的事件处理器
        nioServerHandler.eventHandler(new EventHandler() {
            @Override
            public void handleRead(Handle handle, Object data) {
                // 完成注册
                Handle childHanle = (Handle) data;
                if (handle.eventLoop().inEventLoop())
            }

            @Override
            public void handleException(Handle handle, Exception e) {

            }

            @Override
            public void handleUnActive(Handle handle) {

            }

            @Override
            public void handleInActive(Handle handle) {

            }
        });


        // 注册channel
        bossGroup.register(nioServerHandler);
        // 进行bind动作
        nioServerHandler.bind(new InetSocketAddress(2000));
    }
}
