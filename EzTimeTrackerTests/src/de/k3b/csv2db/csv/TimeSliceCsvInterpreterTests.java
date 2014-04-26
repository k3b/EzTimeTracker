package de.k3b.csv2db.csv;

import java.text.ParseException;

import org.junit.*;

import de.k3b.timetracker.DateTimeFormatter;
import de.k3b.timetracker.database.ICategoryRepsitory;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;

public class TimeSliceCsvInterpreterTests {
	@Test
	public void shouldFindColum() {
		TimeSliceCsvInterpreter sut = new TimeSliceCsvInterpreter(null);
		int found = sut.getCol("not", "Start", "Duration", "notes","CategoryName");
		Assert.assertEquals("found", 2, found);
		
		found = sut.getCol("xx", "Start", "Duration", "notes","CategoryName");
		Assert.assertEquals("not found", -1, found);
	}

	@Test
	public void shouldParseNoData() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(null, "Start").parse();
	}

	@Test
	public void shouldParseStartTime() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(null, "Start").parse("2014-04-25T17:10:27+0200");
		Assert.assertEquals("date", "2014-04-25T17:10:27+0200" , new DateTimeFormatter().getIsoDateTimeStr(result.getStartTime()));
	}

	@Test
	public void shouldParseDuration() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(null, "Start", "Duration").parse("2014-04-25T17:10:27+0200","13");
		Assert.assertEquals("date", "2014-04-25T17:23:27+0200" , new DateTimeFormatter().getIsoDateTimeStr(result.getEndTime()));
	}

	@Test
	public void shouldParseEndTime() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(null, "End").parse("2014-04-25T17:10:27+0200");
		Assert.assertEquals("date", "2014-04-25T17:10:27+0200" , new DateTimeFormatter().getIsoDateTimeStr(result.getEndTime()));
	}

	@Test
	public void shouldParseNotes() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(null, "Notes").parse("hallo\nWelt");
		Assert.assertEquals("hallo\nWelt" , result.getNotes());
	}

	class RepositoryMock implements ICategoryRepsitory {
		@Override
		public TimeSliceCategory getOrCreateTimeSlice(String name) {
			// TODO Auto-generated method stub
			TimeSliceCategory result = new TimeSliceCategory();
			result.setCategoryName(name);
			return result;
		}		
	}
	
	@Test
	public void shouldParseCategory() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(new RepositoryMock(), "Category").parse("hallo");
		Assert.assertEquals("hallo" , result.getCategory().getCategoryName());
	}
}
