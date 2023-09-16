package net.atlas.combatify.util;

import org.apache.logging.log4j.Logger;

public class PrefixLogger {
    private static final String LOG_FORMAT = "[{}] {}";

    private final Logger logger;
    private final String prefix;

    public PrefixLogger(Logger logger) {
        this.logger = logger;
        this.prefix = logger.getName();
    }

    public void info(String message) {
        logger.info(LOG_FORMAT, prefix, message);
    }

    public void error(String message) {
        logger.error(LOG_FORMAT, prefix, message);
    }
	public void error(String message, Object error) {
		logger.error(LOG_FORMAT, prefix, message, error);
	}

    public void warn(String message) {
        logger.warn(LOG_FORMAT, prefix, message);
    }

    public Logger unwrap() {
        return logger;
    }
}
