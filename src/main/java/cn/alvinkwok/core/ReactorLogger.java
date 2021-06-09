package cn.alvinkwok.core;

import java.util.logging.Logger;

public class ReactorLogger {
    public static final Logger logger = Logger.getLogger(ReactorLogger.class.getName());

    public static void info(String format,Object... args) {
      logger.info(getMessage(format,args));
    }

    public static void warn(String format,Object... args) {
        logger.warning(getMessage(format,args));
    }

    public static String getMessage (String format,Object... args) {
        String message = String.format(format,args);
        // 判断下后面一个是否是异常
        if (args != null && args.length > 0) {
            if (args[args.length -1] instanceof Exception) {
                message += args[args.length-1].toString();
            }
        }
        return message;
    }

    public static Logger getLogger() {
        return logger;
    }

}
