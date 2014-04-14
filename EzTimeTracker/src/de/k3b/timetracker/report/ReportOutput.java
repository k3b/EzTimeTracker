package de.k3b.timetracker.report;

import java.util.List;

import de.k3b.timetracker.activity.ReportItemFormatterEx;

public class ReportOutput {

	private List<Object> reportList;
	private String output;
	private ReportItemFormatterEx formatter;

	private ReportOutput() {

	}

	public static ReportOutput makeFormatter(final List<Object> reportList,
			final ReportItemFormatterEx formatter) {
		final ReportOutput f = new ReportOutput();
		f.reportList = reportList;
		f.formatter = formatter;
		return f;
	}

	public String getOutput() {
		if (this.output == null) {
			final StringBuilder builder = new StringBuilder();
			for (final Object reportItem : this.reportList) {
				builder.append(this.formatter.getValueGeneric(reportItem));
			}
			this.output = builder.toString();
		}
		return this.output;
	}

	public ReportOutput setLineTerminator(final String lineTerminator) {
		this.formatter.setLineTerminator(lineTerminator);
		return this;
	}

}
