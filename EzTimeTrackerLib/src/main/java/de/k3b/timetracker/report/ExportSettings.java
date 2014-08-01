package de.k3b.timetracker.report;

public interface ExportSettings {
    public abstract String getExportFormat();

    public abstract void setExportFormat(String exportFormat);

    public abstract String getFileName();

    public abstract void setFileName(String fileName);

    public abstract boolean isUseSendTo();

    public abstract void setUseSendTo(boolean useSendTo);

}