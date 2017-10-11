package com.github.hypfvieh.common;

/**
 * Defines where to look for a library.
 *
 */
public enum SearchOrder {
    /** Look in any given external path */
    CUSTOM_PATH,
    /** Look in classpath, this includes directory and the jar(s) */
    CLASS_PATH,
    /** Look in system path (e.g. /usr/lib on linux/unix systems) */
    SYSTEM_PATH;
}