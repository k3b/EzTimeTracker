package com.zettsett.timetracker.report;

import java.util.List;

import android.widget.TextView;

public class ReportOutput {

	private List<TextView> reportList;
	private String output;
	private String terminator = "\r\n";

	private ReportOutput() {

	}

	public static ReportOutput makeFormatter(final List<TextView> reportList) {
		final ReportOutput f = new ReportOutput();
		f.reportList = reportList;
		return f;
	}

	public String getOutput() {
		final StringBuilder builder = new StringBuilder();
		if (this.output == null) {
			for (final TextView view : this.reportList) {
				builder.append(view.getText().toString());
				builder.append(this.terminator);
			}
			this.output = builder.toString();
		}
		return this.output;
	}

	public void setTerminator(final String terminator) {
		this.terminator = terminator;
	}

}
