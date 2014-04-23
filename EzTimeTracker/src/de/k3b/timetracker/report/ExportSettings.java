package de.k3b.timetracker.report;

import de.k3b.timetracker.activity.TimeSliceFilterParameter;

public interface ExportSettings {
	public abstract String getExportFormat();

	public abstract void setExportFormat(String exportFormat);

	public abstract String getFileName();

	public abstract void setFileName(String fileName);

	public abstract boolean isUseSendTo();

	public abstract void setUseSendTo(boolean useSendTo);

}