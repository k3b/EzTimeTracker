package de.k3b.common;

/**
 * Android indepenant Encaüsulation of logging
 * Created by k3b on 31.07.2014.
 */
public interface Logger {
    void d(String msg);

    void i(String msg);

    void w(String msg);

    void e(String msg);
}
