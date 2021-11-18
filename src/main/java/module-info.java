module com.github.hypfvieh.java.utils {
    opens com.github.hypfvieh.collections;
    opens com.github.hypfvieh.system;
    opens com.github.hypfvieh.files;
    opens com.github.hypfvieh.util;
    opens com.github.hypfvieh.stream;
    opens com.github.hypfvieh.util.xml;
    opens com.github.hypfvieh.threads;
    opens com.github.hypfvieh.formatter;
    opens com.github.hypfvieh.imaging;
    opens com.github.hypfvieh.common;
    opens com.github.hypfvieh.classloader;
    opens com.github.hypfvieh.db;
    opens com.github.hypfvieh.config.xml;

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
