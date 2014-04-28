package de.k3b.csv2db.csv;

import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import de.k3b.timetracker.model.TimeSlice;

public class CsvTimeSliceIteratorTest {

	private CsvTimeSliceIterator iter = null;
	@After
	public void tearDown() throws Exception {
		if (iter != null) {
			iter.close();
		}
	}

	@Test
	public void shouldReadContent() {
		iter = createIterator("Notes\n#1");
		Assert.assertEquals("#1", iter.next().getNotes());
	}

	@Test
	public void shouldRead3Items() {
		iter = createIterator("Notes\n#1\n#2\n#3");
		int count = 0;
		while (iter.hasNext()) {
			iter.next();
			count++;
		}
		Assert.assertEquals(3, count);
	}

	@Test
	public void shouldReadDemoDataFromAssets() {
		Reader reader = new InputStreamReader(CsvTimeSliceIterator.class.getResourceAsStream("/DemoData.csv"));
		// Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("/DemoData.csv"));
		iter = new CsvTimeSliceIterator(reader, new CategoryRepositoryMock());
		int count = 0;
		while (iter.hasNext()) {
			iter.next();
			count++;
		}
	}

	@Test
	public void shouldHandleNoHeader() {
		iter = createIterator("");
		Assert.assertEquals(false, iter.hasNext());
	}

	@Test
	public void shouldHandleNoData() {
		iter = createIterator("start");
		Assert.assertEquals(false, iter.hasNext());
	}


	@Test
	public void shouldThrowOnIllegalFormat() {
		iter = createIterator("start\nNotADate");
		try {
			iter.next();
			Assert.fail("missing exception");
		} catch (CsvException e) {
			System.out.println("got expected exception " + e.getMessage());
		}
	}

	private CsvTimeSliceIterator createIterator(String csvSrc) {
		Reader reader = new java.io.StringReader(csvSrc);
		iter = new CsvTimeSliceIterator(reader, null);
		
		return iter;
	}

}
