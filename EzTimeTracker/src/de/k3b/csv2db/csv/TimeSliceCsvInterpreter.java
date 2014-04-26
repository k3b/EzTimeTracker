package de.k3b.csv2db.csv;

import java.text.ParseException;

import de.k3b.timetracker.DateTimeFormatter;
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

	public TimeSliceCsvInterpreter(ICategoryRepsitory categoryRepository, String... headerColumns) {
		this.categoryRepository = categoryRepository;
		colStart = getCol("start", headerColumns);
		colEnd = getCol("end", headerColumns);
		colDuration = getCol("dur", headerColumns);
		colNotes = getCol("note", headerColumns);
		colCategory = getCol("cat", headerColumns);
	}

	protected int getCol(String colId, String... headerColumns) {
		for (int i=0;i < headerColumns.length; i++)
		{
			if (headerColumns[i].toLowerCase().startsWith(colId)) {
				return i;
			}
		}
		return -1;
	}

	public TimeSlice parse(String... columns) throws ParseException {
		TimeSlice result = new TimeSlice();
		if ((colStart >= 0) & (colStart < columns.length)){
			long value = dt.parseIsoDate(columns[colStart]);
			result.setStartTime(value);
		}
		if ((colEnd >= 0) & (colEnd < columns.length)){
			long value = dt.parseIsoDate(columns[colEnd]);
			result.setEndTime(value);
		}
		if ((colDuration >= 0) & (colDuration < columns.length)) {
			int minutes = Integer.parseInt(columns[colDuration]);
			long value = dt.addMinutes(result.getStartTime(),minutes);
			result.setEndTime(value);
		}
		if ((colNotes >= 0) & (colNotes < columns.length)) {
			result.setNotes(columns[colNotes]);
		}
		
		if ((colCategory >= 0) & (colCategory < columns.length)) {
			TimeSliceCategory value = this.categoryRepository.getOrCreateTimeSlice(columns[colCategory]);
			result.setCategory(value);
		}
		return result;
	}

}
