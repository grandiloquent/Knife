package org.slf4j;

public class LoggerFactory {

    public static Logger getLogger(Class<?> c) {
        return new DefaultLogger();
    }

    private static class DefaultLogger implements Logger {

        @Override
        public void warn(String message) {

        }
    }
}
