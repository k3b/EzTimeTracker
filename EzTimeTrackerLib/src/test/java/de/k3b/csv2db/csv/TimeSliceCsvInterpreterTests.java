package de.k3b.csv2db.csv;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;

import de.k3b.timetracker.model.TimeSlice;
import de.k3b.util.DateTimeUtil;


public class TimeSliceCsvInterpreterTests {

    private final DateTimeUtil dateTimeUtil = new DateTimeUtil(0);

    @Test
    public void shouldFindColumnIndexFromName() {
        TimeSliceCsvInterpreter sut = new TimeSliceCsvInterpreter(null);
        int found = sut.getColumnIndexFromName("not", "Start", "Duration", "notes", "CategoryName");
        Assert.assertEquals("found", 2, found);
	}

    @Test
    public void shouldFindColumnIndexFromNameEx() {
        TimeSliceCsvInterpreter sut = new TimeSliceCsvInterpreter(null);
        int found = sut.getColumnIndexFromName("Not(es)", "Start", "Duration", "notes", "CategoryName");
        Assert.assertEquals("found", 2, found);
    }

    @Test
    public void shouldNotFindColumnIndexFromUnknownName() {
        TimeSliceCsvInterpreter sut = new TimeSliceCsvInterpreter(null);
        int found = sut.getColumnIndexFromName("xx", "Start", "Duration", "notes", "CategoryName");
        Assert.assertEquals("not found", -1, found);
    }

    @Test
    public void shouldParseNoData() throws ParseException {
        TimeSlice result = new TimeSliceCsvInterpreter(null, "Start").parse();
        Assert.assertEquals(null, result);
    }

	@Test
	public void shouldParseStartTime() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(null, "Start").parse("2014-04-25T17:10:27+0200");
        Assert.assertEquals("date", "2014-04-25T17:10:27+0200", dateTimeUtil.getIsoDateTimeStr(result.getStartTime()));
    }

	@Test
	public void shouldParseDuration() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(null, "Start", "Duration").parse("2014-04-25T17:10:27+0200","13");
        Assert.assertEquals("date", "2014-04-25T17:23:27+0200", dateTimeUtil.getIsoDateTimeStr(result.getEndTime()));
    }

    @Test
    public void shouldThrowIfParseDurationWithoutStart() throws ParseException {
        try {
            new TimeSliceCsvInterpreter(null, "Duration");
            Assert.fail("expected CsvException Duration requires StartDate");
        } catch (CsvException e) {
            System.out.println("got expected exception " + e.getMessage());
        }
    }

    @Test
    public void shouldThrowIfParseDurationWithoutStartData() throws ParseException {
        try {
            new TimeSliceCsvInterpreter(null, "Start", "Duration").parse("", "13");
            Assert.fail("expected CsvException Duration requires StartDate");
        } catch (CsvException e) {
            System.out.println("got expected exception " + e.getMessage());
        }
    }

    @Test
    public void shouldParseEndTime() throws ParseException {
        TimeSlice result = new TimeSliceCsvInterpreter(null, "End").parse("2014-04-25T17:10:27+0200");
        Assert.assertEquals("date", "2014-04-25T17:10:27+0200", dateTimeUtil.getIsoDateTimeStr(result.getEndTime()));
    }

	@Test
	public void shouldParseNotes() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(null, "Notes").parse("hallo\nWelt");
		Assert.assertEquals("hallo\nWelt" , result.getNotes());
	}

	@Test
	public void shouldParseCategory() throws ParseException {
		TimeSlice result = new TimeSliceCsvInterpreter(new CategoryRepositoryMock(), "Category").parse("hallo");
		Assert.assertEquals("hallo" , result.getCategory().getCategoryName());
	}
	
	@Test
	public void shouldThrowOnIllegalFormat() throws ParseException {
		try {
            new TimeSliceCsvInterpreter(null, "Start").parse("this is not a valid DateTime");
            Assert.fail("missing exception");
        } catch (CsvException e) {
			System.out.println("got expected exception " + e.getMessage());
		}
	}


}
