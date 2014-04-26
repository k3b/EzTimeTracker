package de.k3b.csv2db.csv;

import static org.junit.Assert.*;

import java.io.Reader;

import org.junit.Test;

import de.k3b.csv2db.csv.CsvReader;

public class TestCsvReader {
	public static Reader createTestReader(String csvSrc) {
		return new java.io.StringReader(csvSrc);
		// return new InputStreamReader(new ByteArrayInputStream(csvSrc.getBytes("UTF-8")));
	}

	@Test
	public void emptyReaderShouldReturnNoData() throws Throwable {
		Reader inputStream = createTestReader("");
		CsvReader parser = new CsvReader(inputStream);
		
		String[] header = parser.readLine();
		assertNull(header);
	}

	@Test
	public void shouldReturn2TabColums() throws Throwable {
		Reader inputStream = createTestReader("a\tb");
		CsvReader parser = new CsvReader(inputStream);
		
		String[] header = parser.readLine();
		assertEquals(2, header.length);
	}

	@Test
	public void shouldReturn2SemicolonColums() throws Throwable {
		Reader inputStream = createTestReader("a;\"b;something\nmulti;line\"");
		CsvReader parser = new CsvReader(inputStream);
		
		String[] header = parser.readLine();
		assertEquals(2, header.length);
	}

	@Test
	public void shouldReturnNullOn2ndLine() throws Throwable {
		Reader inputStream = createTestReader("a;b");
		CsvReader parser = new CsvReader(inputStream);
		
		parser.readLine();
		String[] header = parser.readLine();
		assertNull(header);
	}
	
	@Test
	public void shouldNotReturnNullOn2ndLine() throws Throwable {
		Reader inputStream = createTestReader("a;b\nc");
		CsvReader parser = new CsvReader(inputStream);
		
		parser.readLine();
		String[] header = parser.readLine();
		assertNotNull(header);
	}

	@Test
	public void shouldCountLines() throws Throwable {
		Reader inputStream = createTestReader("a\n\"b;something\nmulti;line\"\n");
		CsvReader parser = new CsvReader(inputStream);
		
		while (null!=parser.readLine()) {};
		
		assertEquals(3, parser.getLineNumner());
	}

	@Test
	public void shouldCountRecordNumber() throws Throwable {
		Reader inputStream = createTestReader("a\n\"b;something\nmulti;line\"\n");
		CsvReader parser = new CsvReader(inputStream);
		
		while (null!=parser.readLine()) {};
		
		assertEquals(2, parser.getRecordNumber());
	}
}
