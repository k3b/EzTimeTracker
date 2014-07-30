package de.k3b.csv2db.csv;

import java.text.ParseException;

public class CsvException extends RuntimeException {
	public CsvException(String columnName, int columnNumber,
			String columnValue, Exception e) {
		super("Error reading column " + columnName + "[#" + columnNumber + "]='"
				+ columnValue + "' : " + e.getMessage() , e);
	}

	public CsvException(int lineNumner, CsvException e) {
		super("Line #" + lineNumner + ": " + e.getMessage() , e);
	}

	private static final long serialVersionUID = 6759186369477039441L;

}
