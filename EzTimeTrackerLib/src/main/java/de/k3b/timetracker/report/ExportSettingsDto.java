package de.k3b.timetracker.report;

import java.io.Serializable;

/**
 * Parameter set by
 *
 * @author k3b
 */
public class ExportSettingsDto implements Serializable, ExportSettings {
    private static final long serialVersionUID = -3435801574095324069L;
    private String exportFormat;
    private String fileName;
    private boolean useSendTo;

    public ExportSettingsDto() {
    }

    public ExportSettingsDto(ExportSettings source) {
        ExportSettingsDto.copy(this, source);
    }

    public static void copy(ExportSettings destination, ExportSettings source) {
        if ((destination != null) && (source != null)) {
            destination.setExportFormat(source.getExportFormat());
            destination.setFileName(source.getFileName());
            destination.setUseSendTo(source.isUseSendTo());
        }
    }

    /* (non-Javadoc)
     * @see de.k3b.timetracker.report.ExportSettings#getExportFormat()
     */
    @Override
    public String getExportFormat() {
        return exportFormat;
    }

    /* (non-Javadoc)
     * @see de.k3b.timetracker.report.ExportSettings#setExportFormat(java.lang.String)
     */
    @Override
    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    /* (non-Javadoc)
     * @see de.k3b.timetracker.report.ExportSettings#getFileName()
     */
    @Override
    public String getFileName() {
        return fileName;
    }

    /* (non-Javadoc)
     * @see de.k3b.timetracker.report.ExportSettings#setFileName(java.lang.String)
     */
    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /* (non-Javadoc)
     * @see de.k3b.timetracker.report.ExportSettings#isUseSendTo()
     */
    @Override
    public boolean isUseSendTo() {
        return useSendTo;
    }

    /* (non-Javadoc)
     * @see de.k3b.timetracker.report.ExportSettings#setUseSendTo(boolean)
     */
    @Override
    public void setUseSendTo(boolean useSendTo) {
        this.useSendTo = useSendTo;
    }
}
