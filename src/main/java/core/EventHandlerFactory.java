package core;

import java.util.Arrays;
import java.util.logging.Logger;

public class EventHandlerFactory {

    private static final Logger log = Logger.getLogger(EventHandlerFactory.class.getName());

    public static EventHandler getEventHandler() {
        return new EventHandler() {

            /**
             * 处理读事件
             *
             * @param channel 与事件处理器绑定的handle
             * @param data   读出的数据
             */
            @Override
            public void handleRead(Channel channel, Object data) {
                System.out.println(Arrays.toString((byte[]) data));
            }

            @Override
            public void handleException(Channel channel, Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void handleUnActive(Channel channel) {
                System.out.println("handle close");
            }

            @Override
            public void handleInActive(Channel channel) {
                System.out.println("handle openn");
            }
        };
    }
}
