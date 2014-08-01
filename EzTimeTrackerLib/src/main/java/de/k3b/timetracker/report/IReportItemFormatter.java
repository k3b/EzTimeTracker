package de.k3b.timetracker.report;

/**
 * to render an item-type specific manner
 * Created by k3b on 31.07.2014.
 */
public interface IReportItemFormatter {
    String getValueGeneric(Object obj);
}
