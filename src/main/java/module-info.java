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

    requires java.desktop;
    requires java.management;
    requires transitive java.sql;
    requires transitive java.xml;
}
