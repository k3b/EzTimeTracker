package de.k3b.csv2db.csv;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import de.k3b.timetracker.database.ICategoryRepsitory;
import de.k3b.timetracker.model.TimeSlice;

/**
 * Iterator that goes through timeslice items in a csv-file
 * 
 * @author k3b
 */
public class CsvTimeSliceIterator implements Iterator<TimeSlice>, Closeable {
	private final CsvReader reader;
	private TimeSliceCsvInterpreter interpreter = null;
	private TimeSlice nextItem = null;
	private ICategoryRepsitory categoryRepository;
	private boolean mustInit = true;

	public CsvTimeSliceIterator(Reader _reader,
			ICategoryRepsitory categoryRepository) {
		this.reader = new CsvReader(_reader);
		this.categoryRepository = categoryRepository;
	}

	private void init() {
		if (mustInit) {
			mustInit = false;
			String[] headerColumns = reader.readLine();
			if (headerColumns != null) {
				this.interpreter = new TimeSliceCsvInterpreter(
						categoryRepository, headerColumns);

				// read first item ahead next item
				next();
			}
		}
	}

	@Override
	public boolean hasNext() {
		init();
		return (this.nextItem != null);
	}

	@Override
	public TimeSlice next() {
		init();
		TimeSlice result = this.nextItem;
		if (this.interpreter != null) {
			try {
				this.nextItem = this.interpreter.parse(reader.readLine());
			} catch (CsvException e) {
				throw new CsvException(this.reader.getLineNumner(), e);
			}
		}
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("CsvTimeSliceIterator.remove()");
	}

	@Override
	public void close() throws IOException {
		this.reader.close();
		this.nextItem = null;
	}

}
