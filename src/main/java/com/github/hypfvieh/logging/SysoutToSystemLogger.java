package com.github.hypfvieh.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * Helper class to redirect all System.out/err.print/println() calls to System.Logger.
 * <p>
 * All messages on stdout will be logged as 'INFO', while all messages on stderr will
 * be logged as 'ERROR'.
 *
 * @author hypfvieh
 * @since v1.2.1 - 2022-09-19
 */
public final class SysoutToSystemLogger {

    private static final Logger LOGGER = System.getLogger(SysoutToSystemLogger.class.getName());

    private static PrintStream oldStdOut;
    private static PrintStream oldStdErr;

    private SysoutToSystemLogger() {
        // hidden utility constructor
    }

    /**
     * Redirect all calls to System.out/System.err to the logging system.
     * @param _sysoutLogLevel log level to use for stdout messages
     * @param _sysErrLogLevel log level to use for syserr messages
     */
    @SuppressWarnings("resource")
    public static void enableRedirectSysout(Level _sysoutLogLevel, Level _sysErrLogLevel) {
        if (_sysoutLogLevel != null && !(System.out instanceof SystemLoggerPrintStream)) {
            oldStdOut = System.out;
            LOGGER.log(Level.INFO, "Enabling sysout redirection");
            System.setOut(new SystemLoggerPrintStream(_sysoutLogLevel, new NullOutputStream()));
        }
        if (_sysErrLogLevel != null && !(System.err instanceof SystemLoggerPrintStream)) {
            oldStdErr = System.err;
            LOGGER.log(Level.INFO, "Enabling syserr redirection");
            System.setErr(new SystemLoggerPrintStream(_sysErrLogLevel, new NullOutputStream()));
        }
    }


    /**
     * Remove redirection to logging system for System.out/System.err and restore previous configuration.
     */
    public static void disableRedirectSysout() {
        if (System.out instanceof SystemLoggerPrintStream) {
            LOGGER.log(Level.INFO, "Disabling sysout redirection");
            System.out.close(); // close system logger print stream before replacing
            System.setOut(oldStdOut);
            oldStdOut = null;
        }
        if (System.err instanceof SystemLoggerPrintStream) {
            LOGGER.log(Level.INFO, "Disabling sysout redirection");
            System.err.close(); // close system logger print stream before replacing
            System.setErr(oldStdErr);
            oldStdErr = null;
        }
    }

    static class SystemLoggerPrintStream extends PrintStream {

        private final Level logLevel;

        SystemLoggerPrintStream(Level _loglevel, OutputStream _org) {
            super(_org);
            logLevel = _loglevel;
        }

        @Override
        public void println(String _x) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            // Element 0 is getStackTrace
            // Element 1 is println
            // Element 2 is the caller
            StackTraceElement caller = stack[2];
            System.getLogger(caller.getClassName()).log(logLevel, _x);
        }

        @Override
        public void print(String _s) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            // Element 0 is getStackTrace
            // Element 1 is print
            // Element 2 is the caller
            StackTraceElement caller = stack[2];
            System.getLogger(caller.getClassName()).log(logLevel, _s);
        }

        @Override
        public void print(boolean _b) {
            print(String.valueOf(_b));
        }

        @Override
        public void print(char _c) {
            print(String.valueOf(_c));
        }

        @Override
        public void print(int _i) {
            print(String.valueOf(_i));
        }

        @Override
        public void print(long _l) {
            print(String.valueOf(_l));
        }

        @Override
        public void print(float _f) {
            print(String.valueOf(_f));
        }

        @Override
        public void print(double _d) {
            print(String.valueOf(_d));
        }

        @Override
        public void print(char[] _s) {
            print(String.valueOf(_s));
        }

        @Override
        public void print(Object _obj) {
            print(String.valueOf(_obj));
        }

        @Override
        public void println() {
            println("");
        }

        @Override
        public void println(boolean _x) {
            println(String.valueOf(_x));
        }

        @Override
        public void println(char _x) {
            println(String.valueOf(_x));
        }

        @Override
        public void println(int _x) {
            println(String.valueOf(_x));
        }

        @Override
        public void println(long _x) {
            println(String.valueOf(_x));
        }

        @Override
        public void println(float _x) {
            println(String.valueOf(_x));
        }

        @Override
        public void println(double _x) {
            println(String.valueOf(_x));
        }

        @Override
        public void println(char[] _x) {
            println(String.valueOf(_x));
        }

        @Override
        public void println(Object _x) {
            println(String.valueOf(_x));
        }

    }

    /**
     * Output stream which will omit any given value (aka /dev/null).
     *
     * @author hypfvieh
     * @since v1.0.3 - 2021-06-03
     */
    static class NullOutputStream extends OutputStream {

        @Override
        public void write(byte[] _b, int _off, int _len) {
            // do nothing
        }

        @Override
        public void write(int _b) {
            // do nothing
        }

        @Override
        public void write(byte[] _b) throws IOException {
            // do nothing
        }

    }
}