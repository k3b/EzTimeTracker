package de.k3b.timetracker.database;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import de.k3b.timetracker.model.TimeSliceCategory;

/**
 * Created by EVE on 02.08.2014.
 */
public class TimeSliceCategorySqlTests {
    private final TimeSliceCategory cat = new TimeSliceCategory(1, "name").setStartTime(100).setEndTime(200).setDescription("descripton");

    @Test
    public void columnListShouldContainAllFlieds() {
        Map<String, String> values = TimeSliceCategorySql.asMap(cat);
        Assert.assertEquals(TimeSliceCategorySql.allColumnNames().length, values.size());
    }

    @Test
    public void shouldTransferAllFlieds() {
        Map<String, String> values = TimeSliceCategorySql.asMap(cat);
        TimeSliceCategory result = new TimeSliceCategory();
        TimeSliceCategorySql.fromMap(result, values);
        Assert.assertEquals("getRowId", cat.getRowId(), result.getRowId());
        Assert.assertEquals("getCategoryName", cat.getCategoryName(), result.getCategoryName());
        Assert.assertEquals("getStartTime", cat.getStartTime(), result.getStartTime());
        Assert.assertEquals("getEndTime", cat.getEndTime(), result.getEndTime());
        Assert.assertEquals("getDescription", cat.getDescription(), result.getDescription());
    }
}
