package de.k3b.android;

import android.util.Log;

import de.k3b.common.Logger;

/**
 * Android specific Logger implementation that maps to android logging.
 * Created by k3b on 31.07.2014.
 */
public class LoggerImpl implements Logger {
    private final String tag;

    public LoggerImpl(String tag) {
        this.tag = tag;
    }

    @Override
    public void d(String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void i(String msg) {
        Log.i(tag, msg);
    }

    @Override
    public void w(String msg) {
        Log.w(tag, msg);
    }

    @Override
    public void e(String msg) {
        Log.e(tag, msg);
    }
}
