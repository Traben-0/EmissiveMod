package traben.tconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TConfigLog {

    private static final Logger LOGGER = LoggerFactory.getLogger("Entity Features");

    public static void log(String ID, String message) {
        LOGGER.info(ID + ": " + message);
    }

    public static void logError(String ID, String message) {
        LOGGER.error(ID + ": " + message);
    }

    @SuppressWarnings("unused")
    public static void logWarn(String ID, String message) {
        LOGGER.warn(ID + ": " + message);
    }
}
