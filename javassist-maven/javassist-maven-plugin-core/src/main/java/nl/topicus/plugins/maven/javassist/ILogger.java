package nl.topicus.plugins.maven.javassist;

import java.io.File;

public interface ILogger {

    public void addMessage(File file, int line, int pos, String message,
            Throwable e);

    /**
     * Log a message at the DEBUG level.
     *
     * @param message
     *            the message string to be logged
     */
    public void debug(String message);

    /**
     * Log a message at the DEBUG level.
     *
     * @param message
     *            the message string to be logged
     * @param throwable
     *            the exception (throwable) to log
     */
    public void debug(String message, Throwable throwable);

    /**
     * Log a message at the INFO level.
     *
     * @param message
     *            the message string to be logged
     */
    public void info(String message);

    /**
     * Log a message at the INFO level.
     *
     * @param message
     *            the message string to be logged
     * @param throwable
     *            the exception (throwable) to log
     */
    public void info(String message, Throwable throwable);

    /**
     * Log a message at the WARN level.
     *
     * @param message
     *            the message string to be logged
     */
    public void warn(String message);

    /**
     * Log a message at the WARN level.
     *
     * @param message
     *            the message string to be logged
     * @param throwable
     *            the exception (throwable) to log
     */
    public void warn(String message, Throwable throwable);

    /**
     * Log a message at the ERROR level.
     *
     * @param message
     *            the message string to be logged
     */
    public void error(String message);

    /**
     * Log a message at the ERROR level.
     *
     * @param message
     *            the message string to be logged
     * @param throwable
     *            the exception (throwable) to log
     */
    public void error(String message, Throwable throwable);
}
