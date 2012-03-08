package com.zettsett.timetracker.report;

import java.util.List;

import android.widget.TextView;

public class ReportOutput {

	private List<TextView> reportList;
	private String output;
	private String terminator = "\r\n";

	private ReportOutput() {

	}

	public static ReportOutput makeFormatter(List<TextView> reportList) {
		ReportOutput f = new ReportOutput();
		f.reportList = reportList;
		return f;
	}

	public String getOutput() {
		StringBuilder builder = new StringBuilder();
		if (output == null) {
			for (TextView view : reportList) {
				builder.append(view.getText().toString());
				builder.append(terminator);
			}
			output = builder.toString();
		}
		return output;
	}

	public void setTerminator(String terminator) {
		this.terminator = terminator;
	}

}
