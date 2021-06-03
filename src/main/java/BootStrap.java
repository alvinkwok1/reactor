import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class BootStrap {
    public static void main(String[] args) throws IOException {
        // 创建事件组，并完成bind
        NioEventLoopGroup group = new NioEventLoopGroup();
        // 初始化线程
        group.run(1);
        // 初始化一个服务端监听器
        NioHandle handle = new NioHandle();
        // 注册channel
        group.register(handle);
        // 进行bind动作
        handle.connect(new InetSocketAddress("localhost",2000));

    }
}
