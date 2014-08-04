package de.k3b.timetracker.database;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import de.k3b.common.database.SqlFilter;
import de.k3b.timetracker.TimeSliceFilterParameter;
import de.k3b.timetracker.model.TimeSlice;
import de.k3b.timetracker.model.TimeSliceCategory;

public class TimeSliceSqlTest {
    private final TimeSliceCategory cat = new TimeSliceCategory(1, "name").setStartTime(100).setEndTime(200).setDescription("descripton");
    private final TimeSliceCategoryRepsitoryMock tsRepository = new TimeSliceCategoryRepsitoryMock(cat);
    private final TimeSlice ts = new TimeSlice(2).setCategory(cat).setEndTime(400).setNotes("notes").setStartTime(300);
    TimeSliceFilterParameter timeSliceFilter;

    @Before
    public void setUp() {
        this.timeSliceFilter = new TimeSliceFilterParameter();
    }

    @Test
    public void ShouldFormatEmpty() {
        final SqlFilter sqlFilter = this.createFilter();

        Assert.assertNotNull("sqlFilter", sqlFilter);
        Assert.assertNull("sqlFilter.args", sqlFilter.args);
        Assert.assertNull("sqlFilter.sql", sqlFilter.sql);
    }

    @Test
    public void ShouldFormatCategory() {

        this.timeSliceFilter.setCategoryId(22);
        final SqlFilter sqlFilter = this.createFilter();

        Assert.assertEquals(sqlFilter.getDebugMessage("sql"),
                "category_id = ?", sqlFilter.sql);
        Assert.assertEquals(sqlFilter.getDebugMessage("args.length"), 1,
                sqlFilter.args.length);
        Assert.assertEquals(sqlFilter.getDebugMessage("args[0]"), "22",
                sqlFilter.args[0]);
    }

    @Test
    public void ShouldFormatIgnoreDate() {
        this.timeSliceFilter.setStartTime(20010101).setEndTime(20011231)
                .setIgnoreDates(true);
        final SqlFilter sqlFilter = this.createFilter();

        Assert.assertNotNull("sqlFilter", sqlFilter);
        Assert.assertNull("sqlFilter.args", sqlFilter.args);
        Assert.assertNull("sqlFilter.sql", sqlFilter.sql);
    }

    @Test
    public void ShouldFormatDateFromTo() {
        this.timeSliceFilter.setStartTime(20010101).setEndTime(20011231);
        final SqlFilter sqlFilter = this.createFilter();

        Assert.assertEquals(sqlFilter.getDebugMessage("sql"),
                "start_time>= ? AND start_time<= ?", sqlFilter.sql);
        Assert.assertEquals(sqlFilter.getDebugMessage("args.length"), 2,
                sqlFilter.args.length);
        Assert.assertEquals(sqlFilter.getDebugMessage("args[0]"), "20010101",
                sqlFilter.args[0]);
        Assert.assertEquals(sqlFilter.getDebugMessage("args[1]"), "20011231",
                sqlFilter.args[1]);
    }

    @Test
    public void ShouldFormatNotesNotNull() {

        this.timeSliceFilter.setNotesNotNull(true);
        final SqlFilter sqlFilter = this.createFilter();

        Assert.assertEquals(sqlFilter.getDebugMessage("sql"),
                "notes IS NOT NULL AND notes <> ''", sqlFilter.sql);
        Assert.assertNull(sqlFilter.getDebugMessage("args"), sqlFilter.args);
    }

    @Test
    public void ShouldFormatNotesLike() {

        this.timeSliceFilter.setNotes("Hello World");
        final SqlFilter sqlFilter = this.createFilter();

        Assert.assertEquals(sqlFilter.getDebugMessage("sql"), "notes LIKE ?",
                sqlFilter.sql);
        Assert.assertEquals(sqlFilter.getDebugMessage("args.length"), 1,
                sqlFilter.args.length);
        Assert.assertEquals(sqlFilter.getDebugMessage("args[0]"),
                "%Hello World%", sqlFilter.args[0]);
    }

    @Test
    public void ShouldFormatAll() {

        this.timeSliceFilter.setParameter(20010101, 20011231, 22).setNotes("Hello World");
        final SqlFilter sqlFilter = this.createFilter();

        Assert.assertNotNull(sqlFilter.getDebugMessage("sql"), sqlFilter.sql);
        Assert.assertEquals(sqlFilter.getDebugMessage("args.length"), 4,
                sqlFilter.args.length);

        System.out.println(this.timeSliceFilter.toString() + ":" + sqlFilter.toString());
        System.out.println("default: " + TimeSliceFilterParameter.filterWithDefaultsIfNeccessary(null).toString());
    }

    @Test
    public void columnListShouldContainAllFlieds() {
        Map<String, String> values = TimeSliceSql.asMap(ts);
        Assert.assertEquals(TimeSliceSql.allColumnNames().length, values.size());
    }

    @Test
    public void shouldTransferAllFlieds() {
        Map<String, String> values = TimeSliceSql.asMap(ts);
        TimeSlice result = new TimeSlice();
        TimeSliceSql.fromMap(result, values, tsRepository);
        Assert.assertEquals("getRowId", ts.getRowId(), result.getRowId());
        Assert.assertEquals("getCategoryName", ts.getCategoryName(), result.getCategoryName());
        Assert.assertEquals("getStartTime", ts.getStartTime(), result.getStartTime());
        Assert.assertEquals("getEndTime", ts.getEndTime(), result.getEndTime());
        Assert.assertEquals("getNotes", ts.getNotes(), result.getNotes());
    }

    private SqlFilter createFilter() {
        return TimeSliceSql.createFilter(this.timeSliceFilter);
    }
}
