package de.k3b.timetracker.report;

import java.util.List;

import de.k3b.timetracker.DateTimeFormatter;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.util.DateTimeUtil;

public class CsvDetailReportRenderer {
	private static final String CSV_FIELD_DELIMITER = "\"";
	private static final String CSV_FIELD_SEPERATOR = ";";
	private static final String CSV_LINE_SEPERATOR = "\n";
	private static final String CSV_FIELD_DELIMITER_REPLACEMENT = "'";

	private static DateTimeUtil dateFormatter = DateTimeFormatter.getInstance();

	public String createReport(final List<Object> list) {
		final StringBuilder output = new StringBuilder();
		addLine(output, "Start","End","DurationInMinutes","CategoryName","CategoryDescription","Notes");
		for (final Object aTimeSlice : list) {
			if (aTimeSlice.getClass().isAssignableFrom(TimeSlice.class)) {
				addLine(output, (TimeSlice) aTimeSlice);
			}
		}
		return output.toString();
	}

	private void addLine(final StringBuilder output,
			final TimeSlice timeSlice) {
		String start = dateFormatter.getIsoDateTimeStr(timeSlice.getStartTime());
		String end = dateFormatter.getIsoDateTimeStr(timeSlice.getEndTime());
		String duration = "" + timeSlice.getDurationInMinutes(); 
		String categoryName = timeSlice.getCategoryName();
		String categoryDescription = timeSlice.getCategoryDescription();
		String notes = timeSlice.getNotes().replace(CSV_LINE_SEPERATOR, " ");
		
		addLine(output, start, end, duration, categoryName,
				categoryDescription, notes);
	}

	public static StringBuilder addLine(final StringBuilder result, String... parameters) {
		int paramCount = (parameters != null) ? parameters.length : 0;
		if (paramCount > 0) {
			addField(result,parameters[0]);
			for(int i=1; i < paramCount; i++){
				addField(result.append(CSV_FIELD_SEPERATOR),parameters[i]);				
			}
			result.append(CSV_LINE_SEPERATOR);
		}
		return result;
	}

	private static StringBuilder addField(StringBuilder result, String field) {
		if (needReplacement(field)) {
			result.append(CSV_FIELD_DELIMITER)
				.append(field.replace(CSV_FIELD_DELIMITER, CSV_FIELD_DELIMITER_REPLACEMENT))
				.append(CSV_FIELD_DELIMITER);			
		} else if (field != null){
			result.append(field);
		}
		return result;
	}

	private static boolean needReplacement(String field) {
		if  (field != null) {
			return field.contains(CSV_FIELD_DELIMITER) ||
					field.contains(CSV_FIELD_SEPERATOR) ||
					field.contains(CSV_LINE_SEPERATOR);
		}
		return false;
	}
}
