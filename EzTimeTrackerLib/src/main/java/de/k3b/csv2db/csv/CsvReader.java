package de.k3b.csv2db.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

/**
 * Raw processing of csv reader That gets lines of csv-columns. Features: Infer
 * column-Delimiter Handle multiline columns if surrounded with ""
 * 
 * @author EVE
 * 
 */
public class CsvReader implements Closeable {

	public static final char FIELDLEN_DELIMITER = ':';
	private static final char CHAR_LINE_DELIMITER = '\n';
	private static final char CHAR_IGNORE = '\r';
	private static final char CHAR_FIELD_SURROUNDER = '\"';
	private char fieldDelimiter = 0;
	private char fieldSurrounder = 0; // != 0: look for matching -"- to allow
										// multiline fields

	private Reader reader;

	// csv file source line number for error messages. (lineNumber >
	// recordNumber) if there is a record with multiline data.
	private int lineNumber = 0;

	// csv recordnumber
	private int recordNumber = 0;

	public CsvReader(Reader reader) {
		this.reader = reader;
	}

	public String[] readLine() {
		final String trennChars = ",;\t";
		Vector<String> result = new Vector<String>();
		StringBuffer content = new StringBuffer();
		this.fieldSurrounder = 0;

		try {
			// this.reader = new
			// Reader(getClass().getResourceAsStream("/data.csv"));
			int ch;
			while ((ch = this.reader.read()) != -1) // ,0,cbuf.length) > 0)
			{
				if (ch == CHAR_LINE_DELIMITER)
					this.lineNumber++;

				if (this.fieldSurrounder == 0) {
					if (fieldDelimiter == 0) {
						// fieldDelimiter unknown: infer
						if (trennChars.indexOf(ch) >= 0)
							fieldDelimiter = (char) ch;
					}
					if (ch == fieldDelimiter) {
						result.addElement(getStringWithoutDelimiters(content));
						content.setLength(0);
					} else if (ch == CHAR_LINE_DELIMITER) {
						result.addElement(getStringWithoutDelimiters(content));
						this.recordNumber++;
						return toStringArray(result);
					} else if (ch != CHAR_IGNORE) {
						content.append((char) ch);
					}

					if (ch == CHAR_FIELD_SURROUNDER)
						this.fieldSurrounder = (char) ch; // start -"- area
				} else {
					// waiting for end--"-
					if (ch != CHAR_IGNORE) {
						content.append((char) ch);
						if (ch == this.fieldSurrounder)
							this.fieldSurrounder = 0;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (content.length() > 0) {
			result.addElement(getStringWithoutDelimiters(content));
		}

		if (result.isEmpty()) {
			return null;
		} else {
			this.recordNumber++;
			return toStringArray(result);
		}
	}

	private String[] toStringArray(Vector<String> result) {
		int len = result.size();
		if (len == 0) {
			return null;
		}

		String[] array = new String[len];
		result.copyInto(array);
		return array;
	}

	/**
	 * 
	 * @param content
	 *            that may contain starting and ending -"-
	 * @return string without starting and ending -"-
	 */
	static private String getStringWithoutDelimiters(StringBuffer content) {
		if (content.length() > 1) {
			if (content.charAt(content.length() - 1) == CHAR_FIELD_SURROUNDER)
				content.deleteCharAt(content.length() - 1);
			if (content.charAt(0) == CHAR_FIELD_SURROUNDER)
				content.deleteCharAt(0);
		}
		if (content.length() > 0)
			return content.toString();
		return null;
	}

	public int getLineNumner() {
		return this.lineNumber;
	}

	public int getRecordNumber() {
		return this.recordNumber;
	}

	@Override
	public void close() throws IOException {
		this.reader.close();
	}
}
