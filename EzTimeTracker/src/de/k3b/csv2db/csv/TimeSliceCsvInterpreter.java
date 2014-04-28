package de.k3b.csv2db.csv;

import java.text.ParseException;

import de.k3b.timetracker.database.ICategoryRepsitory;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;
import de.k3b.util.DateTimeUtil;

public class TimeSliceCsvInterpreter {
	private static DateTimeUtil dt = new DateTimeUtil(0);
	final int colStart;
	final int colEnd;
	final int colDuration;
	final int colNotes;
	final int colCategory;
	private ICategoryRepsitory categoryRepository;

	public TimeSliceCsvInterpreter(ICategoryRepsitory categoryRepository,
			String... headerColumns) {
		this.categoryRepository = categoryRepository;
		colStart = getCol("start", headerColumns);
		colEnd = getCol("end", headerColumns);
		colDuration = getCol("dur", headerColumns);
		colNotes = getCol("note", headerColumns);
		colCategory = getCol("cat", headerColumns);
	}

	protected int getCol(String colId, String... headerColumns) {
		for (int i = 0; i < headerColumns.length; i++) {
			if (headerColumns[i].toLowerCase().startsWith(colId)) {
				return i;
			}
		}
		return -1;
	}

	public TimeSlice parse(String... columns) throws CsvException {
		if ((columns != null) && (columns.length > 0)) {
			TimeSlice result = new TimeSlice();
			String value;
			
			value = getColumnValue(columns, colStart);
			if (value != null) {
				try {
					result.setStartTime(dt.parseIsoDate(columns[colStart]));
				} catch (ParseException e) {
					throw new CsvException("startDate",colStart,value,e);
				}
			}
			
			value = getColumnValue(columns, colEnd);
			if (value != null) {
				try {
					result.setEndTime(dt.parseIsoDate(columns[colEnd]));
				} catch (ParseException e) {
					throw new CsvException("endDate",colEnd,value,e);
				}
			}
			
			value = getColumnValue(columns, colDuration);
			if (value != null) {
				 
				try {
					int minutes = Integer.parseInt(columns[colDuration]);
					long value2 = dt.addMinutes(result.getStartTime(), minutes);
					result.setEndTime(value2);
				} catch (NumberFormatException e) {
					throw new CsvException("duratonInMinutes",colDuration,value,e);
				}
			}
			
			value = getColumnValue(columns, colNotes);
			if (value != null) {
				result.setNotes(columns[colNotes]);
			}

			value = getColumnValue(columns, colCategory);
			if (value != null) {
				TimeSliceCategory category = this.categoryRepository
						.getOrCreateCategory(value);
				result.setCategory(category);
			}
			return result;
		}
		return null;
	}

	private String getColumnValue(String[] columns, int index) {
		if ((index >= 0) & (index < columns.length)) {
			return columns[index];
		}
		return null;
	}

}
