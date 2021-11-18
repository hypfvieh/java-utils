module hypfvieh.java.utils {
    exports com.github.hypfvieh.collections;
    exports com.github.hypfvieh.system;
    exports com.github.hypfvieh.files;
    exports com.github.hypfvieh.util;
    exports com.github.hypfvieh.stream;
    exports com.github.hypfvieh.util.xml;
    exports com.github.hypfvieh.threads;
    exports com.github.hypfvieh.formatter;
    exports com.github.hypfvieh.imaging;
    exports com.github.hypfvieh.common;
    exports com.github.hypfvieh.classloader;
    exports com.github.hypfvieh.db;
    exports com.github.hypfvieh.config.xml;

    requires java.desktop;
    requires java.management;
    requires transitive java.sql;
    requires transitive java.xml;
}
