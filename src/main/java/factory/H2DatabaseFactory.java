package factory;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * dao
 */
public class H2DatabaseFactory {
    private static final Logger logger = LoggerFactory.getLogger(H2DatabaseFactory.class);
    private static Server server;

    private H2DatabaseFactory() {
    }

    public static void startServer() {
        try {
            logger.info("Starting H2DatabaseServer");
            H2DatabaseFactory.server = Server
                    .createTcpServer("-tcpPort", ConnectionFactory.getDatabaseProperty("PORT"), "-tcpAllowOthers")
                    .start();
            logger.info("Start H2DatabaseServer success ({})", server.getURL());
        } catch (SQLException exc) {
            logger.error("H2Database server start error", exc);
        }
    }

    public static void stopServer() {
        logger.info("Stopping H2DatabaseServer");
        String url = server.getURL();
        H2DatabaseFactory.server.stop();
        logger.info("Stop H2DatabaseServer success ({})", url);
    }
}
