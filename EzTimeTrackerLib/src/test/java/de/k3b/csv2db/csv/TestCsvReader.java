package de.k3b.csv2db.csv;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;

import org.junit.After;
import org.junit.Test;

import de.k3b.csv2db.csv.CsvReader;

public class TestCsvReader {
	private CsvReader parser = null;

	public static Reader createTestReader(String csvSrc) {
		return new java.io.StringReader(csvSrc);
		// return new InputStreamReader(new
		// ByteArrayInputStream(csvSrc.getBytes("UTF-8")));
	}

	@After
	public void tearDown() throws IOException {
		if (parser != null)
			parser.close();
	}

	@Test
	public void emptyReaderShouldReturnNoData() {
		Reader inputStream = createTestReader("");
		parser = new CsvReader(inputStream);

		String[] header = parser.readLine();
		assertNull(header);
	}

	@Test
	public void shouldHandleUnbalancedQuote() {
		Reader inputStream = createTestReader("\"");
		parser = new CsvReader(inputStream);

		String[] header = parser.readLine();
	}

	@Test
	public void shouldReturn2TabColums() {
		Reader inputStream = createTestReader("a\tb");
		parser = new CsvReader(inputStream);

		String[] header = parser.readLine();
		assertEquals(2, header.length);
	}

	@Test
	public void shouldReturn2SemicolonColums() {
		Reader inputStream = createTestReader("a;\"b;something\nmulti;line\"");
		parser = new CsvReader(inputStream);

		String[] header = parser.readLine();
		assertEquals(2, header.length);
	}

	@Test
	public void shouldReturnNullOn2ndLine() {
		Reader inputStream = createTestReader("a;b");
		parser = new CsvReader(inputStream);

		parser.readLine();
		String[] header = parser.readLine();
		assertNull(header);
	}

	@Test
	public void shouldNotReturnNullOn2ndLine() {
		Reader inputStream = createTestReader("a;b\nc");
		parser = new CsvReader(inputStream);

		parser.readLine();
		String[] header = parser.readLine();
		assertNotNull(header);
	}

	@Test
	public void shouldCountLines() {
		Reader inputStream = createTestReader("a\n\"b;something\nmulti;line\"\n");
		parser = new CsvReader(inputStream);

		while (null != parser.readLine()) {
		}
		;

		assertEquals(3, parser.getLineNumner());
	}

	@Test
	public void shouldCountRecordNumber() {
		Reader inputStream = createTestReader("a\n\"b;something\nmulti;line\"\n");
		parser = new CsvReader(inputStream);

		while (null != parser.readLine()) {
		}
		;

		assertEquals(2, parser.getRecordNumber());
	}
}
