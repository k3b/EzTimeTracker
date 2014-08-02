package de.k3b.csv2db.csv;

/**
 * thrown if csv parsing failed
 */
public class CsvException extends RuntimeException {
    private static final long serialVersionUID = 6759186369477039441L;

    public CsvException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvException(String columnName, int columnNumber,
                        String columnValue, Exception e) {
        super("Error reading column " + columnName + "[#" + columnNumber + "]='"
                + columnValue + "' : " + e.getMessage(), e);
    }

    public CsvException(int lineNumner, CsvException e) {
        super("Line #" + lineNumner + ": " + e.getMessage(), e);
    }

}
