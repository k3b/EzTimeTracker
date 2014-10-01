package de.k3b.timetracker;

/**
 * Android independant Abstraction from settings
 * Created by k3b on 31.07.2014.
 */
public interface Settings {
    /**
     * Punchout only if longer than this (in seconds). Else discard.
     */
    long getMinPunchOutTreshholdInMilliSecs();
}
