import java.util.Arrays;
import java.util.logging.Logger;

public class EventHandlerFactory {

    private static final Logger log = Logger.getLogger(EventHandlerFactory.class.getName());

    public static EventHandler getEventHandler() {
        return new EventHandler() {

            /**
             * 处理读事件
             *
             * @param handle 与事件处理器绑定的handle
             * @param data   读出的数据
             */
            @Override
            public void handleRead(Handle handle,Object data) {
                System.out.println(Arrays.toString((byte[]) data));
            }

            @Override
            public void handleException(Handle handle, Exception e) {
                e.printStackTrace();
            }

            @Override
            public void handleUnActive(Handle handle) {
                System.out.println("handle close");
            }

            @Override
            public void handleInActive(Handle handle) {
                System.out.println("handle openn");
                byte[] data = new byte[1];
                data[0] = 56;
                handle.write(data);
            }
        };
    }
}
